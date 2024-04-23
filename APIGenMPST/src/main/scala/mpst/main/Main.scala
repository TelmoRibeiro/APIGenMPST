/*
package mpst.main

import mpst.operational_semantic.Network.*
import mpst.projection.Projection.*
import mpst.syntax.Parser
import mpst.syntax.Protocol
import mpst.utilities.Environment.*
import mpst.utilities.Multiset
import mpst.utilities.Types.*
import mpst.wellformedness.*

import scala.annotation.tailrec
import scala.io.StdIn

object Main:
  @tailrec
  private def networkMSTraverse(locals:Set[Protocol],pending:Multiset[Action],trace:List[Action])(using environment:Map[Variable,Protocol]):Unit =
    val nextNetwork = NetworkMultiset.nextNetwork(locals,pending)
    for local <- locals yield
      println(s"local: $local")
    println()
    for (nextAction,_,_) <- nextNetwork yield
      println(s"ACTION: $nextAction\n")
    if nextNetwork.toSeq.isEmpty then
      println(s"final trace: $trace") ; return
    println(s"action index:")
    val actionIndex = StdIn.readInt( )
    val (a,ls,p) = nextNetwork.toSeq(actionIndex)
    println(s"trace: ${trace :+ a}")
    networkMSTraverse(ls,p,trace :+ a)
  end networkMSTraverse

  type Queue = Map[(Agent,Agent),List[Message]]
  @tailrec
  private def networkCSTraverse(locals:Set[Protocol],pending:Queue,trace:List[Action])(using environment:Map[Variable,Protocol]):Unit =
    val nextNetwork = NetworkCausal.nextNetwork(locals,pending)
    for local <- locals yield
      println(s"local: $local")
    println()
    for (nextAction,_,_) <- nextNetwork yield
      println(s"ACTION: $nextAction\n")
    if nextNetwork.toSeq.isEmpty then
      println(s"final trace: $trace") ; return
    println(s"action index:")
    val actionIndex = StdIn.readInt( )
    val (a,ls,p) = nextNetwork.toSeq(actionIndex)
    println(s"trace: ${trace :+ a}")
    networkCSTraverse(ls,p,trace :+ a)
  end networkCSTraverse

  def main(args: Array[String]): Unit =
    // Tests()
    // val protocol = "(m>wA:Done<void> + m>wB:Done<void>)" // not accepted by me or oven but accepted by choreo
    // val protocol = "(m>wA:Done<void> + (m>wA:NotDone<void> ; (m>wB:Done<void> + m>wB:NotDone<void>)))" accepted by me but not oven
    // val protocol = "s>b:Descr<void> ; s>b:Price<void> ; (s>b:Acc<void> + s>b:Rej<void>) ; end" // should and it is accepted
    val protocol = "m>wA:Work<void> ; m>wB:Work<void> ; (wA>m:Done<void> ; end || wB>m:Done<void> ; end)" // should and it is accepted
    println(s"PROTOCOL: $protocol")
    val global = Parser(protocol)
    println(s"GLOBAL: $global")
    val wellformed =  WellCommunicated(global) && WellBounded(global) && WellChannelled(global) && WellBranched(global) && DependentlyGuarded(global)
    if !wellformed then throw new RuntimeException(s"not well formed!\n")
    for agent -> local <- projectionWithAgent(global) yield
      println(s"LOCAL [$agent] - $local")
    val locals = projection(global)
    val environment = getEnvironment(global)(using Map())
    networkMSTraverse(locals,Multiset(),Nil)(using environment)
    //networkCSTraverse(locals,Map(),Nil)(using environment)
  end main
end Main
*/