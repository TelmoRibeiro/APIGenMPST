package mpst.semantics

import mpst.syntax.Protocol
import mpst.syntax.Protocol._

/*  Problems:
    - Send and Receive is non-trivial in "Choreo"
    - since I am using fixed points instead of Kleene closure, next MUST keep GAMMA
    - is Recursion* behaving like expected?
*/

object Semantics:
  // TO DO //
  private def accept(local: Protocol): Boolean =
    local match
      // terminal cases //
      case    Send(agentA, agentB, message) => false
      case Receive(agentA, agentB, message) => false
      case RecursionCall(_)                 => false;
      // recursive cases //
      case Sequence(localA, localB) => accept(localA) && accept(localB)
      case Parallel(localA, localB) => accept(localA) && accept(localB)
      case   Choice(localA, localB) => accept(localA) || accept(localB)
      case RecursionFixedPoint(_, localB) => accept(localB)
      // unexpected cases //
      case _ => throw new RuntimeException("\nExpected:\tLocalType\nFound:\t\tGlobalType")
  end accept

  // using keyword would be helpful here, maybe some previous functions can benefit from it as well //
  private def reduce(environment: Map[String, Protocol], conflictingRoles: Set[String], local: Protocol): Set[(Map[String, Protocol], Protocol)] =
    local match
      case Send(agentA, agentB, message) =>
        if   conflictingRoles contains agentA
        then Set()
        else Set() + ((environment, Skip)) // placeholder

      case Receive(agentA, agentB, message) =>
        if   conflictingRoles contains agentB
        then Set()
        else Set() + ((environment, Skip)) // placeholder

      case Sequence(localA, localB) =>
        val nextListA = reduce(environment, conflictingRoles, localA)
        val rolesA    = Protocol.roles(localA)
        val nextListB = reduce(environment, conflictingRoles++rolesA, localB) // allows the weak ;
        val resultA =
          for   (nextEnvironmentA, nextLocalA) <- nextListA
          yield (nextEnvironmentA, Sequence(nextLocalA, localB))
        // allows the weak ; //
        val resultB =
          for   (nextEnvironmentB, nextLocalB) <- nextListB
          yield (nextEnvironmentB, Sequence(localA, nextLocalB))
        // allows for L = End ; LB to reduce into LB //
        val resultC =
          if   accept(localA)
          then reduce(environment, conflictingRoles, localB)
          else Nil
        resultA ++ resultB ++ resultC

      case Parallel(localA, localB) =>
        val nextListA = reduce(environment, conflictingRoles, localA)
        val nextListB = reduce(environment, conflictingRoles, localB)
        val resultA =
          for   (nextEnvironmentA, nextLocalA) <- nextListA
          yield (nextEnvironmentA, Parallel(nextLocalA, localB))
        val resultB =
          for   (nextEnvironmentB, nextLocalB) <- nextListB
          yield (nextEnvironmentB, Parallel(localA, nextLocalB))
        resultA ++ resultB

      case   Choice(localA, localB) =>
        val nextListA = reduce(environment, conflictingRoles, localA)
        val nextListB = reduce(environment, conflictingRoles, localB)
        nextListA ++ nextListB

      case RecursionCall(variable) =>
        val localB = environment(variable)
        Set() + ((environment, localB))

      case RecursionFixedPoint(variable, localB) =>
        val newEntry = variable -> localB
        Set() + ((environment + newEntry, localB))

      case _ => throw new RuntimeException("\nExpected:\tLocalType\nFound:\t\tGlobalType")
  end reduce
end Semantics