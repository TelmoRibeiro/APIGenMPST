package mpst.projection

import mpst.syntax.Protocol
import mpst.syntax.Protocol.*
import mpst.utilities.Simplifier
import mpst.utilities.Types.*

object Projection:
  def projectionWithAgent(global:Protocol):Set[(Agent,Protocol)] =
    val agents = getAgents(global)
    for agent <- agents yield
      val maybeLocal = getProjection(global)(using agent)
      maybeLocal match
        case Some(local) => agent -> Simplifier(local)
        case None        => throw new RuntimeException(s"projection undefined for agent [$agent] in [$global]\n")
  end projectionWithAgent

  def projection(global:Protocol):Set[Protocol] =
    val agents = getAgents(global)
    for agent <- agents yield
      val maybeLocal = getProjection(global)(using agent)
      maybeLocal match
        case Some(local) => Simplifier(local)
        case None        => throw new RuntimeException(s"projection undefined for agent [$agent] in [$global]\n")
  end projection

  private def getProjection(global:Protocol)(using agent:Agent):Option[Protocol] =
    global match
      case Interaction(agentA,agentB,message,sort) =>
        if      agent == agentA then Some(Send   (agentA,agentB,message,sort))
        else if agent == agentB then Some(Receive(agentB,agentA,message,sort))
        else    None
      case RecursionCall(variable) => Some(global)
      case End => Some(global)
      case Sequence(globalA,globalB) =>
        val maybeLocalA = getProjection(globalA)
        val maybeLocalB = getProjection(globalB)
         maybeLocalA -> maybeLocalB match
          case Some(localA) -> Some(localB) => Some(Sequence(localA, localB))
          case Some(localA) -> None         => Some(localA)
          case None -> Some(localB)         => Some(localB)
          case None -> None                 => None
      case Parallel(globalA,globalB) =>
        val maybeLocalA = getProjection(globalA)
        val maybeLocalB = getProjection(globalB)
        maybeLocalA -> maybeLocalB match
          case Some(localA) -> Some(localB) => Some(Parallel(localA, localB))
          case Some(localA) -> None         => Some(localA)
          case None -> Some(localB)         => Some(localB)
          case None -> None                 => None
      case Choice(globalA,globalB) =>
        val maybeLocalA = getProjection(globalA)
        val maybeLocalB = getProjection(globalB)
        maybeLocalA -> maybeLocalB match
          case Some(localA) -> Some(localB) if localA == localB => Some(localA)
          case Some(localA) -> Some(localB) => Some(Choice(localA, localB))
          case Some(localA) -> None         => Some(localA)
          case None -> Some(localB)         => Some(localB)
          case None -> None                 => None
      case RecursionFixedPoint(variable,globalB) =>
        val maybeLocalB = getProjection(globalB)
        maybeLocalB match
          case Some(RecursionCall(variable)) => None // @ telmo - may be too restrictive but allows projection's correctness
          case None         => None
          case Some(localB) => Some(RecursionFixedPoint(variable,localB))
      case _ => None
  end getProjection
end Projection