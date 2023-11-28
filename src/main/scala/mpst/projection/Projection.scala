package mpst.projection

import mpst.syntax.GlobalType
import mpst.syntax.LocalType
import mpst.syntax.GlobalType._
import mpst.syntax.LocalType._

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
  end roles

  private def projection(global: GlobalType, role: String): LocalType =
    global match
      case Interaction(idA, idB, message)        =>
       if       role == idA then LocalSend   (idA, idB, message)
       else if  role == idB then LocalReceive(idB, idA, message)
       else     LocalEnd
      case End                                   => LocalEnd
      case RecursionFixedPoint(variable, global) => LocalRecursionFixedPoint(variable, projection(global, role))
      case RecursionCall(variable)               => LocalRecursionCall(variable)
      case Sequence(globalA, globalB)            => LocalSequence(projection(globalA, role), projection(globalB, role))
      case Parallel(globalA, globalB)            => LocalParallel(projection(globalA, role), projection(globalB, role))
      case Choice  (globalA, globalB)            => LocalChoice  (projection(globalA, role), projection(globalB, role))
  end projection

  def apply(global: GlobalType): Set[LocalType] = for role <- roles(global) yield LocalType(projection(global, role))
end Projection