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
    @tailrec
    def networkTraverse(locals:Set[Protocol],network:Multiset[Action],counter:Int,trace:List[Action])(using environment:Map[Variable,Protocol]):Unit =
      println(s"TRACE: $trace")
      if counter <= 0 then
        println("OOPS, TO reached!")
        return
      val result = NetworkMultiset.next(locals,network)
      for (nextAction,nextLocals,nextNetwork) <- result yield
        println(s"CURRENT ENTRY")
        println(s"Action Selected: $nextAction")
        println(s"Locals State:")
        for nextLocal <- nextLocals yield
          println(s"Local: $nextLocal")
        println(s"Network State: $nextNetwork")
        println()
      println(s"PLEASE PICK ACTION:")
      val actionIndex = StdIn.readInt()
      val (a,ls,n) = result.toSeq(actionIndex)
      networkTraverse(ls,n,counter-1,trace :+ a)
    end networkTraverse
    val locals = projection(global)
    val environment = getEnvironment(global)(using Map())
    networkTraverse(locals,Multiset(),1000,Nil)(using environment)
  end main
end Main