package mpst.syntax

import scala.annotation.tailrec

// GLOBAL TYPE'S GRAMMAR
enum GlobalType:
  case Interaction(actorA: String, actorB: String, message: String) // actorA > actorB: message
  case End extends GlobalType                                       // end
  case RecursionFixedPoint(variable: String, global: GlobalType)    // μX ; G
  case RecursionCall(variable: String) extends GlobalType           // X
  case Sequence(globalA: GlobalType, globalB: GlobalType)           // GA ;  GB
  case Parallel(globalA: GlobalType, globalB: GlobalType)           // GA || GB
  case Choice  (globalA: GlobalType, globalB: GlobalType)           // GA +  GB
end GlobalType

object GlobalType:
  private def cleanOnce(global: GlobalType): GlobalType =
    global match
      // associate ";", "||" and "+"
      case Sequence(Sequence(globalA, globalB), globalC) => cleanOnce(Sequence(globalA, Sequence(globalB, globalC)))
      case Parallel(Parallel(globalA, globalB), globalC) => cleanOnce(Parallel(globalA, Parallel(globalB, globalC)))
      case Choice  (Choice  (globalA, globalB), globalC) => cleanOnce(Choice(globalA,   Choice  (globalB, globalC)))
      // remove "end"
      case Sequence(End, global) => cleanOnce(global)
      case Sequence(global, End) => cleanOnce(global)
      case Parallel(End, global) => cleanOnce(global)
      case Parallel(global, End) => cleanOnce(global)
      case RecursionFixedPoint(_, End) => End
      // recursive and terminal cases
      case Sequence(globalA, globalB)                       => Sequence(cleanOnce(globalA), cleanOnce(globalB))
      case Parallel(globalA, globalB)                       => Parallel(cleanOnce(globalA), cleanOnce(globalB))
      case Choice  (globalA, globalB) if globalA == globalB => cleanOnce(globalA)
      case Choice  (globalA, globalB)                       => Choice  (cleanOnce(globalA), cleanOnce(globalB))
      case RecursionFixedPoint(variable, global)            => RecursionFixedPoint(variable, cleanOnce(global))
      case End                            => End
      case Interaction(idA, idB, message) => Interaction(idA, idB, message)
      case RecursionCall(variable)        => RecursionCall(variable)
  end cleanOnce

  @tailrec
  private def clean(global: GlobalType): GlobalType =
    val cleanGlobal = cleanOnce(global)
    if  cleanGlobal == global then global else clean(cleanGlobal)
  end clean

  def apply(global: GlobalType): GlobalType = clean(global)
end GlobalType