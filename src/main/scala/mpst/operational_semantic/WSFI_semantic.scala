package mpst.operational_semantic

import mpst.syntax.Protocol
import mpst.syntax.Protocol.*

object WSFI_semantic:
  def accept(local: Protocol): Boolean =
    local match
      // terminal cases //
      case      Send(_,_,_) => false
      case   Receive(_,_,_) => false
      case RecursionCall(_) => false
      case End => true
      // recursive cases //
      case RecursionFixedPoint(_, localB) => accept(localB)
      case Sequence(localA, localB) => accept(localA) && accept(localB)
      case Parallel(localA, localB) => accept(localA) && accept(localB)
      case   Choice(localA, localB) => accept(localA) || accept(localB)
      // unexpected cases //
      case   Skip => throw RuntimeException("unexpected case of \"Skip\"\n")
      case global => throw RuntimeException(s"unexpected global type $global found\n")
  end accept

  def reduce(environment: Map[String,Protocol], local: Protocol)(using conflictingRoles: Set[String] = Set()): List[(Map[String,Protocol],(Protocol,Protocol))] =
    local match
      // terminal cases //
      case    Send(agentA, agentB, message) =>
        if (conflictingRoles contains agentA) || (conflictingRoles contains agentB) then Nil else List(environment -> (local -> End))
      case Receive(agentA, agentB, message) =>
        if (conflictingRoles contains agentA) || (conflictingRoles contains agentB) then Nil else List(environment -> (local -> End))
      case RecursionCall(variable) =>
        val             localB: Protocol = environment(variable)
        val nonRecursiveLocalB: Protocol = removeRecursion(variable, localB)
        for   nextEnvironmentB -> (nextActionB -> nextLocalB) <- reduce(environment, nonRecursiveLocalB)
        yield nextEnvironmentB -> (nextActionB -> removeLabel(nextActionB, localB))
      case End => Nil
      // recursive cases //
      case RecursionFixedPoint(variable, localB) =>
        val recursionCall: (String,Protocol) = variable -> localB
        reduce(environment + recursionCall, localB)
      case Sequence(localA, localB) =>
        val nextListA: List[(Map[String,Protocol],(Protocol,Protocol))] = reduce(environment, localA)
        val rolesA: Set[String] = roles(localA)
        val nextListB: List[(Map[String,Protocol],(Protocol,Protocol))] = reduce(environment, localB)(using conflictingRoles ++ rolesA)
        val   resultA: List[(Map[String,Protocol],(Protocol,Protocol))] =
          for   nextEnvironmentA -> (nextActionA -> nextLocalA) <- nextListA
          yield nextEnvironmentA -> (nextActionA -> Protocol(Sequence(nextLocalA, localB)))
        val   resultB: List[(Map[String,Protocol],(Protocol,Protocol))] =
          for   nextEnvironmentB -> (nextActionB -> nextLocalB) <- nextListB
          yield nextEnvironmentB -> (nextActionB -> Protocol(Sequence(localA, nextLocalB)))
        val   resultC: List[(Map[String,Protocol],(Protocol,Protocol))] =
          if accept(localA) then nextListB else Nil
        resultA ++ resultB ++ resultC
      case Parallel(localA, localB) =>
        val nextListA: List[(Map[String,Protocol],(Protocol,Protocol))] = reduce(environment, localA)
        val nextListB: List[(Map[String,Protocol],(Protocol,Protocol))] = reduce(environment, localB)
        val   resultA: List[(Map[String,Protocol],(Protocol,Protocol))] =
          for   nextEnvironmentA -> (nextActionA -> nextLocalA) <- nextListA
          yield nextEnvironmentA -> (nextActionA -> Protocol(Parallel(nextLocalA, localB)))
        val   resultB: List[(Map[String,Protocol],(Protocol,Protocol))] =
          for   nextEnvironmentB -> (nextActionB -> nextLocalB) <- nextListB
          yield nextEnvironmentB -> (nextActionB -> Protocol(Parallel(localA, nextLocalB)))
        resultA ++ resultB
      case   Choice(localA, localB) =>
        val nextListA: List[(Map[String,Protocol],(Protocol,Protocol))] = reduce(environment, localA)
        val nextListB: List[(Map[String,Protocol],(Protocol,Protocol))] = reduce(environment, localB)
        nextListA ++ nextListB
      // unexpected cases //
      case   Skip => throw new RuntimeException("unexpected case of \"Skip\"\n")
      case global => throw new RuntimeException(s"unexpected global type $global found\n")
  end reduce
end WSFI_semantic