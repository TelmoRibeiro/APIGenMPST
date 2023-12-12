package mpst.wellformedness

import mpst.syntax.Protocol
import mpst.syntax.Protocol._

object Projectability:
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
      // unexpected case //
      case _ => throw new RuntimeException("Expected: LocalType\nFound: GlobalType")
  end interactions

  private def projectability(local: Protocol): Boolean =
    local match
      // terminal cases //
      case Send   (_, _, _) => true
      case Receive(_, _, _) => true
      case End              => true
      case RecursionCall(_) => true
      // recursive cases //
      case RecursionFixedPoint(_, localB) => projectability(localB)
      case Sequence(localA, localB)       => projectability(localA) && projectability(localB)
      case Parallel(localA, localB)       => projectability(localA) && projectability(localB)
      case   Choice(localA, localB)       =>
        val iterationsA: Set[Protocol] = interactions(localA)
        val iterationsB: Set[Protocol] = interactions(localB)
        iterationsA.intersect(iterationsB).isEmpty
      // unexpected cases //
      case _ => throw new RuntimeException("Expected: LocalType\nFound: GlobalType")
  end projectability

  def apply(local: Protocol): Boolean = projectability(local)
end Projectability