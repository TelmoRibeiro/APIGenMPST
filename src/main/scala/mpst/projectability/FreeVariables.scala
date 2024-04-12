package mpst.projectability

import mpst.syntax.Protocol
import mpst.syntax.Protocol._

object FreeVariables:
  private def freeVariables(variables: Set[String], global: Protocol): Boolean =
    global match
      // terminal cases //
      case Interaction  (_, _, _, _)  => true
      case RecursionCall(variable) => variables.contains(variable)
      case End                     => true
      // recursive cases //
      case RecursionFixedPoint(variable, globalB) => freeVariables(variables + variable, globalB)
      case Sequence(globalA, globalB)  =>
        globalA match
          case RecursionCall(variable) => freeVariables(variables, globalA) && freeVariables(variables - variable, globalB)
          case _                       => freeVariables(variables, globalA) && freeVariables(variables, globalB)
      case Parallel(globalA, globalB)  => freeVariables(Set(), globalA)     && freeVariables(Set(), globalB)
      case Choice  (globalA, globalB)  => freeVariables(variables, globalA) && freeVariables(variables, globalB)
      // unexpected cases //
      case local => throw new RuntimeException(s"unexpected local type $local found\n")
  end freeVariables

  def apply(global: Protocol): Boolean = freeVariables(Set(), global)
end FreeVariables