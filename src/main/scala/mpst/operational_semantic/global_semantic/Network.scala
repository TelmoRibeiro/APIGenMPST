package mpst.operational_semantic.global_semantic

import mpst.operational_semantic.local_semantic.SSFI_semantic
import mpst.syntax.Protocol
import mpst.syntax.Protocol.*
import mpst.utilities.Multiset

case class Network(states:Set[State], pending:Multiset[Action]):
  override def toString: String =
    s"${states.mkString("\n")} ${
      if pending.isEmpty then "" else s"\n[pending:$pending]"
    }"

object Network:
  def apply(states:Set[State]):Network = Network(states,Multiset())

  def next(l: Network): Set[(Action,Network)] =
    Network.next(l.states, l.pending).map(p=>(p._1, Network(p._2, p._3)))

  private def next(states:Set[State], network:Multiset[Action]): Set[(Action,Set[State],Multiset[Action])] =
    val s = for (state <- states) yield
      val es = evolveLocal(state, network)
      val newES = for (actionES, stateES, networkES) <- es yield
        (actionES, states - state + stateES, networkES)
      newES
    s.flatten
  end next

  private def evolveLocal(state:State, network:Multiset[Action]):Set[(Action,State,Multiset[Action])] =
    val (environment, local) = state
    val reductions = SSFI_semantic.reduce(environment, local)
    for (nextAction, (nextEnvironment, nextLocal)) <- reductions if allowed(nextAction, network) yield
      val nextState = nextEnvironment -> nextLocal
      (nextAction, nextState, nextAction match
        case Receive(agentA, agentB, message, sort) => network - Send(agentB, agentA, message, sort)
        case Send   (agentA, agentB, message, sort) => network + Send(agentA, agentB, message, sort)
        case protocol => throw new RuntimeException(s"unexpected $protocol found")
      )
  end evolveLocal

  private def allowed(action: Action, network:Multiset[Action]): Boolean =
    action match
      case Receive(agentA, agentB, message, sort) => network.contains(Send(agentB, agentA, message, sort))
      case Send(_, _, _, _) => true
      case protocol => throw new RuntimeException(s"unexpected $protocol found")
  end allowed
end Network