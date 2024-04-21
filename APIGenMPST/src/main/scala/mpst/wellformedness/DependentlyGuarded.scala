package mpst.wellformedness

import mpst.syntax.Protocol
import mpst.syntax.Protocol.*
import mpst.utilities.Types.*

/* IDEA:
  - check well-formedness on *recursion*
    - a loop is dependently guarded if:
      - for all actions l
      - the loop body can only *partially *terminate* for l
      - if l does not occur in the loop body at all
    - other words:
      - if a participant occurs in some branch of the loop
      - it must occur in all branches of the loop

  @ telmo -
    test this!
*/

object DependentlyGuarded:
  private def isDependentlyGuarded(global:Protocol):Boolean =
    global match
      case Interaction(_, _, _, _) => true
      case RecursionCall(_) => true
      case End => true
      case Sequence(globalA,globalB) => isDependentlyGuarded(globalA) && isDependentlyGuarded(globalB)
      case Parallel(globalA,globalB) => isDependentlyGuarded(globalA) && isDependentlyGuarded(globalB)
      case Choice  (globalA,globalB) => isDependentlyGuarded(globalA) && isDependentlyGuarded(globalB)
      case RecursionFixedPoint(_,globalB) =>
        val agents = getAgents(globalB)
        for agent <- agents yield
          val maybeGlobal = checkDependentlyGuarded(globalB,agent)
          maybeGlobal match
            case Some(global) if globalB != global => throw new RuntimeException(s"[$globalB] partially evaluates to [$global] for agent [$agent] yet they are different\n")
            case _ =>
        isDependentlyGuarded(globalB)
      case local => throw new RuntimeException(s"unexpected local type found in [$local]\n")
  end isDependentlyGuarded

  private def checkDependentlyGuarded(global:Protocol, agent:Agent):Option[Protocol] =
    global match
      case Interaction(agentA,agentB,_, _) =>
        if agent == agentA || agent == agentB
        then None
        else Some(global)
      case RecursionCall(_) => Some(global)
      case End => Some(global)
      case Sequence(globalA,globalB) =>
        val maybeGlobalA = checkDependentlyGuarded(globalA,agent)
        val maybeGlobalB = checkDependentlyGuarded(globalB,agent)
        maybeGlobalA -> maybeGlobalB match
          case None -> _ => None
          case _ -> None => None
          case Some(gA) -> Some(gB) => Some(Sequence(gA,gB))
      case Parallel(globalA,globalB) =>
        val maybeGlobalA = checkDependentlyGuarded(globalA,agent)
        val maybeGlobalB = checkDependentlyGuarded(globalB,agent)
        maybeGlobalA -> maybeGlobalB match
          case None -> _ => None
          case _ -> None => None
          case Some(gA) -> Some(gB) => Some(Parallel(gA,gB))
      case Choice(globalA,globalB) =>
        val maybeGlobalA = checkDependentlyGuarded(globalA,agent)
        val maybeGlobalB = checkDependentlyGuarded(globalB,agent)
        maybeGlobalA -> maybeGlobalB match
          case None -> maybeGlobalB => maybeGlobalB
          case maybeGlobalA -> None => maybeGlobalA
          case Some(gA) -> Some(gB) => Some(Choice(gA,gB))
      case RecursionFixedPoint(variable,globalB) =>
        val maybeGlobalB = checkDependentlyGuarded(globalB,agent)
        maybeGlobalB match
          case Some(gB) => Some(gB)
          case _ => Some(End)
      case local => throw new RuntimeException(s"unexpected local type found in [$local]\n")
  end checkDependentlyGuarded

  def apply(global:Protocol):Boolean =
    isDependentlyGuarded(global)
  end apply
end DependentlyGuarded