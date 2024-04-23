package mpst.syntax

import mpst.utilities.Types.*

enum Protocol:
  override def toString:String =
    this match
      case Interaction(agentA,agentB,message,sort) => s"$agentA>$agentA:$message<$sort>"
      case Send   (agentA,agentB,message,sort) => s"$agentA$agentB!$message<$sort>"
      case Receive(agentA,agentB,message,sort) => s"$agentA$agentB?$message<$sort>"
      case RecursionCall(variable) => s"$variable"
      case End => s"end"
      case Sequence(protocolA,protocolB) => s"$protocolA ; $protocolB"
      case Parallel(protocolA,protocolB) => s"($protocolA || $protocolB)"
      case Choice  (protocolA,protocolB) => s"($protocolA + $protocolB)"
      case RecursionFixedPoint(variable,protocolB) => s"def $variable in ($protocolB)"
  end toString

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