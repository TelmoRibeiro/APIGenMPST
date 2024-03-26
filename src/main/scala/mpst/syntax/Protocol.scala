package mpst.syntax

import mpst.utilities.Simplifier


enum Protocol:
  // Global Types //
  case Interaction(agentA: String, agentB: String, message: String) // agentA>agentB:message
  case RecursionFixedPoint(variable: String, protocolB: Protocol)   // μX ; G   | μX ; L
  case RecursionCall(variable: String)                              // X
  case Sequence(protocolA: Protocol, protocolB: Protocol)           // GA ;  GB | LA ;  LB
  case Parallel(protocolA: Protocol, protocolB: Protocol)           // GA || GB | LA || LB
  case Choice  (protocolA: Protocol, protocolB: Protocol)           // GA +  GB | LA +  LB
  // Local Types //
  case Send   (agentA: String, agentB: String, message: String)     // agentAagentB!message
  case Receive(agentA: String, agentB: String, message: String)     // agentAagentB?message
  // Internal //
  case End                                                          // 0
end Protocol

object Protocol:
  type Action = Protocol
  type State  = (Map[String,Protocol],Protocol)

  def roles(protocol: Protocol): Set[String] =
    protocol match
      // terminal cases //
      case Interaction(agentA, agentB, _) => (Set() + agentA) + agentB
      case        Send(agentA, agentB, _) => (Set() + agentA) + agentB
      case     Receive(agentA, agentB, _) => (Set() + agentA) + agentB
      case RecursionCall(_)               =>  Set()
      case End                            =>  Set()
      // recursive cases //
      case RecursionFixedPoint(_, protocolB) => roles(protocolB)
      case    Sequence(protocolA, protocolB) => roles(protocolA) ++ roles(protocolB)
      case    Parallel(protocolA, protocolB) => roles(protocolA) ++ roles(protocolB)
      case      Choice(protocolA, protocolB) => roles(protocolA) ++ roles(protocolB)
  end roles

  def headInteraction(global: Protocol)(using role: String): Set[Protocol] =
    global match
      // terminal cases //
      case Interaction(agentA, agentB, message) =>
        if role != agentA && role != agentB
        then Set()
        else Set() + Interaction(agentA, agentB, message)
      case RecursionCall(_) => Set()
      case End              => Set()
      // recursive cases //
      case RecursionFixedPoint(_, globalB) => headInteraction(globalB)
      case Sequence(globalA, globalB) =>
        val headGlobalA: Set[Protocol] = headInteraction(globalA)
        val headGlobalB: Set[Protocol] = headInteraction(globalB)
        if      headGlobalA.nonEmpty then headGlobalA
        else if headGlobalB.nonEmpty then headGlobalB
        else    Set()
      case Parallel(globalA, globalB) => headInteraction(globalA) ++ headInteraction(globalB)
      case   Choice(globalA, globalB) => headInteraction(globalA) ++ headInteraction(globalB)
      // unexpected cases //
      case local => throw new RuntimeException(s"unexpected local type $local found\n")
  end headInteraction

  def interactions(global: Protocol): Set[Protocol] =
    global match
      // terminal cases //
      case Interaction(agentA, agentB, message) => Set() + Interaction(agentA, agentB, message)
      case RecursionCall(_) => Set()
      case End              => Set()
      // recursive cases //
      case RecursionFixedPoint(_, globalB) => interactions(globalB)
      case Sequence(globalA, globalB)      => interactions(globalA) ++ interactions(globalB)
      case Parallel(globalA, globalB)      => interactions(globalA) ++ interactions(globalB)
      case   Choice(globalA, globalB)      => interactions(globalA) ++ interactions(globalB)
      // unexpected cases //
      case local => throw new RuntimeException(s"unexpected local type $local found\n")
  end interactions

  def removeRecursion(recursionVariable: String, local: Protocol): Protocol =
    def removeRecursionAuxiliary(local: Protocol)(using recursionVariable: String): Protocol =
      local match
        // terminal cases //
        case    Send(agentA, agentB, message) => local
        case Receive(agentA, agentB, message) => local
        case RecursionCall(variable) => if variable == recursionVariable then End else local
        case End => local
        // recursive cases //
        case RecursionFixedPoint(variable, localB) => RecursionFixedPoint(variable, removeRecursionAuxiliary(localB))
        case Sequence(localA, localB) => Sequence(removeRecursionAuxiliary(localA), removeRecursionAuxiliary(localB))
        case Parallel(localA, localB) => Parallel(removeRecursionAuxiliary(localA), removeRecursionAuxiliary(localB))
        case   Choice(localA, localB) =>   Choice(removeRecursionAuxiliary(localA), removeRecursionAuxiliary(localB))
        // unexpected cases //
        case global => throw new RuntimeException(s"unexpected global type $global found\n")
    end removeRecursionAuxiliary
    Simplifier(removeRecursionAuxiliary(local)(using recursionVariable))
  end removeRecursion

  def removeLabel(label: Protocol, local: Protocol): Protocol =
    def removeLabelAuxiliary(label: Protocol, local: Protocol): Protocol =
      local match
        // terminal cases //
        case    Send(agentA, agentB, message) => if label ==    Send(agentA, agentB, message) then End else local
        case Receive(agentA, agentB, message) => if label == Receive(agentA, agentB, message) then End else local
        case RecursionCall(variable) => local
        case End                     => local
        // recursive cases //
        case RecursionFixedPoint(variable, localB) => RecursionFixedPoint(variable, removeLabelAuxiliary(label, localB))
        case Sequence(localA, localB) =>
          val newLocalA: Protocol = removeLabelAuxiliary(label, localA)
          if newLocalA == localA
          then Sequence(localA, removeLabelAuxiliary(label, localB))
          else Sequence(newLocalA, localB)
        case Parallel(localA, localB) => Parallel(removeLabelAuxiliary(label, localA), removeLabelAuxiliary(label, localB))
        case   Choice(localA, localB) =>   Choice(removeLabelAuxiliary(label, localA), removeLabelAuxiliary(label, localB))
        // unexpected cases //
        case global => throw new RuntimeException(s"unexpected global type $global found\n")
    end removeLabelAuxiliary
    Simplifier(removeLabelAuxiliary(label, local))
  end removeLabel
end Protocol