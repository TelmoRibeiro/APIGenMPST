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