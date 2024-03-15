package mpst.operational_semantic.local_semantic

import mpst.syntax.Protocol
import mpst.syntax.Protocol.*
import mpst.utilities.Simplifier

// Weak Sequencing Free Interleaving - WSFI //
object WSFI_semantic:
  private def accept(local: Protocol): Boolean =
    local match
      // terminal cases //
      case    Send(agentA, agentB, message) => false
      case Receive(agentA, agentB, message) => false
      case RecursionCall(_)                 => false
      case End                              => true
      // recursive cases //
      case RecursionFixedPoint(_, localB) => accept(localB)
      case Sequence(localA, localB) => accept(localA) && accept(localB)
      case Parallel(localA, localB) => accept(localA) && accept(localB)
      case   Choice(localA, localB) => accept(localA) || accept(localB)
      // unexpected cases //
      case global => throw new RuntimeException(s"unexpected global type $global found\n")
  end accept

  def reduce(environment: Map[String,Protocol], local: Protocol)(using conflictingRoles: Set[String] = Set()): List[(Action, State)] =
    local match
      // terminal cases //
      case    Send(agentA, agentB, message) =>
        if   (conflictingRoles contains agentA) || (conflictingRoles contains agentB)
        then Nil
        else List(local -> (environment -> End))
      case Receive(agentA, agentB, message) =>
        if   (conflictingRoles contains agentA) || (conflictingRoles contains agentB)
        then Nil
        else List(local -> (environment -> End))
      case RecursionCall(variable) =>
        val             localB: Protocol = environment(variable)
        val nonRecursiveLocalB: Protocol = removeRecursion(variable, localB)
        for   nextActionB -> (nextEnvironmentB -> nextLocalB) <- reduce(environment, nonRecursiveLocalB)
        yield nextActionB -> (nextEnvironmentB -> removeLabel(nextActionB, localB))
      case End => Nil
      // recursive cases //
      case RecursionFixedPoint(variable, localB) =>
        val recursionCall: (String,Protocol) = variable -> localB
        reduce(environment + recursionCall, localB)
      case Sequence(localA, localB) =>
        val nextListA: List[(Action,State)] = reduce(environment, localA)
        val rolesA: Set[String] = roles(localA)
        val nextListB: List[(Action,State)] = reduce(environment, localB)(using conflictingRoles ++ rolesA)
        val   resultA: List[(Action,State)] =
          for   nextActionA -> (nextEnvironmentA -> nextLocalA) <- nextListA
          yield nextActionA -> (nextEnvironmentA -> Simplifier(Sequence(nextLocalA, localB)))
        val   resultB: List[(Action,State)] =
          for   nextActionB -> (nextEnvironmentB -> nextLocalB) <- nextListB
          yield nextActionB -> (nextEnvironmentB -> Simplifier(Sequence(localA, nextLocalB)))
        val   resultC: List[(Action,State)] =
          if accept(localA) then reduce(environment, localB) else Nil
        resultA ++ resultB ++ resultC
      case Parallel(localA, localB) =>
        val nextListA: List[(Action,State)] = reduce(environment, localA)
        val nextListB: List[(Action,State)] = reduce(environment, localB)
        val   resultA: List[(Action,State)] =
          for   nextActionA -> (nextEnvironmentA -> nextLocalA) <- nextListA
          yield nextActionA -> (nextEnvironmentA -> Simplifier(Parallel(nextLocalA, localB)))
        val   resultB: List[(Action,State)] =
          for   nextActionB -> (nextEnvironmentB -> nextLocalB) <- nextListB
          yield nextActionB -> (nextEnvironmentB -> Simplifier(Parallel(localA, nextLocalB)))
        resultA ++ resultB
      case   Choice(localA, localB) =>
        val nextListA: List[(Action,State)] = reduce(environment, localA)
        val nextListB: List[(Action,State)] = reduce(environment, localB)
        nextListA ++ nextListB
      // unexpected cases //
      case global => throw new RuntimeException(s"unexpected global type $global found\n")
  end reduce
end WSFI_semantic