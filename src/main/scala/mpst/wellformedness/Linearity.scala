package mpst.wellformedness

import mpst.syntax.Protocol
import mpst.syntax.Protocol._

object Linearity:
  private def interactions(local: Protocol): Set[Protocol] =
    local match
      // terminal cases //
      case Send   (agentA, agentB, message) => Set() + Send   (agentA, agentB, message)
      case Receive(agentA, agentB, message) => Set() + Receive(agentA, agentB, message)
      case End                              => Set()
      case RecursionCall(_)                 => Set()
      // recursive cases //
      case RecursionFixedPoint(_, localB) => interactions(localB)
      case Sequence(localA, localB)       => interactions(localA) ++ interactions(localB)
      case Parallel(localA, localB)       => interactions(localA) ++ interactions(localB)
      case Choice  (localA, localB)       => interactions(localA) ++ interactions(localB)
      // unexpected cases //
      case _ => throw new RuntimeException("Expected: LocalType\nFound: GlobalType")
  end interactions

  private def linearity(local: Protocol): Boolean =
    local match
      // terminal cases //
      case Send   (_, _, _) => true
      case Receive(_, _, _) => true
      case End              => true
      case RecursionCall(_) => true
      // recursive cases //
      case RecursionFixedPoint(_, localB) => linearity(localB)
      case Sequence(localA, localB)       => linearity(localA) && linearity(localB)
      case Parallel(localA, localB)       =>
        val iterationsA: Set[Protocol] = interactions(localA)
        val iterationsB: Set[Protocol] = interactions(localB)
        iterationsA.intersect(iterationsB).isEmpty
      case Choice  (localA, localB)       => linearity(localA) && linearity(localB)
      // unexpected cases //
      case _ => throw new RuntimeException("Expected: LocalType\nFound: GlobalType")
  end linearity

  def apply(local: Protocol): Boolean = linearity(local)
end Linearity