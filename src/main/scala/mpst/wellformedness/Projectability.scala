package mpst.wellformedness

import scala.annotation.tailrec

import mpst.syntax.Protocol
import mpst.syntax.Protocol.*

object Projectability:
  @tailrec
  private def roleProjectability(globalA: Protocol, globalB: Protocol, roles: Set[String], isStillProjectable: Boolean): Boolean =
    if   roles.isEmpty
    then isStillProjectable
    else
      val role: String               = roles.head
      val headGlobalA: Set[Protocol] = headInteraction(globalA, role)
      val headGlobalB: Set[Protocol] = headInteraction(globalB, role)
      val isProjectable: Boolean     = (headGlobalA intersect headGlobalB).isEmpty
      roleProjectability(globalA, globalB, roles - role, isStillProjectable && isProjectable)
  end roleProjectability

  private def projectability(global: Protocol): Boolean =
    global match
      // terminal cases //
      case Interaction(_, _, _) => true
      case RecursionCall(_)     => true
      // recursive cases //
      case RecursionFixedPoint(_, globalB) => projectability(globalB)
      case Sequence(globalA, globalB)      => projectability(globalA) && projectability(globalB)
      case Parallel(globalA, globalB)      => projectability(globalA) && projectability(globalB)
      case   Choice(globalA, globalB)      => roleProjectability(globalA, globalB, roles(global), true)
      // unexpected cases //
      case _ => throw new RuntimeException("\nExpected:\tGlobalType\nFound:\t\tLocalType")
  end projectability

  def apply(global: Protocol): Boolean = projectability(global)
end Projectability