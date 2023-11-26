package mpst.syntax

import scala.annotation.tailrec

// LOCAL TYPE'S GRAMMAR
sealed trait LocalType

object LocalType:
  case class LocalSend    (actorA: String, actorB: String, message: String) extends LocalType // actorA, actorB! message
  case class LocalReceive (actorA: String, actorB: String, message: String) extends LocalType // actorA, actorB? message
  case object LocalEnd                                                      extends LocalType // end
  case class LocalRecursionFixedPoint(variable: String, local: LocalType)   extends LocalType // μX ; G
  case class LocalRecursionCall(variable: String)                           extends LocalType // X
  case class LocalSequence(localA: LocalType, localB: LocalType)            extends LocalType // GA ;  GB
  case class LocalParallel(localA: LocalType, localB: LocalType)            extends LocalType // GA || GB
  case class LocalChoice  (localA: LocalType, localB: LocalType)            extends LocalType // GA +  GB

  /*  TO ASK:
      1) Why isn't associative || needed?
  */
  private def cleanOnce(local: LocalType): LocalType =
    local match
      // associate ;
      case LocalSequence(LocalSequence(localA, localB), localC) => cleanOnce(LocalSequence(localA, LocalSequence(localB, localC)))
      // associate +
      case LocalChoice(LocalChoice(localA, localB), localC) => cleanOnce(LocalChoice(localA, LocalChoice(localB, localC)))
      // remove end
      case LocalSequence(LocalEnd, local)        => cleanOnce(local)
      case LocalSequence(local, LocalEnd)        => cleanOnce(local)
      case LocalParallel(LocalEnd, local)        => cleanOnce(local)
      case LocalParallel(local, LocalEnd)        => cleanOnce(local)
      case LocalRecursionFixedPoint(_, LocalEnd) => LocalEnd
      // recursive
      case LocalSequence(localA, localB)                     => LocalSequence(cleanOnce(localA), cleanOnce(localB))
      case LocalParallel(localA, localB)                     => LocalParallel(cleanOnce(localA), cleanOnce(localB))
      case LocalChoice  (localA, localB) if localA == localB => cleanOnce(localA)
      case LocalChoice  (localA, localB)                     => LocalChoice  (cleanOnce(localA), cleanOnce(localB))
      case LocalRecursionFixedPoint(variable, local)         => LocalRecursionFixedPoint(variable, cleanOnce(local))
      // terminals
      case LocalEnd => LocalEnd
      case LocalSend   (idA, idB, message) => LocalSend   (idA, idB, message)
      case LocalReceive(idA, idB, message) => LocalReceive(idA, idB, message)
      case LocalRecursionCall(variable)    => LocalRecursionCall(variable)
  end cleanOnce

  @tailrec
  def clean(local: LocalType): LocalType =
    val cleanLocal = cleanOnce(local)
    if cleanLocal == local then local else clean(cleanLocal)
  end clean
end LocalType