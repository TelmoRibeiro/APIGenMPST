package mpst.projection

import mpst.syntax.GlobalType
import mpst.syntax.GlobalType._

object Projection:
  private def roles(global: GlobalType): Set[String] =
    global match
      case Interaction(idA, idB, _)       => List(idA, idB).toSet
      case End                            => Set()
      case RecursionFixedPoint(_, global) => roles(global)
      case RecursionCall(_)               => Set()
      case Sequence(globalA, globalB)     => roles(globalA) ++ roles(globalB)
      case Parallel(globalA, globalB)     => roles(globalA) ++ roles(globalB)
      case Choice  (globalA, globalB)     => roles(globalA) ++ roles(globalB)
      case _                              => throw new RuntimeException("Expected: GlobalType\nFound: LocalType")
  end roles

  private def projection(global: GlobalType, role: String): GlobalType =
    global match
      case Interaction(idA, idB, message) =>
       if       role == idA then Send    (idA, idB, message)
       else if  role == idB then Receive (idB, idA, message)
       else     NoAction
      case End => End
      case RecursionFixedPoint(variable, global) => RecursionFixedPoint(variable, projection(global, role))
      case RecursionCall(variable)               => RecursionCall(variable)
      case Sequence(globalA, globalB)            => Sequence(projection(globalA, role), projection(globalB, role))
      case Parallel(globalA, globalB)            => Parallel(projection(globalA, role), projection(globalB, role))
      case Choice  (globalA, globalB)            => Choice  (projection(globalA, role), projection(globalB, role))
      case _                                     => throw new RuntimeException("Expected GlobalType\nFound: LocalType")
  end projection

  def apply(global: GlobalType): Set[GlobalType] = for role <- roles(global) yield GlobalType(projection(global, role))
end Projection