package mpst.wellformedness

import mpst.syntax.GlobalType
import mpst.syntax.GlobalType._

object Projectability:
  private def interactions(local: GlobalType): Set[GlobalType] =
    local match
      // terminal cases //
      case Send   (idA, idB, message) => Set() + Send   (idA, idB, message)
      case Receive(idA, idB, message) => Set() + Receive(idA, idB, message)
      case End => Set()
      case RecursionCall(_) => Set()
      // recursive cases //
      case RecursionFixedPoint(_, localB) => interactions(localB)
      case Sequence(localA, localB) => interactions(localA) ++ interactions(localB)
      case Parallel(localA, localB) => interactions(localA) ++ interactions(localB)
      case Choice  (localA, localB) => interactions(localA) ++ interactions(localB)
      // unexpected case //
      case _ => throw new RuntimeException("Expected: LocalType\nFound: GlobalType")
  end interactions

  // projectability: checks if two branches could be confused //
  private def projectability(local: GlobalType): Boolean =
    local match
      // terminal cases //
      case Send   (_, _, _) => true
      case Receive(_, _, _) => true
      case End              => true
      case RecursionCall(_) => true
      // recursive cases //
      case RecursionFixedPoint(_, localB) => projectability(localB)
      case Sequence(localA, localB) => projectability(localA) && projectability(localB)
      case Parallel(localA, localB) => projectability(localA) && projectability(localB)
      case   Choice(localA, localB) =>
        val iterationsA: Set[GlobalType] = interactions(localA)
        val iterationsB: Set[GlobalType] = interactions(localB)
        iterationsA.intersect(iterationsB).isEmpty
      // unexpected case //
      case _ => throw new RuntimeException("Expected: LocalType\nFound: GlobalType")
  end projectability

  def apply(local: GlobalType): Boolean = projectability(local)
end Projectability