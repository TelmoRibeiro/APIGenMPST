package mpst.operational_semantic

import mpst.syntax.Protocol
import mpst.syntax.Protocol.*
import mpst.utilities.Multiset
import mpst.utilities.Types.*

/* IDEA:
  - SyncNetworkMS is being experimented upon

  @ telmo -
    problem: Environment is global
    causal network initially from choreo
    unrelated: MSyncST - projection erases parallel | Gentle - parallel is never defined
*/

object Network:
  object NetworkMultiset:
    def nextNetwork(locals:Set[Local],pending:Multiset[Action])(using environment:Map[Variable,Protocol]):Set[(Action,Set[Local],Multiset[Action])] =
      val nextNetwork = for local <- locals yield
        val nextEntry = getNextEntry(local,pending)
        for (nextAction,nextLocal,nextPending) <- nextEntry yield
          val nextLocals = locals-local+nextLocal
          (nextAction,nextLocals,nextPending)
      nextNetwork.flatten
    end nextNetwork

    private def getNextEntry(local:Local,pending:Multiset[Action])(using environment:Map[Variable,Protocol]):Set[(Action,Local,Multiset[Action])] =
      for nextAction -> nextLocal <- AsyncSemantic.next(local) if notBlocked(nextAction,pending) yield
        val nextPending = getNextPending(nextAction,pending)
        (nextAction,nextLocal,nextPending)
    end getNextEntry

    private def getNextPending(action:Action,pending:Multiset[Action]):Multiset[Action] =
      action match
        case Send   (agentA,agentB,message,sort) => pending + Send(agentA,agentB,message,sort)
        case Receive(agentA,agentB,message,sort) => pending - Send(agentB,agentA,message,sort)
        case protocol => throw new RuntimeException(s"unexpected protocol found in [$protocol]\n")
    end getNextPending

    private def notBlocked(action:Action,pending:Multiset[Action]):Boolean =
      action match
        case Send   (_, _, _, _) => true
        case Receive(agentA,agentB,message,sort) => pending `contains` Send(agentB, agentA, message, sort)
        case protocol => throw new RuntimeException(s"unexpected protocol found in [$protocol]\n")
    end notBlocked
  end NetworkMultiset

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  object SyncNetworkMultiset:
    // map or 2 seq
    // ms vs cn vs #sessions
    def nextNetwork(locals:Set[Local],pending:Multiset[Action])(using environment:Map[Variable,Protocol]):Set[(Action,Set[Local],Multiset[Action])] =
      val nextNetwork = for local <- locals yield
        val nextEntry = getNextEntry(local,locals,pending)
        for (nextAction,nextLocal,nextPending) <- nextEntry yield
          val nextLocals = locals-local+nextLocal
          (nextAction,nextLocals,nextPending)
      nextNetwork.flatten
    end nextNetwork

    private def getNextEntry(local:Local,locals:Set[Local],pending:Multiset[Action])(using environment:Map[Variable,Protocol]):Set[(Action,Local,Multiset[Action])] =
      for nextAction -> nextLocal <- SyncSemantic.next(local) if notBlocked(nextAction,locals,pending) yield
        val nextPending = getNextPending(nextAction,pending)
        (nextAction,nextLocal,nextPending)
    end getNextEntry

    private def getNextPending(action:Action,pending:Multiset[Action]):Multiset[Action] =
      action match
        case Send   (agentA,agentB,message,sort) => pending + Send(agentA,agentB,message,sort)
        case Receive(agentA,agentB,message,sort) => pending - Send(agentB,agentA,message,sort)
        case protocol => throw new RuntimeException(s"unexpected protocol found in [$protocol]\n")
    end getNextPending

    private def notBlocked(action:Action,locals:Set[Local],pending:Multiset[Action])(using environment:Map[Variable,Protocol]):Boolean =
      // @ telmo - one action dealt at the time & there is an agent who can receive immediately
      def canReceiveImmediately(action:Action,locals:Set[Local]):Boolean =
        action match
          case Send(agentA,agentB,message,sort) =>
            val receives = for local <- locals yield
              for nextAction -> _ <- SyncSemantic.next(local) if nextAction == Receive(agentB,agentA,message,sort) yield nextAction
            val flatReceives = receives.flatten
            // println(s"ACTION: $action | RECEIVES: $flatReceives")
            flatReceives.nonEmpty
          case action => throw new RuntimeException(s"unexpected action found in [$action]\n")
      end canReceiveImmediately
      action match
        case Send   (agentA,agentB,message,sort) => pending.isEmpty && canReceiveImmediately(action,locals)
        case Receive(agentA,agentB,message,sort) => pending `contains` Send(agentB,agentA,message,sort)
        case protocol => throw new RuntimeException(s"unexpected protocol found in [$protocol]\n")
    end notBlocked
  end SyncNetworkMultiset

  private type Queue = Map[(Agent,Agent),List[Message]]
  object NetworkCausal:
    def nextNetwork(locals:Set[Local],pending:Queue)(using environment:Map[Variable,Protocol]):Set[(Action,Set[Local],Queue)] =
      val nextNetwork = for local <- locals yield
        val nextEntry = getNextEntry(local,pending)
        for (nextAction,nextLocal,nextNetwork) <- nextEntry yield
          val nextLocals = locals-local+nextLocal
          (nextAction,nextLocals,nextNetwork)
      nextNetwork.flatten
    end nextNetwork

    private def getNextEntry(local:Local,pending:Queue)(using environment:Map[Variable,Protocol]):Set[(Action,Local,Queue)] =
      for nextAction -> nextLocal <- AsyncSemantic.next(local) if notBlocked(nextAction,pending) yield
        val nextPending = getNextPending(nextAction,pending)
        (nextAction,nextLocal,nextPending)
    end getNextEntry

    private def getNextPending(action:Action,pending:Queue):Queue =
      action match
        case Send(agentA,agentB,message,_) =>
          val entry = (agentA -> agentB) -> (pending.getOrElse(agentA -> agentB,Nil):::List(message))
          pending + entry
        case Receive(agentA,agentB,_,_) =>
          val entry = (agentB -> agentA) -> pending(agentB -> agentA).tail
          pending + entry
        case protocol => throw new RuntimeException(s"unexpected protocol found in [$protocol]\n")
    end getNextPending

    private def notBlocked(action:Action,network:Queue):Boolean =
      action match
        case Send(_,_,_,_) => true
        case Receive(agentA,agentB,message,sort) => (network contains agentB -> agentA) && network(agentB -> agentA).nonEmpty && network(agentB -> agentA).head == message
        case protocol => throw new RuntimeException(s"unexpected protocol found in [$protocol]\n")
    end notBlocked
  end NetworkCausal
end Network