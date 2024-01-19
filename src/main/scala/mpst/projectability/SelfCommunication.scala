package mpst.projectability

import mpst.syntax.Protocol
import mpst.syntax.Protocol._

object SelfCommunication:
  private def selfCommunication(global: Protocol): Boolean =
    global match
      // terminal cases //
      case Interaction(agentA, agentB, _) => agentA != agentB
      case RecursionCall(_)               => true
      // recursive cases //
      case RecursionFixedPoint(_, globalB) => selfCommunication(globalB)
      case Sequence(globalA, globalB)      => selfCommunication(globalA) && selfCommunication(globalB)
      case Parallel(globalA, globalB)      => selfCommunication(globalA) && selfCommunication(globalB)
      case Choice  (globalA, globalB)      => selfCommunication(globalA) && selfCommunication(globalB)
      // unexpected cases //
      case _ => throw new RuntimeException("\nExpected:\tGlobalType\nFound:\t\tLocalType")
  end selfCommunication
  
  def apply(global: Protocol): Boolean = selfCommunication(global)
end SelfCommunication