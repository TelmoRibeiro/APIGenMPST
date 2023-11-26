package mpst.syntax

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
end LocalType