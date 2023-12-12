package mpst.wellformedness

import mpst.syntax.Protocol
import mpst.syntax.Protocol._

object EndPosition:
  private def endPosition(global: Protocol): Boolean =
    global match
      // terminal cases //
      case Interaction(_, _, _) => true
      case End                  => true
      case RecursionCall(_)     => true
      // recursive cases //
      case RecursionFixedPoint(_, globalB) =>
        globalB match
          case End => false
          case _   => endPosition(globalB)
      case Sequence(globalA, globalB)      =>
        globalA match
          case End => false
          case _   => endPosition(globalA) && endPosition(globalB)
      case Parallel(globalA, globalB)      =>
        globalA match
          case End => false
          case _   => globalB match
            case End => false
            case _ => endPosition(globalA) && endPosition(globalB)
      case Choice  (globalA, globalB) =>
        globalA match
          case End => false
          case _ => globalB match
            case End => false
            case _ => endPosition(globalA) && endPosition(globalB)
      // unexpected cases //
      case _ => throw new RuntimeException("\nExpected:\tGlobalType\nFound:\t\tLocalType")
  end endPosition

  def apply(global: Protocol): Boolean = endPosition(global)
end EndPosition