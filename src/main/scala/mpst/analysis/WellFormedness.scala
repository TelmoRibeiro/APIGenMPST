package mpst.analysis

import mpst.syntax.LocalType
import mpst.syntax.LocalType.*

object WellFormedness:
  private def recursion(variables: Set[String], local: LocalType): Boolean =
    local match
      case LocalSend   (_, _, _) => true
      case LocalReceive(_, _, _) => true
      case LocalEnd              => true
      case LocalRecursionFixedPoint(variable, local) => !variables.contains(variable) && recursion(variables + variable, local)
      case LocalRecursionCall(variable)              =>  variables.contains(variable)
      case LocalSequence(localA, localB)             =>
        localA match
          case LocalRecursionCall(variable) =>
            recursion(variables, localA)
            recursion(variables - variable, localB)
          case _  => recursion(variables, localA) && recursion(variables, localB)
      case LocalParallel(localA, localB)             =>  recursion(variables, localA) && recursion(variables, localB)
      case LocalChoice  (localA, localB)             =>  recursion(variables, localA) && recursion(variables, localB)
  end recursion

  def apply(local: LocalType): Boolean = recursion(Set(), local)
end WellFormedness