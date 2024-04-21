package mpst.operational_semantic

import mpst.syntax.Protocol
import mpst.syntax.Protocol.*
import mpst.utilities.Multiset
import mpst.utilities.Types.*

type Queue = Map[(Agent,Agent),List[Message]]

object Network:
  object NetworkMultiset:
    def next(locals:Set[Protocol],network:Multiset[Action])(using environment:Map[Variable,Protocol]):Set[(Action,Set[Protocol],Multiset[Action])] =
      val x = for local <- locals yield
        val y = evolveLocal(local,network)
        val z = for (nextAction,nextLocal,nextNetwork) <- y yield
          (nextAction,locals-local+nextLocal,nextNetwork)
        z
      x.flatten
    end next

    private def evolveLocal(local:Protocol,network:Multiset[Action])(using environment:Map[Variable,Protocol]):Set[(Action,Protocol,Multiset[Action])] =
      for nextAction -> nextLocal <- SSSemantic.next(local) if allowed(nextAction,network) yield
        (nextAction,nextLocal,nextAction match
          case Receive(agentA,agentB,message,sort) => network - Send(agentB,agentA,message,sort)
          case Send   (agentA,agentB,message,sort) => network + Send(agentA,agentB,message,sort)
          case protocol => throw new RuntimeException(s"unexpected protocol found in [$protocol]\n")
        )
    end evolveLocal

    private def allowed(action:Action,network:Multiset[Action]):Boolean =
      action match
        case Receive(agentA,agentB,message,sort) => network.contains(Send(agentB,agentA,message,sort))
        case Send   (_,_,_,_) => true
        case protocol => throw new RuntimeException(s"unexpected protocol found in [$protocol]\n")
    end allowed
  end NetworkMultiset
end Network

/*
case class NetworkCausal(states:Set[State],pending:Queue):
  object NetworkCausal:
    def next(locals:Set[Protocol],network:Queue)(using environment:Map[Variable,Protocol]):Set[(Action,Set[Protocol],Queue)] =
      val x = for local <- locals yield // get each local from projections
        val y = evolveLocal(local,network)(using environment) // get all evolutions
        val z = for (nextAction,nextLocal,nextNetwork) <- y yield
          (nextAction, locals-local+nextLocal, nextNetwork)
        z
      x.flatten
  end NetworkCausal

  private def evolveLocal(local:Protocol,network: Queue)(using environment:Map[Variable,Protocol]):Set[(Action,Protocol,Queue)] =
    for nextAction -> nextState <- SSSemantic.nextLocal(local) if allowed(nextAction,network) yield
      val nextEnvironment -> nextLocal = nextState
      (nextAction, nextLocal, nextAction match
        case Receive(agentA,agentB,message,_) => network + (agentB -> agentA -> network(agentB -> agentA).tail) // take out the tail
        // case Send   (agentA,agentB,message,_) => network + (agentA -> agentB -> network.getOrElse(agentA -> agentB,Nil):::List(message))
      )
  end evolveLocal

  private def allowed(action:Action,network:Queue):Boolean =
    action match
      case Receive(agentA,agentB,message,_) => network.contains(agentB -> agentA) && network(agentB -> agentA).nonEmpty && network(agentB -> agentA).head == message
      case Send   (_,_,_,_) => true
  end allowed
end NetworkCausal
*/

/*
package mpst.operational_semantic.global_semantic

import mpst.operational_semantic.local_semantic.SSSemantic
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
  // extend more examples
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
    val reductions = SSSemantic.reduce(state)
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
 */