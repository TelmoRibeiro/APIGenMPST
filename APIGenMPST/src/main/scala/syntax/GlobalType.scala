package syntax

// GLOBAL TYPE'S GRAMMAR
sealed trait GlobalType
case object End                                                         extends show.syntax.GlobalType // end
case class  Message(actorA: String, actorB: String, globalType: String) extends show.syntax.GlobalType // actorA > actorB: globalType
case class  Choice  (globalA: show.syntax.GlobalType, globalB: show.syntax.GlobalType)          extends show.syntax.GlobalType // GA +  GB
case class  Parallel(globalA: show.syntax.GlobalType, globalB: show.syntax.GlobalType)          extends show.syntax.GlobalType // GA || GB
case class  Sequence(globalA: show.syntax.GlobalType, globalB: show.syntax.GlobalType)          extends GlobalType // GA ;  GB