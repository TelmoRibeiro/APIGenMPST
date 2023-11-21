package syntax

// GLOBAL TYPE'S GRAMMAR
sealed trait GlobalType
case object End                                                         extends GlobalType // end
case class  Message(actorA: String, actorB: String, globalType: String) extends GlobalType // actorA > actorB: globalType
case class  Choice  (globalA: GlobalType, globalB: GlobalType)          extends GlobalType // GA +  GB
case class  Parallel(globalA: GlobalType, globalB: GlobalType)          extends GlobalType // GA || GB
case class  Sequence(globalA: GlobalType, globalB: GlobalType)          extends GlobalType // GA ;  GB