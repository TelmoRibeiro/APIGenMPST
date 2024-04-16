package mpst.analysis

import mpst.analysis.Graph.*
import mpst.syntax.Protocol
import mpst.syntax.Protocol.*


object BoundedCondition:
  def isBounded(protocol:Protocol):Boolean =
    protocol match
      case Send(_,_,_,_)    => true
      case RecursionCall(_) => true
      case End => true
      case Sequence(protocolA,protocolB) => isBounded(protocolA) && isBounded(protocolB)
      case Parallel(protocolA,protocolB) => isBounded(protocolA) && isBounded(protocolB) // can this not be the case?
      case Choice  (protocolA,protocolB) => isBounded(protocolA) && isBounded(protocolB)
      case RecursionFixedPoint(_,protocolB) => buildGraph(protocolB).forall(isStronglyConnected) && isBounded(protocolB)
      case local => throw new RuntimeException(s"unexpected local type found in $local")
  end isBounded

  // @ telmo - dfs this
  private def isStronglyConnected(graph:Graph):Boolean = true