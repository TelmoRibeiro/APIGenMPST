package mpst.wellformedness

import mpst.syntax.Protocol
import mpst.syntax.Protocol._

object Projectability:
  private def projectability(global: Protocol): Boolean =
    global match
      // terminal cases //
      case Interaction(_, _, _) => true
      case RecursionCall(_)     => true
      // recursive cases //
      case RecursionFixedPoint(_, globalB) => projectability(globalB)
      case Sequence(globalA, globalB)      => projectability(globalA) && projectability(globalB)
      case Parallel(globalA, globalB)      => projectability(globalA) && projectability(globalB)
      case   Choice(globalA, globalB)      =>
        val iterationsA: Set[Protocol] = interactions(globalA)
        val iterationsB: Set[Protocol] = interactions(globalB)
        iterationsA.intersect(iterationsB).isEmpty
      // unexpected cases //
      case _ => throw new RuntimeException("\nExpected:\tGlobalType\nFound:\t\tLocalType")
  end projectability

  def apply(global: Protocol): Boolean = projectability(global)
end Projectability