package mpst.operational_semantic.global_semantic

import mpst.operational_semantic.local_semantic.SyncSemantic
import mpst.syntax.Protocol
import mpst.syntax.Protocol.*
import mpst.utilities.Multiset

case class Network(states:Set[State], pending:Multiset[Action]):
  override def toString: String =
    s"${states.mkString("\n")} ${
      if pending.isEmpty then "" else s"\n[pending:$pending]"
    }"
  end toString

object Network:
  def apply(states:Set[State]):Network = Network(states,Multiset())

  private def next(states:Set[State], network:Multiset[Action]): Set[(Action,Set[State],Multiset[Action])] =
    val s = for (state <- states) yield
      val x = reduceState(state, network)
      val y = for (reducedAction, reducedState, reducedNetwork) <- x yield
        (reducedAction, states - state + reducedState, reducedNetwork)
      y
    s.flatten
  end next

  private def reduceState(state:State, network:Multiset[Action]):Set[(Action,State,Multiset[Action])] =
    val reductions = SyncSemantic.reduce(state)
    for nextAction -> nextState <- reductions if allowed(nextAction, network) yield
      (nextAction, nextState, nextAction match
        case Receive(agentA, agentB, message, sort) => network - Send(agentB, agentA, message, sort)
        case Send   (agentA, agentB, message, sort) => network + Send(agentA, agentB, message, sort)
        case protocol => throw new RuntimeException(s"unexpected $protocol found")
      )
  end reduceState

  private def allowed(action: Action, network:Multiset[Action]): Boolean =
    action match
      case Receive(agentA, agentB, message, sort) => network.contains(Send(agentB, agentA, message, sort))
      case Send(_, _, _, _) => true
      case protocol => throw new RuntimeException(s"unexpected $protocol found")
  end allowed
end Network