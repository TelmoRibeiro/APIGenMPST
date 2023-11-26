package mpst.projection

import scala.annotation.tailrec

// this is looking extremely cumbersome, there must be a way to simplify this //
import mpst.syntax.GlobalType
import mpst.syntax.LocalType
import mpst.syntax.GlobalType.*
import mpst.syntax.LocalType.*

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

  // why isn't it needed an associate parallel //
  private def cleanOnce(local: LocalType): LocalType =
    local match
      // associate ;
      case LocalSequence(LocalSequence(localA, localB), localC) => cleanOnce(LocalSequence(localA, LocalSequence(localB, localC)))
      // associate +
      case LocalChoice(LocalChoice(localA, localB), localC)     => cleanOnce(LocalChoice(localA, LocalChoice(localB, localC)))
      // remove end
      case LocalSequence(LocalEnd, local)                       => cleanOnce(local)
      case LocalSequence(local, LocalEnd)                       => cleanOnce(local)
      case LocalParallel(LocalEnd, local)                       => cleanOnce(local)
      case LocalParallel(local, LocalEnd)                       => cleanOnce(local)
      case LocalRecursionFixedPoint(_, LocalEnd)                => LocalEnd
      // recursive
      case LocalSequence(localA, localB)                        => LocalSequence(cleanOnce(localA), cleanOnce(localB))
      case LocalParallel(localA, localB)                        => LocalParallel(cleanOnce(localA), cleanOnce(localB))
      case LocalChoice  (localA, localB) if localA == localB    => cleanOnce(localA)
      case LocalChoice  (localA, localB)                        => LocalChoice  (cleanOnce(localA), cleanOnce(localB))
      case LocalRecursionFixedPoint(variable, local)            => LocalRecursionFixedPoint(variable, cleanOnce(local))
      // terminals
      case LocalEnd                                             => LocalEnd
      case LocalSend   (idA, idB, message)                      => LocalSend   (idA, idB, message)
      case LocalReceive(idA, idB, message)                      => LocalReceive(idA, idB, message)
      case LocalRecursionCall(variable)                         => LocalRecursionCall(variable)
  end cleanOnce

  @tailrec
  private def clean(local: LocalType): LocalType =
    val cleanLocal = cleanOnce(local)
    if cleanLocal == local then local else clean(cleanLocal)
  end clean

  def apply(global: GlobalType): Set[LocalType] = for role <- roles(global) yield clean(projection(global, role))
end Projection