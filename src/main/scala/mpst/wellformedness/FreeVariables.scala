package mpst.wellformedness

import mpst.syntax.Protocol
import mpst.syntax.Protocol._

object FreeVariables:
  private def freeVariables(variables: Set[String], global: Protocol): Boolean =
    global match
      // terminal cases //
      case Interaction(_, _, _) => true
      case End                  => true
      case RecursionCall(variable) => variables.contains(variable)
      // recursive cases //
      case RecursionFixedPoint(variable, globalB) => freeVariables(variables + variable, globalB)
      case Sequence(globalA, globalB)  =>
        globalA match
          case RecursionCall(variable) => freeVariables(variables, globalA) && freeVariables(variables - variable, globalB)
          case _                       => freeVariables(variables, globalA) && freeVariables(variables, globalB)
      case Parallel(globalA, globalB)  => freeVariables(Set(), globalA) && freeVariables(Set(), globalB)
      case Choice  (globalA, globalB)  => freeVariables(variables, globalA) && freeVariables(variables, globalB)
      // unexpected cases //
      case _ => throw new RuntimeException("\nExpected:\tGlobalType\nFound:\t\tLocalType")
  end freeVariables

  def apply(global: Protocol): Boolean = freeVariables(Set(), global)
end FreeVariables