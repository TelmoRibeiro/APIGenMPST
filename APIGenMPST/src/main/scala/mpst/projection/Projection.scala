package mpst.projection

import mpst.syntax.Protocol
import mpst.syntax.Protocol.*
import mpst.utilities.Simplifier
import mpst.utilities.Types.*


object Projection:
  def projectionWithAgent(global:Protocol):Set[(Agent,Protocol)] =
    val agents = getAgents(global)
    for agent <- agents yield
      val local = apply(agent, global)
      agent -> local
  end projectionWithAgent

  def projection(global:Protocol)(using agent:Agent):Option[Protocol] =
    global match
      case Interaction(agentA,agentB,message,sort) =>
        if      agent == agentA then Some(Send   (agentA,agentB,message,sort))
        else if agent == agentB then Some(Receive(agentB,agentA,message,sort))
        else    None
      case RecursionCall(variable) => Some(global)
      case End => Some(global)
      case RecursionFixedPoint(variable, globalB) =>
        val maybeLocalB: Option[Protocol] = projection(globalB)
        maybeLocalB match
          // may be too restrictive but allows projection's correctness //
          case Some(RecursionCall(variable)) => None
          case None                          => None
          case Some(localB)                  => Some(RecursionFixedPoint(variable, localB))
      case Sequence(globalA, globalB) =>
        val maybeLocalA: Option[Protocol] = projection(globalA)
        val maybeLocalB: Option[Protocol] = projection(globalB)
         maybeLocalA -> maybeLocalB match
          case Some(localA) -> Some(localB) => Some(Sequence(localA, localB))
          case Some(localA) -> None         => Some(localA)
          case None -> Some(localB)         => Some(localB)
          case None -> None                 => None
      case Parallel(globalA, globalB) =>
        val maybeLocalA: Option[Protocol] = projection(globalA)
        val maybeLocalB: Option[Protocol] = projection(globalB)
        maybeLocalA -> maybeLocalB match
          case Some(localA) -> Some(localB) => Some(Parallel(localA, localB))
          case Some(localA) -> None         => Some(localA)
          case None -> Some(localB)         => Some(localB)
          case None -> None                 => None
      case Choice  (globalA, globalB) =>
        val maybeLocalA: Option[Protocol] = projection(globalA)
        val maybeLocalB: Option[Protocol] = projection(globalB)
        maybeLocalA -> maybeLocalB match
          case Some(localA) -> Some(localB) if localA == localB => Some(localA)
          case Some(localA) -> Some(localB) => Some(Choice(localA, localB))
          case Some(localA) -> None         => Some(localA)
          case None -> Some(localB)         => Some(localB)
          case None -> None                 => None
      // unexpected cases //
      case _ => None
  end projection

  def apply(role: String, global: Protocol): Protocol =
    val maybeLocal: Option[Protocol] = projection(global)(using role)
    maybeLocal match
      case Some(local) => Simplifier(local)
      case None        => throw new RuntimeException(s"could not project global type in $global\n")
  end apply
end Projection