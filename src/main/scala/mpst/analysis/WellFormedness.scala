package mpst.analysis

import mpst.syntax.GlobalType
import mpst.syntax.GlobalType._

object WellFormedness:
  private def recursion(variables: Set[String], global: GlobalType): Boolean =
    global match
      case Send   (_, _, _) => true
      case Receive(_, _, _) => true
      case End              => true
      case RecursionFixedPoint(variable, global) => !variables.contains(variable) && recursion(variables + variable, global)
      case RecursionCall(variable)               =>  variables.contains(variable)
      case Sequence(globalA, globalB)            =>
        globalA match
          case RecursionCall(variable) =>
            recursion(variables, globalA)
            recursion(variables - variable, globalB)
          case _                       =>
            recursion(variables, globalA) && recursion(variables, globalB)
      case Parallel(globalA, globalB) =>  recursion(variables, globalA) && recursion(variables, globalB)
      case Choice  (globalA, globalB) =>  recursion(variables, globalA) && recursion(variables, globalB)
      case _ => throw new RuntimeException("Expected: LocalType\nFound: GlobalType")
  end recursion

  def apply(local: GlobalType): Boolean = recursion(Set(), local)
end WellFormedness