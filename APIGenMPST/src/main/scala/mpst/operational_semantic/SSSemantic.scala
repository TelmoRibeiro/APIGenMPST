package mpst.operational_semantic

import mpst.syntax.Protocol
import mpst.syntax.Protocol.*
import mpst.utilities.Simplifier
import mpst.utilities.Types.*

// STRONG SEQ SEMANTIC //
object SSSemantic:
  def accept(local:Protocol):Boolean =
    SSSemantic.acceptAuxiliary(local)
  end accept

  def next(local:Protocol)(using environment:Map[Variable,Protocol]):Set[(Action,Protocol)] =
    SSSemantic.nextAuxiliary(local).toSet
  end next

  private def acceptAuxiliary(local:Protocol):Boolean =
    local match
      // terminal cases //
      case Interaction(_,_,_,_) => false
      case Send   (_,_,_,_) => false
      case Receive(_,_,_,_) => false
      case RecursionCall(_) => false
      case End              => true
      case Sequence(localA, localB) => accept(localA) && accept(localB)
      case Parallel(localA, localB) => accept(localA) && accept(localB)
      case Choice  (localA, localB) => accept(localA) || accept(localB)
      case RecursionFixedPoint(_, localB) => accept(localB)
  end acceptAuxiliary

  // using
  private def nextAuxiliary(protocol:Protocol)(using environment:Map[Variable,Protocol]):List[(Action,Protocol)] =
    protocol match
      case Interaction(agentA,agentB,message,sort) => List(Send(agentA,agentB,message,sort) -> Receive(agentB,agentA,message,sort))
      case Send   (agentA, agentB, message, sort) => List(protocol -> End)
      case Receive(agentA, agentB, message, sort) => List(protocol -> End)
      case RecursionCall(variable) =>
        val protocolB = environment(variable)
        val nonRecursiveProtocolB = recursionFree(variable,protocolB)
        for nextActionB -> nextProtocolB <- nextAuxiliary(nonRecursiveProtocolB) yield
          nextActionB -> consumeAction(nextActionB,protocolB)
      case End => Nil
      case Sequence(protocolA,protocolB) =>
        val nextA = nextAuxiliary(protocolA)
        val nextB = nextAuxiliary(protocolB)
        val resultA = for nextActionA -> nextProtocolA <- nextA yield
          nextActionA -> Simplifier(Sequence(nextProtocolA,protocolB))
        val resultB = if accept(protocolA) then nextB else Nil
        resultA ++ resultB
      case Parallel(protocolA,protocolB) =>
        val nextA = nextAuxiliary(protocolA)
        val nextB = nextAuxiliary(protocolB)
        val resultA = for nextActionA -> nextProtocolA <- nextA yield
          nextActionA -> Simplifier(Parallel(nextProtocolA,protocolB))
        val resultB = for nextActionB -> nextProtocolB <- nextB yield
          nextActionB -> Simplifier(Parallel(protocolA,nextProtocolB))
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
        case Interaction(agentA,agentB,message,sort) => protocol
        case Send   (agentA,agentB,message,sort) => protocol
        case Receive(agentA,agentB,message,sort) => protocol
        case RecursionCall(variable) => if variable == recursionVariable then End else protocol
        case End => protocol
        case Sequence(protocolA,protocolB) => Sequence(recursionFreeAuxiliary(protocolA),recursionFreeAuxiliary(protocolB))
        case Parallel(protocolA,protocolB) => Parallel(recursionFreeAuxiliary(protocolA),recursionFreeAuxiliary(protocolB))
        case Choice  (protocolA,protocolB) => Choice  (recursionFreeAuxiliary(protocolA),recursionFreeAuxiliary(protocolB))
        case RecursionFixedPoint(variable,protocolB) => RecursionFixedPoint(variable,recursionFreeAuxiliary(protocolB))
    end recursionFreeAuxiliary
    Simplifier(recursionFreeAuxiliary(protocol)(using recursionVariable))
  end recursionFree

  private def consumeAction(action:Action,protocol:Protocol):Protocol =
    def consumeActionAuxiliary(protocol:Protocol)(using action:Action):Protocol =
      protocol match
        case Interaction(agentA,agentB,message,sort) => if action == protocol then End else protocol
        case Send   (agentA,agentB,message,sort)     => if action == protocol then End else protocol
        case Receive(agentA,agentB,message,sort)     => if action == protocol then End else protocol
        case RecursionCall(variable) => protocol
        case End => protocol
        case RecursionFixedPoint(variable,protocolB) => RecursionFixedPoint(variable,consumeActionAuxiliary(protocolB))
        case Sequence(protocolA,protocolB) =>
          val pA = consumeActionAuxiliary(protocolA)
          if pA == protocolA
          then Sequence(protocolA,consumeActionAuxiliary(protocolB))
          else Sequence(pA,protocolB)
        case Parallel(protocolA,protocolB) => Parallel(consumeActionAuxiliary(protocolA),consumeActionAuxiliary(protocolB))
        case Choice  (protocolA,protocolB) => Choice  (consumeActionAuxiliary(protocolA),consumeActionAuxiliary(protocolB))
    end consumeActionAuxiliary
    Simplifier(consumeActionAuxiliary(protocol)(using action))
  end consumeAction
end SSSemantic