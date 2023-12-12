package mpst.projection

import mpst.syntax.Protocol
import mpst.syntax.Protocol._

object Projection:
  private def projection(global: Protocol, role: String): Protocol =
    global match
      // terminal cases //
      case Interaction(agentA, agentB, message) =>
        if      role == agentA then Send   (agentA, agentB, message)
        else if role == agentB then Receive(agentB, agentA, message)
        else    NoAction
      case End => End
      case RecursionCall(variable) => RecursionCall(variable)
      // recursive cases //
      case RecursionFixedPoint(variable, globalB) => RecursionFixedPoint(variable, projection(globalB, role))
      case Sequence(globalA, globalB) => Sequence(projection(globalA, role), projection(globalB, role))
      case Parallel(globalA, globalB) => Parallel(projection(globalA, role), projection(globalB, role))
      case Choice  (globalA, globalB) => Choice  (projection(globalA, role), projection(globalB, role))
      // unexpected cases //
      case _ => throw new RuntimeException("\nExpected:\tGlobalType\nFound:\t\tLocalType")
  end projection

  def apply(global: Protocol, role: String): Protocol = Protocol(projection(global, role))
end Projection