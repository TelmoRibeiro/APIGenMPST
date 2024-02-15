package mpst.projectability

import mpst.syntax.Protocol
import mpst.syntax.Protocol._

object Linearity:
  private def linearity(global: Protocol): Boolean =
    global match
      // terminal cases //
      case Interaction(_, _, _) => true
      case RecursionCall(_)     => true
      case End                  => true
      // recursive cases //
      case RecursionFixedPoint(_, globalB) => linearity(globalB)
      case Sequence(globalA, globalB)      => linearity(globalA) && linearity(globalB)
      case Parallel(globalA, globalB)      =>
        val iterationsA: Set[Protocol] = interactions(globalA)
        val iterationsB: Set[Protocol] = interactions(globalB)
        (iterationsA intersect iterationsB).isEmpty
      case Choice  (globalA, globalB)      => linearity(globalA) && linearity(globalB)
      // unexpected cases //
      case Skip  => throw new RuntimeException("unexpected case of \"Skip\"\n")
      case local => throw new RuntimeException(s"unexpected local type $local found\n")
  end linearity

  def apply(global: Protocol): Boolean = linearity(global)
end Linearity