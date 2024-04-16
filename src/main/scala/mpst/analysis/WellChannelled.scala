package mpst.analysis

import mpst.syntax.Protocol
import mpst.syntax.Protocol.*
import mpst.utilities.Types.*

object WellChannelled:
  private def channels(protocol:Protocol):Set[(Agent,Agent)] =
    protocol match
      case Interaction(agentA, agentB, _, _) => Set(agentA -> agentB)
      case Send   (agentA,agentB,_,_) => Set(agentA -> agentB)
      case Receive(agentA,agentB,_,_) => Set(agentB -> agentA)
      case RecursionCall(_) => Set()
      case End              => Set()
      case Sequence(protocolA,protocolB) => channels(protocolA) ++ channels(protocolB)
      case Parallel(protocolA,protocolB) => channels(protocolA) ++ channels(protocolB)
      case Choice  (protocolA,protocolB) => channels(protocolA) ++ channels(protocolB)
      case RecursionFixedPoint(_, protocolB) => channels(protocolB)
  end channels

  // similar to projectability //
  private def wellChannelled(protocolA:Protocol, protocolB:Protocol):Boolean =
    val channelsA -> channelsB = channels(protocolA) -> channels(protocolB)
    channelsA.intersect(channelsB).isEmpty
  end wellChannelled

  def apply(protocol:Protocol):Boolean =
    protocol match
      case Sequence(protocolA,protocolB) => apply(protocolA) && apply(protocolB)
      case Parallel(protocolA,protocolB) => wellChannelled(protocolA,protocolB)
      case Choice  (protocolA,protocolB) => apply(protocolA) && apply(protocolB)
      case RecursionFixedPoint(_,protocolB) => apply(protocolB)
      case _ => true
  end apply
end WellChannelled