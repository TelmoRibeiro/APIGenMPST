package mpst.syntax

// LOCAL TYPE'S GRAMMAR
sealed trait LocalType

object LocalType:
  case class Send    (actorA: String, actorB: String, message: String) extends LocalType // actorA, actorB! message
  case class Receive (actorA: String, actorB: String, message: String) extends LocalType // actorA, actorB? message
  case object End                                                      extends LocalType // end
  case class RecursionFixedPoint(variable: String, local: LocalType)   extends LocalType // μX ; G
  case class RecursionCall(variable: String)                           extends LocalType // X
  case class Sequence(localA: LocalType, localB: LocalType)            extends LocalType // GA ;  GB
  case class Parallel(localA: LocalType, localB: LocalType)            extends LocalType // GA || GB
  case class Choice  (localA: LocalType, localB: LocalType)            extends LocalType // GA +  GB
end LocalType