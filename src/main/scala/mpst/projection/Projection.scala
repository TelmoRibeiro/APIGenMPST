package mpst.projection

import mpst.syntax.Protocol
import mpst.syntax.Protocol.*

object Projection:
  private def projection(global: Protocol, role: String): Option[Protocol] =
    global match
      // terminal cases //
      case Interaction(agentA, agentB, message) =>
        if      role == agentA then Some(Send   (agentA, agentB, message))
        else if role == agentB then Some(Receive(agentB, agentA, message))
        else    Some(Skip)
      case RecursionCall(variable) => Some(RecursionCall(variable))
      // recursive cases //
      case RecursionFixedPoint(variable, globalB) => Some(RecursionFixedPoint(variable, projection(globalB, role).get))
      case Sequence(globalA, globalB) => Some(Sequence(projection(globalA, role).get, projection(globalB, role).get))
      case Parallel(globalA, globalB) => Some(Parallel(projection(globalA, role).get, projection(globalB, role).get))
      case Choice  (globalA, globalB) => Some(Choice  (projection(globalA, role).get, projection(globalB, role).get))
      // unexpected cases //
      case _ => None
  end projection

  def apply(global: Protocol, role: String): Protocol = Protocol(projection(global, role).get)
end Projection