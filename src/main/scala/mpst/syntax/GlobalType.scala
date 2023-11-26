package mpst.syntax

import scala.annotation.tailrec

// GLOBAL TYPE'S GRAMMAR
sealed trait GlobalType

/*  To Do:
    1) implementation in ENUM
    2) show (pretty print)
*/
/*  TO ASK:
    1) Why isn't associative || needed?
*/
object GlobalType:
  private def cleanOnce(global: GlobalType): GlobalType =
    global match
      // associate ;
      case Sequence(Sequence(globalA, globalB), globalC) => cleanOnce(Sequence(globalA, Sequence(globalB, globalC)))
      // associate +
      case Choice(Choice(globalA, globalB), globalC) => cleanOnce(Choice(globalA, Choice(globalB, globalC)))
      // remove end
      case Sequence(End, global)       => cleanOnce(global)
      case Sequence(global, End)       => cleanOnce(global)
      case Parallel(End, global)       => cleanOnce(global)
      case Parallel(global, End)       => cleanOnce(global)
      case RecursionFixedPoint(_, End) => End
      // recursive
      case Sequence(globalA, globalB)                       => Sequence(cleanOnce(globalA), cleanOnce(globalB))
      case Parallel(globalA, globalB)                       => Parallel(cleanOnce(globalA), cleanOnce(globalB))
      case Choice  (globalA, globalB) if globalA == globalB => cleanOnce(globalA)
      case Choice  (globalA, globalB)                       => Choice  (cleanOnce(globalA), cleanOnce(globalB))
      case RecursionFixedPoint(variable, global)            => RecursionFixedPoint(variable, cleanOnce(global))
      // terminals
      case End                            => End
      case Interaction(idA, idB, message) => Interaction(idA, idB, message)
      case RecursionCall(variable)        => RecursionCall(variable)
  end cleanOnce

  @tailrec
  def clean(global: GlobalType): GlobalType =
    val cleanGlobal = cleanOnce(global)
    if  cleanGlobal == global then global else clean(cleanGlobal)
  end clean
  
  case class Interaction(actorA: String, actorB: String, message: String) extends GlobalType // actorA > actorB: message
  case object End                                                         extends GlobalType // end
  case class RecursionFixedPoint(variable: String, global: GlobalType)    extends GlobalType // μX ; G
  case class RecursionCall(variable: String)                              extends GlobalType // X
  case class Sequence(globalA: GlobalType, globalB: GlobalType)           extends GlobalType // GA ;  GB
  case class Parallel(globalA: GlobalType, globalB: GlobalType)           extends GlobalType // GA || GB
  case class Choice  (globalA: GlobalType, globalB: GlobalType)           extends GlobalType // GA +  GB
end GlobalType

/*
  Message can be (reasonably) extended in order to allow for Asserted MPST:
    actorA>actorB:l(x: S){A} where
      l     => branch label
      x : S => interaction variable (x) of sort (S)
      A     => assertion
    example: C>A:Login(x: string){tt}
*/