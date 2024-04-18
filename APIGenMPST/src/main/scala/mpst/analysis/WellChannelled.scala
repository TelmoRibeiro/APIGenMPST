package mpst.analysis

import mpst.syntax.Protocol
import mpst.syntax.Protocol.*
import mpst.utilities.Types.*

/* IDEA:
  - check well-formedness (linearity?) on *parallel* branches
    - channel = sendingAgent -> receivingAgent
    - if channels are shared in both branches then *error*
    - otherwise communications are secure since they are independent
    - error = "possible dependent communications in branch [globalA] and [globalB] caused by the shared channels [sharedChannels]"

  @ telmo -
    can we not relax this condition?
    there is no cases where shared channels are not risky?
    not even trying to disambiguate by message and sort that pass in the channel
*/

object WellChannelled:
  private def isWellChannelled(global:Protocol):Boolean =
    global match
      case Interaction(_,_,_,_) => true
      case RecursionCall(_) => true
      case End => true
      case Sequence(globalA,globalB) => isWellChannelled(globalA) && isWellChannelled(globalB)
      case Parallel(globalA,globalB) =>
        val channelsA = channels(globalA)
        val channelsB = channels(globalB)
        val sharedChannels = channelsA intersect channelsB
        if sharedChannels.isEmpty
        then true
        else throw new RuntimeException(s"possible dependent communications in branch [$globalA] and [$globalB] caused by the shared channels [$sharedChannels]\n")
      case Choice(globalA,globalB) => isWellChannelled(globalA) && isWellChannelled(globalB)
      case RecursionFixedPoint(_,globalB) => isWellChannelled(globalB)
      case local => throw new RuntimeException(s"unexpected local type found in [$local\n]")
  end isWellChannelled

  // @ telmo - maybe rename branch to global or even protocol and serve it as an util?
  private def channels(branch:Protocol):Set[(Agent,Agent)] =
    branch match
      case Interaction(agentA,agentB,_,_) => Set() + (agentA -> agentB)
      case RecursionCall(_) => Set()
      case End              => Set()
      case Sequence(globalA,globalB) => channels(globalA) ++ channels(globalB)
      case Parallel(globalA,globalB) => channels(globalA) ++ channels(globalB)
      case Choice  (globalA,globalB) => channels(globalA) ++ channels(globalB)
      case RecursionFixedPoint(_,globalB) => channels(globalB)
      case local => throw new RuntimeException(s"unexpected local type found in [$local]\n")
  end channels

  def apply(global:Protocol):Boolean =
    isWellChannelled(global)
  end apply
end WellChannelled

/* @ telmo -
  previous implementation
  too restrictive... instead of shared channels we were checking for shared roles!

object Linearity:
  private def linearity(global: Protocol): Boolean =
    global match
      // terminal cases //
      case Interaction(_, _, _, _) => true
      case RecursionCall(_) => true
      case End              => true
      // recursive cases //
      case RecursionFixedPoint(_, globalB) => linearity(globalB)
      case Sequence(globalA, globalB)      => linearity(globalA) && linearity(globalB)
      case Parallel(globalA, globalB)      =>
        val iterationsA: Set[Protocol] = interactions(globalA)
        val iterationsB: Set[Protocol] = interactions(globalB)
        (iterationsA intersect iterationsB).isEmpty
      case Choice  (globalA, globalB)      => linearity(globalA) && linearity(globalB)
      // unexpected cases //
      case local => throw new RuntimeException(s"unexpected local type $local found\n")
  end linearity

  def apply(global: Protocol): Boolean = linearity(global)
end Linearity
*/