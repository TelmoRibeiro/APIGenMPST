package mpst.syntax

import scala.annotation.tailrec

enum GlobalType:
  case Interaction(actorA: String, actorB: String, message: String) extends GlobalType // actorA > actorB: message
  case End                                                          extends GlobalType // end
  case RecursionFixedPoint(variable: String, global: GlobalType)    extends GlobalType // μX ; G
  case RecursionCall(variable: String)                              extends GlobalType // X
  case Sequence(globalA: GlobalType, globalB: GlobalType)           extends GlobalType // GA ;  GB
  case Parallel(globalA: GlobalType, globalB: GlobalType)           extends GlobalType // GA || GB
  case Choice  (globalA: GlobalType, globalB: GlobalType)           extends GlobalType // GA +  GB
  case Send   (actorA: String, actorB: String, message: String)     extends GlobalType // actorA,actorB!message
  case Receive(actorA: String, actorB: String, message: String)     extends GlobalType // actorA,actorB?message
  case NoAction                                                     extends GlobalType

// GLOBAL TYPE'S GRAMMAR
object GlobalType:
  private def cleanOnce(global: GlobalType): GlobalType =
    global match 
      // associate ";", "||" and "+"
      case Sequence(Sequence(globalA, globalB), globalC) => cleanOnce(Sequence(globalA, Sequence(globalB, globalC)))
      case Parallel(Parallel(globalA, globalB), globalC) => cleanOnce(Parallel(globalA, Parallel(globalB, globalC)))
      case Choice  (Choice  (globalA, globalB), globalC) => cleanOnce(Choice(globalA,   Choice  (globalB, globalC)))
      // remove "NoAction"
      case Sequence(NoAction, global) => cleanOnce(global)
      case Sequence(global, NoAction) => cleanOnce(global)
      case Parallel(NoAction, global) => cleanOnce(global)
      case Parallel(global, NoAction) => cleanOnce(global)
      case Choice  (NoAction, global) => cleanOnce(global)
      case Choice  (global, NoAction) => cleanOnce(global)
      case RecursionFixedPoint(_, NoAction) => NoAction
      // recursive cases //
      case Parallel(RecursionCall(_), global) => cleanOnce(global) // testing
      case Choice  (RecursionCall(_), global) => cleanOnce(global) // testing

      case Sequence(globalA, globalB)                       => Sequence(cleanOnce(globalA), cleanOnce(globalB))
      case Parallel(globalA, globalB)                       => Parallel(cleanOnce(globalA), cleanOnce(globalB))
      case Choice  (globalA, globalB) if globalA == globalB => cleanOnce(globalA)
      case Choice  (globalA, globalB)                       => Choice  (cleanOnce(globalA), cleanOnce(globalB))
      case RecursionFixedPoint(variable, global)            => RecursionFixedPoint(variable, cleanOnce(global))
      // terminal cases //
      case End                            => End
      case NoAction                       => NoAction
      case Interaction(idA, idB, message) => Interaction(idA, idB, message)
      case RecursionCall(variable)        => RecursionCall(variable)
      case Send   (idA, idB, message)     => Send   (idA, idB, message)
      case Receive(idA, idB, message)     => Receive(idA, idB, message)
  end cleanOnce

  @tailrec
  private def clean(global: GlobalType): GlobalType =
    val cleanGlobal = cleanOnce(global)
    if  cleanGlobal == global then global else clean(cleanGlobal)
  end clean

  def apply(global: GlobalType): GlobalType = clean(global)
end GlobalType