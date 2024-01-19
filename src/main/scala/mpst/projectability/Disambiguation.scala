package mpst.projectability

import scala.annotation.tailrec

import mpst.syntax.Protocol
import mpst.syntax.Protocol._

object Disambiguation:
  @tailrec
  private def roleDisambiguation(globalA: Protocol, globalB: Protocol, roles: Set[String], isStillProjectable: Boolean): Boolean =
    if   roles.isEmpty
    then isStillProjectable
    else
      val role: String               = roles.head
      val headGlobalA: Set[Protocol] = headInteraction(globalA, role)
      val headGlobalB: Set[Protocol] = headInteraction(globalB, role)
      val isProjectable: Boolean     = (headGlobalA intersect headGlobalB).isEmpty
      roleDisambiguation(globalA, globalB, roles - role, isStillProjectable && isProjectable)
  end roleDisambiguation

  private def disambiguation(global: Protocol): Boolean =
    global match
      // terminal cases //
      case Interaction(_, _, _) => true
      case RecursionCall(_)     => true
      // recursive cases //
      case RecursionFixedPoint(_, globalB) => disambiguation(globalB)
      case Sequence(globalA, globalB)      => disambiguation(globalA) && disambiguation(globalB)
      case Parallel(globalA, globalB)      => disambiguation(globalA) && disambiguation(globalB)
      case   Choice(globalA, globalB)      => roleDisambiguation(globalA, globalB, roles(global), true)
      // unexpected cases //
      case _ => throw new RuntimeException("\nExpected:\tGlobalType\nFound:\t\tLocalType")
  end disambiguation

  def apply(global: Protocol): Boolean = disambiguation(global)
end Disambiguation