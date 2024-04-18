package mpst.analysis

import mpst.syntax.Protocol
import mpst.syntax.Protocol.*
import mpst.utilities.Types.*

/* IDEA:
  - check well-formedness on *recursion*

   - check loop:
    - for agent <- agents(global)
      - val x = Send(agent,agent,Nil)
      - SSSemantic.agrees(global, ell) match
        - case Some(globalB) if global != globalB => global partially evaluates to globalB for agent and they are differ
        - case _ => true
*/

object DependentlyGuarded:
  /* testing stuff
  def agrees(protocol:Protocol,action:Action):Option[Protocol] =
    protocol match
      case Interaction(agentA,agentB,_,_) =>
        val actionAgents = getAgents(action)
        if (actionAgents contains agentA) || (actionAgents contains agentB) then None
        else Some(protocol)
      case Send(agentA,_,_,_) =>
        val actionAgents = getAgents(action)
        if  actionAgents contains agentA then None
        else Some(protocol)
      case Receive(agentA,_,_,_) =>
        val actionAgents = getAgents(action)
        if  actionAgents contains agentA then None
        else Some(protocol)
      case RecursionCall(_) => ??? // check
      case End => Some(protocol)
      case Sequence(protocolA,protocolB) =>
        val xsA = agrees(protocolA,action)
        val xsB = agrees(protocolB,action)
        if xsA.isEmpty || xsB.isEmpty then None
        else for xA <- xsA ; xB <- xsB yield Sequence(xA,xB)
      case Parallel(protocolA,protocolB) =>
        val xsA = agrees(protocolA,action)
        val xsB = agrees(protocolB,action)
        if xsA.isEmpty || xsB.isEmpty then None
        else for xA <- xsA ; xB <- xsB yield Parallel(xA,xB)
      case Choice(protocolA,protocolB) =>
        val xsA = agrees(protocolA,action)
        val xsB = agrees(protocolB,action)
        if xsA.isEmpty then xsB
        else if xsB.isEmpty then xsB
        else for xA <- xsA ; xB <- xsB yield Choice(xA,xB)
      case RecursionFixedPoint(variable,protocolB) =>
        // check
        val xB = agrees(protocolB,action)
        if xB contains protocolB then Some(protocolB)
        else Some(End)
  end agrees
  */
end DependentlyGuarded