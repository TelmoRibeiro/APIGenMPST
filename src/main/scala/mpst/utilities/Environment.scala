package mpst.utilities

import mpst.syntax.Protocol
import mpst.syntax.Protocol.*
import mpst.utilities.Types.*


object Environment:
  // @ telmo - used to get the environment out of global
  // @ telmo - ASSUMES NO REPETITION OF VARIABLES
  def getEnvironment(protocol:Protocol)(using environment:Map[Variable,Protocol]):Map[Variable,Protocol] =
    protocol match
      case Interaction(agentA,agentB,message,sort) => Map()
      case Send   (agentA,agentB,message,sort) => Map()
      case Receive(agentA,agentB,message,sort) => Map()
      case RecursionCall(variable) => Map()
      case End => Map()
      case Sequence(protocolA,protocolB) =>
        val environmentA = getEnvironment(protocolA)
        val environmentB = getEnvironment(protocolB)(using environmentA)
        environmentB
      case Parallel(protocolA,protocolB) => getEnvironment(protocolA) ++ getEnvironment(protocolB)
      case Choice  (protocolA,protocolB) => getEnvironment(protocolA) ++ getEnvironment(protocolB)
      case RecursionFixedPoint(variable,protocolB) => getEnvironment(protocolB)(using environment+(variable->protocol))
  end getEnvironment
end Environment