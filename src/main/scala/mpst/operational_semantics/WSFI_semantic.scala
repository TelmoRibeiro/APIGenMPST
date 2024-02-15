package mpst.operational_semantics

import mpst.syntax.Protocol
import mpst.syntax.Protocol.*

/*
  WSFI Semantics:
  - Weak Sequentialization
  - Free Interleaving

  - relax this sequentialization
*/

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
      case Skip => throw new RuntimeException("unexpected case of \"Skip\"\n")
      case _    => throw new RuntimeException("unexpected global type found\n")
  end accept

  private def reduce(environment: Map[String, Protocol], conflictingRoles: Set[String], local: Protocol): List[(Map[String, Protocol], (Protocol, Protocol))] =
    local match
      // terminal cases //
      case Send   (agentA, agentB, message) => 
        if   (conflictingRoles contains agentA) || (conflictingRoles contains agentB)
        then Nil
        else List(environment -> (Send   (agentA, agentB, message) -> End))
      case Receive(agentA, agentB, message) => 
        if   (conflictingRoles contains agentA) || (conflictingRoles contains agentB) 
        then Nil
        else List(environment -> (Receive(agentA, agentB, message) -> End))
      case RecursionCall(variable) =>
        val localB: Protocol = environment(variable)
        val nonRecursiveLocalB: Protocol = removeRecursion(variable, localB)
        for   nextEnvironmentB -> (nextLabelB -> nextLocalB) <- reduce(environment, conflictingRoles, nonRecursiveLocalB)
        yield environment -> (nextLabelB -> removeLabel(nextLabelB, localB))
      case End => Nil
      // recursive cases //
      case Sequence(localA, localB) =>
        val nextListA: List[(Map[String,Protocol],(Protocol,Protocol))] = reduce(environment, conflictingRoles, localA)
        val rolesA: Set[String] = roles(localA)
        val nextListB: List[(Map[String,Protocol],(Protocol,Protocol))] = reduce(environment, conflictingRoles ++ rolesA, localB)
        val resultA:   List[(Map[String,Protocol],(Protocol,Protocol))] =
          for   nextEnvironmentA -> (nextLabelA -> nextLocalA) <- nextListA
          yield nextEnvironmentA -> (nextLabelA -> Protocol(Sequence(nextLocalA, localB)))
        val resultB:   List[(Map[String,Protocol],(Protocol,Protocol))] =
          for   nextEnvironmentB -> (nextLabelB -> nextLocalB) <- nextListB
          yield nextEnvironmentB -> (nextLabelB -> Protocol(Sequence(localA, nextLocalB)))
        val resultC:   List[(Map[String,Protocol],(Protocol,Protocol))] =
          if   accept(localA)
          then reduce(environment, conflictingRoles, localB)
          else Nil
        resultA ++ resultB ++ resultC
      case Parallel(localA, localB) =>
        val nextListA: List[(Map[String,Protocol],(Protocol,Protocol))] = reduce(environment, conflictingRoles, localA)
        val nextListB: List[(Map[String,Protocol],(Protocol,Protocol))] = reduce(environment, conflictingRoles, localB)
        val resultA:   List[(Map[String,Protocol],(Protocol,Protocol))] =
          for   nextEnvironmentA -> (nextLabelA -> nextLocalA) <- nextListA
          yield nextEnvironmentA -> (nextLabelA -> Protocol(Parallel(nextLocalA, localB)))
        val resultB:   List[(Map[String,Protocol],(Protocol,Protocol))] =
          for   nextEnvironmentB -> (nextActionB -> nextLocalB) <- nextListB
          yield nextEnvironmentB -> (nextActionB -> Protocol(Parallel(localA, nextLocalB)))
        resultA ++ resultB
      case   Choice(localA, localB) =>
        val nextListA = reduce(environment, conflictingRoles, localA)
        val nextListB = reduce(environment, conflictingRoles, localB)
        nextListA ++ nextListB
      case RecursionFixedPoint(variable, localB) =>
        val recursionMap: (String, Protocol) = variable -> localB
        reduce(environment + recursionMap, conflictingRoles, localB)
      // unexpected cases //
      case Skip   => throw new RuntimeException("unexpected case of \"Skip\"\n")
      case global => throw new RuntimeException(s"unexpected global type $global found\n")
  end reduce
end WSFI_semantic