package mpst.analysis

import mpst.operational_semantic.local_semantic.SSSemantic
import mpst.projection.Projection
import mpst.projection.Projection.*
import mpst.syntax.Protocol
import mpst.syntax.Protocol.*
import mpst.utilities.Environment
import mpst.utilities.Types.*


// @ telmo - ADD BETTER ERROR HANDLING //
object WellBranched:
  private def isWellBranched(global:Protocol)(using environment:Map[String,Protocol]):Boolean =
    global match
      case Interaction(_,_,_,_) => true
      case RecursionCall(_)     => true
      case End => true
      case Sequence(globalA,globalB) => isWellBranched(globalA) && isWellBranched(globalB)
      case Parallel(globalA,globalB) => isWellBranched(globalA) && isWellBranched(globalB)
      case Choice  (globalA,globalB) => checkWellBranched(globalA,globalB) && isWellBranched(globalA) && isWellBranched(globalB)
      case RecursionFixedPoint(_,globalB) => isWellBranched(globalB)
      case local => throw new RuntimeException(s"unexpected local type found in $local")
  end isWellBranched

  private def checkWellBranched(globalA:Protocol, globalB:Protocol)(using environment:Map[Variable,Protocol]):Boolean =
    // @ telmo - nextActionsX = actions produced in reducing globalX
    val nextActionsA = for actionsA -> stateA <- SSSemantic.reduceState(environment -> globalA) yield actionsA
    val nextActionsB = for actionsB -> stateB <- SSSemantic.reduceState(environment -> globalB) yield actionsB
    val selectors = for case Send(agentA,_,_,_) <- nextActionsA ++ nextActionsB yield agentA
    if selectors.isEmpty  then throw new RuntimeException(s"no selector in [$globalA] and [$globalB]\n")
    if selectors.size > 1 then throw new RuntimeException(s"multiple selectors in [$globalA] and [$globalB]\n")
    val selector = selectors.head
    // @ telmo - may need to pay attention to the sort
    // @ telmo - sendingX = receiver and messages from actions being send by selector in actionX
    val sendingActionsA = for case Send(selector,agentB,message,_) <- nextActionsA yield agentB -> message
    val sendingActionsB = for case Send(selector,agentB,message,_) <- nextActionsB yield agentB -> message
    // @ telmo - receivingX = receiver and message from action being received by selector in actionX
    val receivingActionsA = receivingActions(globalA,sendingActionsA)
    val receivingActionsB = receivingActions(globalB,sendingActionsB)
    // @ telmo - all global sends have a local leading receive
    for sendingActionA <- sendingActionsA if !receivingActionsA.contains(sendingActionA) yield
      throw new RuntimeException(s"$selector>${sendingActionA._1}${sendingActionA._2} in [$globalA] cannot be received\n")
    for sendingActionB <- sendingActionsB if !receivingActionsB.contains(sendingActionB) yield
      throw new RuntimeException(s"$selector>${sendingActionB._1}${sendingActionB._2} in [$globalB] cannot be received\n")
    // @ telmo - receiving agents must be the same in both branches but the messages must be different
    // @ telmo - each receiving "agentB:mA" @ globalA have a matching "agentB:mB" @ globalB where mA != mB
    // @ telmo - fails because mA == mB
    for agentB -> messageToAgentB <- receivingActionsA yield
      if receivingActionsB.contains(agentB -> messageToAgentB)
      then throw new RuntimeException(s"$selector>$agentB$messageToAgentB is ambiguous in [$globalA] and [$globalB]\n")
    // @ telmo - fails because there is no "agentB:mB" @ globalB
    for agentB -> messageToAgentB <- receivingActionsA yield
      if !receivingActionsB.map(_._1).contains(agentB)
      then throw new RuntimeException(s"$selector>$agentB$messageToAgentB from [$globalA] cannot be matched in [$globalB]\n")
    // @ telmo - fails because there is no "agentB:mA" @ globalA
    for agentB -> messageToAgentB <- receivingActionsB yield
      if !receivingActionsA.map(_._1).contains(agentB)
      then throw new RuntimeException(s"$selector>$agentB$messageToAgentB from [$globalB] cannot be matched in [$globalA]\n")
    true
  end checkWellBranched

  private def receivingActions(global:Protocol, sendingActions:Set[(Agent,Message)])(using environment:Map[Variable,Protocol]):Set[(Agent,Message)] =
    for agent -> local <- Projection.projectionWithAgent(global)
        case Receive(agent,selector,message,_) -> _ <- SSSemantic.reduceState(environment -> local) if sendingActions contains agent -> message
    yield agent -> message
  end receivingActions

  // @ telmo - ASSUMING THE ORIGINAL PROTOCOL //
  def apply(global:Protocol):Boolean =
    val environment = Environment.getEnvironment(global)(using Map())
    isWellBranched(global)(using environment)
end WellBranched