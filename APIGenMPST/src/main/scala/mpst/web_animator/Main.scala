package mpst.web_animator

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

import org.scalajs.dom
import org.scalajs.dom.document

object Main:
  private def go(index:Int,nextNetwork:Set[(Action,Set[Protocol],Multiset[Action])],trace:List[Action])(using environment:Map[Variable,Protocol]):Unit =
    val (a,ls,p) = nextNetwork.toSeq(index)
    appendTextDiv(s"trace: ${trace :+ a}")
    networkMSTraverse(ls,p,trace :+ a)
  end go

  private def appendButton(label:String,index:Int,nextNetwork:Set[(Action,Set[Protocol],Multiset[Action])],trace:List[Action])(using environment:Map[Variable,Protocol]):Unit =
    val body = document.body
    val button = document.createElement("button").asInstanceOf[dom.html.Button]
    button.textContent = label
    button.onclick = (_:dom.MouseEvent) => {
      go(index,nextNetwork,trace)
    }
    body.appendChild(button)
  end appendButton

  private def appendTextDiv(text:String):Any =
    val body = document.body
    val div  = document.createElement("div").asInstanceOf[dom.html.Div]
    div.textContent = text
    body.appendChild(div)
    div
  end appendTextDiv

  // @tailrec
  private def networkMSTraverse(locals:Set[Protocol],pending:Multiset[Action],trace:List[Action])(using environment:Map[Variable,Protocol]):Unit =
    val nextNetwork = NetworkMultiset.nextNetwork(locals,pending)
    for local <- locals yield
      appendTextDiv(s"local: $local")
    appendTextDiv("")
    var index = 0
    for (nextAction,_,_) <- nextNetwork yield
      appendTextDiv(s"ACTION: $nextAction")
      appendButton(nextAction.toString,index,nextNetwork,trace)
      index += 1
    if nextNetwork.toSeq.isEmpty then
      appendTextDiv(s"final trace: $trace") ; return
    // appendTextDiv(s"action index:")
  end networkMSTraverse

  /*
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
  */

  def main(args: Array[String]): Unit =
    val protocol = "m>wA:Work<void> ; m>wB:Work<void> ; (wA>m:Done<void> ; end || wB>m:Done<void> ; end)" // should and it is accepted
    appendTextDiv(s"PROTOCOL: $protocol")
    val global = Parser(protocol)
    appendTextDiv(s"GLOBAL: $global")
    val wellformed =  WellCommunicated(global) && WellBounded(global) && WellChannelled(global) && WellBranched(global) && DependentlyGuarded(global)
    if !wellformed then throw new RuntimeException(s"not well formed!\n")
    for agent -> local <- projectionWithAgent(global) yield
      appendTextDiv(s"LOCAL [$agent] - $local")
    val locals = projection(global)
    val environment = getEnvironment(global)(using Map())
    networkMSTraverse(locals,Multiset(),Nil)(using environment)
  //networkCSTraverse(locals,Map(),Nil)(using environment)
  end main
end Main

/*
package mpst.web_animator

import mpst.operational_semantic.Network.*
import mpst.projection.Projection.*
import mpst.syntax.*
import mpst.utilities.Multiset
import mpst.utilities.Types.*
import mpst.utilities.Environment.*
import mpst.wellformedness.*

import org.scalajs.dom
import org.scalajs.dom.document

object Main:
  private def show(text:String):Unit =
    val body = document.body
    val div = document.createElement("div")
    div.textContent = text
    body.appendChild(div)
  end show

  def main(args:Array[String]):Unit =
    val protocol = "m>wA:Work<void> ; m>wB:Work<void> ; (wA>m:Done<void> ; end || wB>m:Done<void> ; end)"
    show(s"PROTOCOL: $protocol")
    val global = Parser(protocol)
    show(s"GLOBAL: $global")
    val wellformed = WellCommunicated(global) && WellBounded(global) && WellChannelled(global) && WellBranched(global) && DependentlyGuarded(global)
    if !wellformed then show(s"not wellformed!")
    for agent -> local <- projectionWithAgent(global) yield
      show(s"LOCAL [$agent] - $local")
    def networkTraverse(locals:Set[Protocol],network:Multiset[Action],counter:Int,trace:List[Action])(using environment:Map[Variable,Protocol]):Unit =
      show(s"TRACE: $trace")
      if counter <= 0 then
        show("OOPS, TO reached!")
        return
      val result = NetworkMultiset.next(locals,network)
      for (nextAction,nextLocals,nextNetwork) <- result yield
        show(s"CURRENT ENTRY")
        show(s"Action Selected: $nextAction")
        show(s"Locals State:")
        for nextLocal <- nextLocals yield
          show(s"Local: $nextLocal")
        show(s"Network State: $nextNetwork")
        show("\n")
      show("PLEASE PICK ACTION:")
      val submitButton = document.getElementById("submitButton")
      submitButton.addEventListener("click", (event: dom.Event) => {
        event.preventDefault()
        val inputField = document.getElementById("inputField").asInstanceOf[dom.html.Input]
        val inputValue = inputField.value
        val integerValue = inputValue.toInt
        val (a, ls, n) = result.toSeq(integerValue)
        networkTraverse(ls, n, counter - 1, trace :+ a)
      })
    end networkTraverse
    val locals = projection(global)
    val environment = getEnvironment(global)(using Map())
    networkTraverse(locals,Multiset(),10,Nil)(using environment)
  end main
end Main
*/