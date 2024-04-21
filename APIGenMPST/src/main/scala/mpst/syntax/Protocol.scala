package mpst.syntax

import mpst.utilities.Types.*

enum Protocol:
  case Interaction(agentA:Agent,agentB:Agent,message:Message,sort:Sort)
  case Send       (agentA:Agent,agentB:Agent,message:Message,sort:Sort)
  case Receive    (agentA:Agent,agentB:Agent,message:Message,sort:Sort)
  case RecursionCall(variable:Variable)
  case End
  case Sequence(protocolA:Protocol,protocolB:Protocol)
  case Parallel(protocolA:Protocol,protocolB:Protocol)
  case Choice  (protocolA:Protocol,protocolB:Protocol)
  case RecursionFixedPoint(variable:Variable,protocolB:Protocol)
end Protocol

object Protocol:
  def getAgents(protocol:Protocol):Set[Agent] =
    protocol match
      case Interaction(agentA,agentB,_,_) => (Set() + agentA) + agentB
      case Send   (agentA,agentB,_,_) => (Set() + agentA) + agentB
      case Receive(agentA,agentB,_,_) => (Set() + agentA) + agentB
      case RecursionCall(_) => Set()
      case End => Set()
      case Sequence(protocolA,protocolB) => getAgents(protocolA) ++ getAgents(protocolB)
      case Parallel(protocolA,protocolB) => getAgents(protocolA) ++ getAgents(protocolB)
      case Choice  (protocolA,protocolB) => getAgents(protocolA) ++ getAgents(protocolB)
      case RecursionFixedPoint(_,protocolB) => getAgents(protocolB)
  end getAgents
end Protocol