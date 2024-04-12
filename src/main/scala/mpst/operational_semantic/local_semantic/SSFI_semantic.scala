package mpst.operational_semantic.local_semantic

import mpst.syntax.Protocol
import mpst.syntax.Protocol.*
import mpst.utilities.Simplifier

// Strict Sequencing Free Interleaving - SSFI //
object SSFI_semantic:
  def accept(local: Protocol): Boolean =
    local match
      // terminal cases //
      case    Send(agentA, agentB, message, sort) => false
      case Receive(agentA, agentB, message, sort) => false
      case RecursionCall(_) => false
      case End              => true
      // recursive cases //
      case RecursionFixedPoint(_, localB) => accept(localB)
      case Sequence(localA, localB) => accept(localA) && accept(localB)
      case Parallel(localA, localB) => accept(localA) && accept(localB)
      case   Choice(localA, localB) => accept(localA) || accept(localB)
      // unexpected cases //
      case global => throw new RuntimeException(s"unexpected global type $global found\n")
  end accept

  def reduce(environment: Map[String,Protocol], local: Protocol): Set[(Action, State)] =
    local match
      // terminal cases //
      case Send   (agentA, agentB, message, sort) => Set(local -> (environment -> End))
      case Receive(agentA, agentB, message, sort) => Set(local -> (environment -> End))
      case RecursionCall(variable) =>
        val             localB: Protocol = environment(variable)
        val nonRecursiveLocalB: Protocol = removeRecursion(variable, localB)
        for   nextActionB -> (nextEnvironmentB -> nextLocalB) <- reduce(environment, nonRecursiveLocalB)
        yield nextActionB -> (nextEnvironmentB -> removeLabel(nextActionB, localB))
      case End => Set()
      // recursive cases //
      case RecursionFixedPoint(variable, localB) =>
        val recursionCall: (String,Protocol) = variable -> localB
        reduce(environment + recursionCall, localB)
      case Sequence(localA, localB) =>
        val nextListA = reduce(environment, localA)
        val nextListB = reduce(environment, localB)
        val resultA =
          for   nextActionA -> (nextEnvironmentA -> nextLocalA) <- nextListA
          yield nextActionA -> (nextEnvironmentA -> Simplifier(Sequence(nextLocalA, localB)))
        val resultB =
          if accept(localA) then nextListB else Nil
        resultA ++ resultB
      case Parallel(localA, localB) =>
        val nextListA = reduce(environment, localA)
        val nextListB = reduce(environment, localB)
        val   resultA =
          for   nextActionA -> (nextEnvironmentA -> nextLocalA) <- nextListA
          yield nextActionA -> (nextEnvironmentA -> Simplifier(Parallel(nextLocalA, localB)))
        val   resultB =
          for   nextActionB -> (nextEnvironmentB -> nextLocalB) <- nextListB
          yield nextActionB -> (nextEnvironmentB -> Simplifier(Parallel(localA, nextLocalB)))
        resultA ++ resultB
      case   Choice(localA, localB) =>
        val nextListA = reduce(environment, localA)
        val nextListB = reduce(environment, localB)
        nextListA ++ nextListB
      // unexpected cases //
      case global => throw new RuntimeException(s"unexpected global type $global found\n")
  end reduce
end SSFI_semantic