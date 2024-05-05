package mpst.utilities

import mpst.syntax.Protocol
import mpst.syntax.Protocol.*
import mpst.utilities.Types.*

/*
  IDEA:
  - get the environment (MAP from RecursionVariable to GlobalType)
  - assumes we provide the *whole initial type*
  - assumes *no repetition* of recursion variables (different scopes should still use different variables)

  @ telmo -
    known problems: need a version that can applied in "Network"!
*/

object Environment:
  def getEnvironment(protocol:Protocol)(using environment:Map[Variable,Protocol]):Map[Variable,Protocol] =
    protocol match
      case Interaction(agentA,agentB,message,sort) => environment
      case Send   (agentA,agentB,message,sort) => environment
      case Receive(agentA,agentB,message,sort) => environment
      case RecursionCall(variable) => environment
      case Skip => environment
      case Sequence(protocolA,protocolB) =>
        val environmentA = getEnvironment(protocolA)
        val environmentB = getEnvironment(protocolB)(using environmentA)
        environmentB
      case Parallel(protocolA,protocolB) => getEnvironment(protocolA) ++ getEnvironment(protocolB)
      case Choice  (protocolA,protocolB) => getEnvironment(protocolA) ++ getEnvironment(protocolB)
      case RecursionFixedPoint(variable,protocolB) => getEnvironment(protocolB)(using environment+(variable->protocol))
  end getEnvironment

  def apply(protocol:Protocol):Map[Variable,Protocol] =
    getEnvironment(protocol)(using Map())
  end apply
end Environment