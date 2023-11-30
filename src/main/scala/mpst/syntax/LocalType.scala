package mpst.syntax

import scala.annotation.tailrec

// LOCAL TYPE'S GRAMMAR
enum LocalType:
  case LocalSend(actorA: String, actorB: String, message: String)    // actorA, actorB! message
  case LocalReceive(actorA: String, actorB: String, message: String) // actorA, actorB? message
  case LocalEnd                                                      // end
  case LocalRecursionFixedPoint(variable: String, local: LocalType)  // μX ; G
  case LocalRecursionCall(variable: String)                          // X
  case LocalSequence(localA: LocalType, localB: LocalType)           // GA ;  GB
  case LocalParallel(localA: LocalType, localB: LocalType)           // GA || GB
  case LocalChoice(localA: LocalType, localB: LocalType)             // GA +  GB
end LocalType

object LocalType:
  private def cleanOnce(local: LocalType): LocalType =
    local match
      // associate ";", "||" and "+"
      case LocalSequence(LocalSequence(localA, localB), localC) => cleanOnce(LocalSequence(localA, LocalSequence(localB, localC)))
      case LocalParallel(LocalParallel(localA, localB), localC) => cleanOnce(LocalParallel(localA, LocalParallel(localB, localC)))
      case LocalChoice  (LocalChoice  (localA, localB), localC) => cleanOnce(LocalChoice  (localA, LocalChoice  (localB, localC)))
      // remove "end"
      case LocalSequence(LocalEnd, local) => cleanOnce(local)
      case LocalSequence(local, LocalEnd) => cleanOnce(local)
      case LocalParallel(LocalEnd, local) => cleanOnce(local)
      case LocalParallel(local, LocalEnd) => cleanOnce(local)
      case LocalRecursionFixedPoint(_, LocalEnd) => LocalEnd
      // recursive and terminal cases
      case LocalSequence(localA, localB)                   => LocalSequence(cleanOnce(localA), cleanOnce(localB))
      case LocalParallel(localA, localB)                   => LocalParallel(cleanOnce(localA), cleanOnce(localB))
      case LocalChoice(localA, localB) if localA == localB => cleanOnce(localA)
      case LocalChoice(localA, localB)                     => LocalChoice(cleanOnce(localA), cleanOnce(localB))
      case LocalRecursionFixedPoint(variable, local)       => LocalRecursionFixedPoint(variable, cleanOnce(local))
      case LocalEnd                        => LocalEnd
      case LocalSend(idA, idB, message)    => LocalSend(idA, idB, message)
      case LocalReceive(idA, idB, message) => LocalReceive(idA, idB, message)
      case LocalRecursionCall(variable)    => LocalRecursionCall(variable)
  end cleanOnce

  @tailrec
  private def clean(local: LocalType): LocalType =
    val cleanLocal = cleanOnce(local)
    if cleanLocal == local then local else clean(cleanLocal)
  end clean

  def apply(local: LocalType): LocalType = clean(local)
end LocalType