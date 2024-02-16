package mpst.operational_semantic

import mpst.syntax.Protocol
import mpst.syntax.Protocol.*

/*
  SSFI Semantics:
  - Strict/Strong Sequentialization
  - Free Interleaving

  TO DEBATE:
    - why we need End? check accept!
      Normalization for local types would invalidate the need for end
    - Send/Receive (Out/In) -
      Receive needed as a middle step of Interaction(Send)
      but we do not stipulate semantics for global types but instead for local types
    - Recursion behaving as expected?
*/

object SSFI_semantic:
  def accept(local: Protocol): Boolean =
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
      case Skip   => throw new RuntimeException("unexpected case of \"Skip\"\n")
      case global => throw new RuntimeException(s"unexpected global type $global found\n")
  end accept

  def reduce(environment: Map[String, Protocol], local: Protocol): List[(Map[String, Protocol], (Protocol, Protocol))] =
    local match
      // terminal cases //
      case Send   (agentA, agentB, message) => List(environment -> (Send   (agentA, agentB, message) -> End))
      case Receive(agentA, agentB, message) => List(environment -> (Receive(agentA, agentB, message) -> End))
      case RecursionCall(variable) =>
        val localB: Protocol = environment(variable)
        val nonRecursiveLocalB: Protocol = removeRecursion(variable, localB)
        for   nextEnvironmentB -> (nextLabelB -> nextLocalB) <- reduce(environment, nonRecursiveLocalB)
        yield environment -> (nextLabelB -> removeLabel(nextLabelB, localB))
      case End => Nil
      // recursive cases //
      case Sequence(localA, localB) =>
        val nextListA: List[(Map[String,Protocol],(Protocol,Protocol))] = reduce(environment, localA)
        val nextListB: List[(Map[String,Protocol],(Protocol,Protocol))] = reduce(environment, localB)
        val resultA:   List[(Map[String,Protocol],(Protocol,Protocol))] =
          for   nextEnvironmentA -> (nextLabelA -> nextLocalA) <- nextListA
          yield nextEnvironmentA -> (nextLabelA -> Protocol(Sequence(nextLocalA, localB)))
        val resultB:   List[(Map[String,Protocol],(Protocol,Protocol))] =
          if   accept(localA)
          then reduce(environment, localB)
          else Nil
        resultA ++ resultB
      case Parallel(localA, localB) =>
        val nextListA: List[(Map[String,Protocol],(Protocol,Protocol))] = reduce(environment, localA)
        val nextListB: List[(Map[String,Protocol],(Protocol,Protocol))] = reduce(environment, localB)
        val resultA:   List[(Map[String,Protocol],(Protocol,Protocol))] =
          for   nextEnvironmentA -> (nextLabelA -> nextLocalA) <- nextListA
          yield nextEnvironmentA -> (nextLabelA -> Protocol(Parallel(nextLocalA, localB)))
        val resultB:   List[(Map[String,Protocol],(Protocol,Protocol))] =
          for   nextEnvironmentB -> (nextActionB -> nextLocalB) <- nextListB
          yield nextEnvironmentB -> (nextActionB -> Protocol(Parallel(localA, nextLocalB)))
        resultA ++ resultB
      case   Choice(localA, localB) =>
        val nextListA = reduce(environment, localA)
        val nextListB = reduce(environment, localB)
        nextListA ++ nextListB
      case RecursionFixedPoint(variable, localB) =>
        val recursionMap: (String, Protocol) = variable -> localB
        reduce(environment + recursionMap, localB)
      // unexpected cases //
      case Skip   => throw new RuntimeException("unexpected case of \"Skip\"\n")
      case global => throw new RuntimeException(s"unexpected global type $global found\n")
  end reduce
end SSFI_semantic