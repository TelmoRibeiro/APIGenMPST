package mpst.syntax

// GLOBAL TYPE'S GRAMMAR
sealed trait GlobalType

object GlobalType:
  case class Interaction(actorA: String, actorB: String, message: String) extends GlobalType // actorA > actorB: message
  case object End                                                         extends GlobalType // end
  case class RecursionFixedPoint(variable: String, global: GlobalType)    extends GlobalType // μX ; G
  case class RecursionCall(variable: String)                              extends GlobalType // X
  case class Sequence(globalA: GlobalType, globalB: GlobalType)           extends GlobalType // GA ;  GB
  case class Parallel(globalA: GlobalType, globalB: GlobalType)           extends GlobalType // GA || GB
  case class Choice  (globalA: GlobalType, globalB: GlobalType)           extends GlobalType // GA +  GB
end GlobalType

// TRY IMPLEMENTATION IN ENUM //

// SHOW (PRETTY PRINT)

/*
  Message can be (reasonably) extended in order to allow for Asserted MPST:
    actorA>actorB:l(x: S){A} where
      l     => branch label
      x : S => interaction variable (x) of sort (S)
      A     => assertion
    example: C>A:Login(x: string){tt}
*/