package mpst.wellformedness

import mpst.syntax.Protocol
import mpst.syntax.Protocol.*

/* IDEA:
  - do not allow p->p:m<s>
    - many references avoiding it

  @ telmo -
    naive!
      can I not do it at "parse time"?
      at least merge with other Well-Something for optimisation!
*/

object WellCommunicated:
  private def isWellCommunicated(global:Protocol):Boolean =
    global match
      case Interaction(agentA,agentB,_,_) => agentA != agentB
      case RecursionCall(_) => true
      case End => true
      case Sequence(globalA,globalB) => isWellCommunicated(globalA) && isWellCommunicated(globalB)
      case Parallel(globalA,globalB) => isWellCommunicated(globalA) && isWellCommunicated(globalB)
      case Choice  (globalA,globalB) => isWellCommunicated(globalA) && isWellCommunicated(globalB)
      case RecursionFixedPoint(_,globalB) => isWellCommunicated(globalB)
      case local => throw new RuntimeException(s"unexpected local type found in [$local]\n")
  end isWellCommunicated

  def apply(global:Protocol):Boolean =
    isWellCommunicated(global)
  end apply
end WellCommunicated