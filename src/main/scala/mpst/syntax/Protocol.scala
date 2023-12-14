package mpst.syntax

import scala.annotation.tailrec

/*
    PROTOCOL'S GRAMMAR:
      - GlobalTypes
      - LocalTypes
      - NoAction
*/
enum Protocol:
  // GlobalTypes //
  case Interaction(agentA: String, agentB: String, message: String) extends Protocol // agentA>agentB:message
  case RecursionFixedPoint(variable: String, protocolB: Protocol)   extends Protocol // μX ; G   | μX ; L
  case RecursionCall(variable: String)                              extends Protocol // X
  case Sequence(protocolA: Protocol, protocolB: Protocol)           extends Protocol // GA ;  GB | LA ;  LB
  case Parallel(protocolA: Protocol, protocolB: Protocol)           extends Protocol // GA || GB | LA || LB
  case Choice  (protocolA: Protocol, protocolB: Protocol)           extends Protocol // GA +  GB | LA +  LB
  // LocalTypes //
  case Send   (agentA: String, agentB: String, message: String)     extends Protocol // agentA,agentB!message
  case Receive(agentA: String, agentB: String, message: String)     extends Protocol // agentA,agentB?message
  // NoAction //
  case NoAction                                                     extends Protocol
end Protocol

// SOME FUNCTIONALITY TO PROTOCOL //
object Protocol:
  def roles(global: Protocol): Set[String] =
    global match
      // terminal cases //
      case Interaction(agentA, agentB, _) => (Set() + agentA) + agentB
      case RecursionCall(_)               =>  Set()
      // recursive cases //
      case RecursionFixedPoint(_, globalB) => roles(globalB)
      case Sequence(globalA, globalB)      => roles(globalA) ++ roles(globalB)
      case Parallel(globalA, globalB)      => roles(globalA) ++ roles(globalB)
      case Choice  (globalA, globalB)      => roles(globalA) ++ roles(globalB)
      // unexpected cases //
      case _ => throw new RuntimeException("\nExpected:\tGlobalType\nFound:\t\tLocalType")
  end roles

  def headInteraction(global: Protocol, role: String): Set[Protocol] =
    global match
      // terminal cases //
      case Interaction(agentA, agentB, message) =>
        if   role != agentA && role != agentB  then Set()
        else Set() + Interaction(agentA, agentB, message)
      case RecursionCall(_) => Set()
      // recursive cases //
      case RecursionFixedPoint(_, globalB) => headInteraction(globalB, role)
      case Sequence(globalA, globalB)      =>
        val headGlobalA: Set[Protocol] = headInteraction(globalA, role)
        val headGlobalB: Set[Protocol] = headInteraction(globalB, role)
        if      headGlobalA.nonEmpty then headGlobalA
        else if headGlobalB.nonEmpty then headGlobalB
        else    Set()
      case Parallel(globalA, globalB)      => headInteraction(globalA, role) ++ headInteraction(globalB, role)
      case Choice  (globalA, globalB)      => headInteraction(globalA, role) ++ headInteraction(globalB, role)
      // unexpected cases //
      case _ => throw new RuntimeException("\nExpected:\tGlobalType\nFound:\t\tLocalType")
  end headInteraction

  def interactions(global: Protocol): Set[Protocol] =
    global match
      // terminal cases //
      case Interaction(agentA, agentB, message) => Set() + Interaction(agentA, agentB, message)
      case RecursionCall(_) => Set()
      // recursive cases //
      case RecursionFixedPoint(_, globalB) => interactions(globalB)
      case Sequence(globalA, globalB)      => interactions(globalA) ++ interactions(globalB)
      case Parallel(globalA, globalB)      => interactions(globalA) ++ interactions(globalB)
      case Choice  (globalA, globalB)      => interactions(globalA) ++ interactions(globalB)
      // unexpected case //
      case _ => throw new RuntimeException("\nExpected:\tGlobalType\nFound:\t\tLocalType")
  end interactions

  private def cleanOnce(protocol: Protocol): Protocol =
    protocol match
      // associate ";", "||" and "+" //
      case Sequence(Sequence(protocolA, protocolB), protocolC) => cleanOnce(Sequence(protocolA, Sequence(protocolB, protocolC)))
      case Parallel(Parallel(protocolA, protocolB), protocolC) => cleanOnce(Parallel(protocolA, Parallel(protocolB, protocolC)))
      case Choice  (Choice  (protocolA, protocolB), protocolC) => cleanOnce(Choice(protocolA,   Choice  (protocolB, protocolC)))
      // propagate "NoAction"
      case Sequence(NoAction, protocolB)    => cleanOnce(protocolB)
      case Sequence(protocolA, NoAction)    => cleanOnce(protocolA)
      case Parallel(NoAction, protocolB)    => cleanOnce(protocolB)
      case Parallel(protocolA, NoAction)    => cleanOnce(protocolA)
      case Choice  (NoAction, protocolB)    => cleanOnce(protocolB)
      case Choice  (protocolA, NoAction)    => cleanOnce(protocolA)
      case RecursionFixedPoint(_, NoAction) => NoAction
      // recursive cases //
        // testing //
      case Parallel(RecursionCall(_), protocolB) => cleanOnce(protocolB)
      case Choice  (RecursionCall(_), protocolB) => cleanOnce(protocolB)
        // testing
      case Sequence(protocolA, protocolB) => Sequence(cleanOnce(protocolA), cleanOnce(protocolB))
      case Parallel(protocolA, protocolB) => Parallel(cleanOnce(protocolA), cleanOnce(protocolB))
      case Choice  (protocolA, protocolB) if protocolA == protocolB => cleanOnce(protocolA)
      case Choice  (protocolA, protocolB)           => Choice (cleanOnce(protocolA), cleanOnce(protocolB))
      case RecursionFixedPoint(variable, protocolB) => RecursionFixedPoint(variable, cleanOnce(protocolB))
      // terminal cases //
      case NoAction => NoAction
      case RecursionCall(variable) => RecursionCall(variable)
      case Interaction(agentA, agentB, message) => Interaction(agentA, agentB, message)
      case Send   (agentA, agentB, message)     => Send   (agentA, agentB, message)
      case Receive(agentA, agentB, message)     => Receive(agentA, agentB, message)
  end cleanOnce

  @tailrec
  private def clean(protocol: Protocol): Protocol =
    val cleanProtocol: Protocol = cleanOnce(protocol)
    if  cleanProtocol == protocol then protocol else clean(cleanProtocol)
  end clean

  def apply(protocol: Protocol): Protocol = clean(protocol)
end Protocol