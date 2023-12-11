package mpst.wellformedness

import mpst.syntax.GlobalType
import mpst.syntax.GlobalType._

object FreeVariables:
  private def freeVariables(variables: Set[String], local: GlobalType): Boolean =
    local match
      // terminal cases //
      case Send   (_, _, _) => true
      case Receive(_, _, _) => true
      case End              => true
      case RecursionCall(variable) => variables.contains(variable)
      // recursive cases //
      case RecursionFixedPoint(variable, localB) => freeVariables(variables + variable, localB)
      case Sequence(localA, localB) =>
        localA match
          case RecursionCall(variable) => freeVariables(variables, localA) && freeVariables(variables - variable, localB)
          case _                       => freeVariables(variables, localA) && freeVariables(variables, localB)
      case Parallel(localA, localB) => freeVariables(Set(), localA) && freeVariables(Set(), localB)                       // preventing parallel race conditions
      case Choice  (localA, localB) => freeVariables(variables, localA) && freeVariables(variables, localB)
      // unexpected case //
      case _ => throw new RuntimeException("Expected: LocalType\nFound: GlobalType")
  end freeVariables

  def apply(local: GlobalType): Boolean = freeVariables(Set(), local)
end FreeVariables