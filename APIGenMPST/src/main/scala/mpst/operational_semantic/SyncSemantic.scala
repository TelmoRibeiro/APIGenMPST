package mpst.operational_semantic

import mpst.syntax.Protocol
import mpst.syntax.Protocol.*
import mpst.utilities.StructuralCongruence
import mpst.utilities.Types.*

object SyncSemantic:
  def accept(protocol:Protocol):Boolean =
    SyncSemantic.acceptAuxiliary(protocol)
  end accept

  def next(protocol:Protocol)(using environment:Map[Variable,Protocol]):Set[(Action,Protocol)] =
    SyncSemantic.nextAuxiliary(protocol).toSet
  end next

  private def acceptAuxiliary(local:Protocol):Boolean =
    local match
      case Interaction(_,_, _, _) => false
      case Send   (_,_,_,_) => false
      case Receive(_,_,_,_) => false
      case RecursionCall(_) => false // checked with prof. José Proença //
      case Skip             => true
      case Sequence(localA,localB) => accept(localA) && accept(localB)
      case Parallel(localA,localB) => accept(localA) && accept(localB)
      case Choice  (localA,localB) => accept(localA) || accept(localB)
      case RecursionFixedPoint(_,localB) => accept(localB)
  end acceptAuxiliary

  private def nextAuxiliary(protocol:Protocol)(using environment:Map[Variable,Protocol]):List[(Action,Protocol)] =
    protocol match
      case Interaction(agentA,agentB,message,sort) => List(Send(agentA,agentB,message,sort) -> Receive(agentB,agentA,message,sort))
      case Send   (agentA,agentB,message,sort) => List(protocol -> Skip) // @ telmo - avoid inserting "harmful" ends by inserting "harmless" skips
      case Receive(agentA,agentB,message,sort) => List(protocol -> Skip) // @ telmo - avoid inserting "harmful" ends by inserting "harmless" skips
      case RecursionCall(variable) =>
        val protocolB = environment(variable)
        val nonRecursiveProtocolB = recursionFree(variable,protocolB)
        for nextActionB -> nextProtocolB <- nextAuxiliary(nonRecursiveProtocolB) yield
          nextActionB -> consumeAction(nextActionB,protocolB)
      case Skip => Nil
      case Sequence(protocolA,protocolB) =>
        val nextA = nextAuxiliary(protocolA)
        val nextB = nextAuxiliary(protocolB)
        val resultA = for nextActionA -> nextProtocolA <- nextA yield
          nextActionA -> StructuralCongruence(Sequence(nextProtocolA,protocolB))
        val resultB = if accept(protocolA) then nextB else Nil
        resultA ++ resultB
      case Parallel(protocolA,protocolB) =>
        val nextA = nextAuxiliary(protocolA)
        val nextB = nextAuxiliary(protocolB)
        val resultA = for nextActionA -> nextProtocolA <- nextA yield
          nextActionA -> StructuralCongruence(Parallel(nextProtocolA,protocolB))
        val resultB = for nextActionB -> nextProtocolB <- nextB yield
          nextActionB -> StructuralCongruence(Parallel(protocolA,nextProtocolB))
        resultA ++ resultB
      case Choice(protocolA,protocolB) =>
        val nextA = nextAuxiliary(protocolA)
        val nextB = nextAuxiliary(protocolB)
        nextA ++ nextB
      case RecursionFixedPoint(variable,protocolB) =>
        nextAuxiliary(protocolB)
  end nextAuxiliary

  private def recursionFree(recursionVariable:Variable,protocol:Protocol):Protocol =
    def recursionFreeAuxiliary(protocol:Protocol)(using recursionVariable:Variable):Protocol =
      protocol match
        case Interaction(_,_,_,_) => protocol
        case Send   (_,_,_,_) => protocol
        case Receive(_,_,_,_) => protocol
        case RecursionCall(variable) => if variable == recursionVariable then Skip else protocol
        case Skip => protocol
        case Sequence(protocolA,protocolB) => Sequence(recursionFreeAuxiliary(protocolA),recursionFreeAuxiliary(protocolB))
        case Parallel(protocolA,protocolB) => Parallel(recursionFreeAuxiliary(protocolA),recursionFreeAuxiliary(protocolB))
        case Choice  (protocolA,protocolB) => Choice  (recursionFreeAuxiliary(protocolA),recursionFreeAuxiliary(protocolB))
        case RecursionFixedPoint(variable, protocolB) => RecursionFixedPoint(variable,recursionFreeAuxiliary(protocolB))
    end recursionFreeAuxiliary
    StructuralCongruence(recursionFreeAuxiliary(protocol)(using recursionVariable))
  end recursionFree

  private def consumeAction(action:Action,protocol:Protocol):Protocol =
    def consumeActionAuxiliary(protocol:Protocol)(using action:Action):Protocol =
      protocol match
        case Interaction(_,_,_,_) => if action == protocol then Skip else protocol
        case Send   (_,_,_,_)     => if action == protocol then Skip else protocol
        case Receive(_,_,_,_)     => if action == protocol then Skip else protocol
        case RecursionCall(_) => protocol
        case Skip => protocol
        case RecursionFixedPoint(variable,protocolB) => RecursionFixedPoint(variable,consumeActionAuxiliary(protocolB))
        case Sequence(protocolA,protocolB) =>
          val pA = consumeActionAuxiliary(protocolA)
          if  pA == protocolA
          then Sequence(protocolA,consumeActionAuxiliary(protocolB))
          else Sequence(pA,protocolB)
        case Parallel(protocolA,protocolB) => Parallel(consumeActionAuxiliary(protocolA),consumeActionAuxiliary(protocolB))
        case Choice  (protocolA,protocolB) => Choice  (consumeActionAuxiliary(protocolA),consumeActionAuxiliary(protocolB))
    end consumeActionAuxiliary
    StructuralCongruence(consumeActionAuxiliary(protocol)(using action))
  end consumeAction
end SyncSemantic