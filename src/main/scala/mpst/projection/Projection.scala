package mpst.projection

import mpst.syntax.Protocol
import mpst.syntax.Protocol._

object Projection:
  def roles(global: Protocol): Set[String] =
    global match
      // terminal cases //
      case Interaction(agentA, agentB, _) => (Set() + agentA) + agentB
      case End                            => Set()
      case RecursionCall(_)               => Set()
      // recursive cases //
      case RecursionFixedPoint(_, globalB) => roles(globalB)
      case Sequence(globalA, globalB)      => roles(globalA) ++ roles(globalB)
      case Parallel(globalA, globalB)      => roles(globalA) ++ roles(globalB)
      case Choice  (globalA, globalB)      => roles(globalA) ++ roles(globalB)
      // unexpected cases //
      case _ => throw new RuntimeException("Expected: GlobalType\nFound: LocalType")
  end roles

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
      case _ => throw new RuntimeException("Expected GlobalType\nFound: LocalType")
  end projection

  def apply(global: Protocol, role: String): Protocol = Protocol(projection(global, role))
end Projection