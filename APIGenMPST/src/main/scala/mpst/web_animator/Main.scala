package mpst.web_animator

import mpst.operational_semantic.Network.*
import mpst.projection.Projection.*
import mpst.syntax.Parser
import mpst.syntax.Protocol
import mpst.utilities.Environment.*
import mpst.utilities.Multiset
import mpst.utilities.Types.*
import mpst.wellformedness.*
import org.scalajs.dom

import org.scalajs.dom.{HTMLParagraphElement, MouseEvent, document, html}

object Main:
  private def removeButtons(except:Set[String]):Unit =
    val buttons = document.querySelectorAll("button")
    for button <- buttons if !(except contains button.id) yield
      button.parentNode.removeChild(button)
  end removeButtons

  private def addParagraph(text:String,parentNode:dom.HTMLElement):Unit =
    val paragraph = dom.document.createElement("p").asInstanceOf[HTMLParagraphElement]
    paragraph.textContent = text
    parentNode.appendChild(paragraph)
  end addParagraph

  private def selectAction(buttonIndex:Int,trace:List[Action],network:Set[(Action,Set[Protocol],Multiset[Action])])(using environment:Map[Variable,Protocol]):Unit =
    if network.isEmpty then
      addParagraph(s"FINAL TRACE: $trace",document.body)
      return
    val (action,locals,pending) = network.toSeq(buttonIndex)
    addParagraph(s"TRACE: ${trace :+ action}",document.body)
    removeButtons(Set() + "protocolButton")
    networkMSTraverse(locals,pending,trace :+ action)
  end selectAction

  private def addActionButton(label:String,index:Int,trace:List[Action],network:Set[(Action,Set[Protocol],Multiset[Action])])(using environment:Map[Variable,Protocol]):Unit =
    val button = document.createElement("button").asInstanceOf[html.Button]
    button.textContent = label
    button.onclick     = (_:dom.MouseEvent) => {
      selectAction(index,trace,network)
    }
    document.body.appendChild(button)
  end addActionButton

  private def networkMSTraverse(locals:Set[Protocol],pending:Multiset[Action],trace:List[Action])(using environment:Map[Variable,Protocol]):Unit =
    val nextNetwork = NetworkMultiset.nextNetwork(locals,pending)
    for local <- locals yield
      addParagraph(s"LOCAL: $local",document.body)
    var actionIndex = 0
    for (nextAction,_,_) <- nextNetwork yield
      addActionButton(nextAction.toString,actionIndex,trace,nextNetwork)
      actionIndex += 1
  end networkMSTraverse

  private def showProtocol(protocol:String):Unit =
    val protocolTitle = document.getElementById("protocolTitle").asInstanceOf[html.Title]
    val protocolDiv   = showText("PROTOCOL",protocol)
    protocolTitle.parentNode.insertBefore(protocolDiv,protocolTitle.nextSibling)
  end showProtocol

  private def showGlobal(global:String):Unit =
    val globalTitle = document.getElementById("globalTitle").asInstanceOf[html.Title]
    val globalDiv   = showText("GLOBAL",global)
    globalTitle.parentNode.insertBefore(globalDiv,globalTitle.nextSibling)
  end showGlobal

  private def showAnalysis(wellFormed:Boolean):Unit =
    val analysisTitle = document.getElementById("analysisTitle").asInstanceOf[html.Title]
    val analysisDiv   = showText("WELL-FORMED",wellFormed.toString)
    analysisTitle.parentNode.insertBefore(analysisDiv,analysisTitle.nextSibling)
  end showAnalysis

  private def showLocal(agent:String,local:String):Unit =
    val localTitle = document.getElementById("localTitle").asInstanceOf[html.Title]
    val localDiv   = showText(s"LOCAL [$agent]",local)
    localTitle.parentNode.insertBefore(localDiv,localTitle.nextSibling)
  end showLocal

  private def showText(label:String,description:String):html.Div =
    val div = document.createElement("div").asInstanceOf[html.Div]
    val labelSpan = document.createElement("span").asInstanceOf[html.Span]
    labelSpan.textContent = label + ": "
    labelSpan.style.color = "blue"
    val descriptionSpan = document.createElement("span").asInstanceOf[html.Span]
    descriptionSpan.textContent = description
    descriptionSpan.style.color = "black"
    div.appendChild(labelSpan)
    div.appendChild(descriptionSpan)
    div
  end showText

  def main(args:Array[String]):Unit =
    val protocol = "m>wA:Work<void> ; m>wB:Work<void> ; (wA>m:Done<void> ; end || wB>m:Done<void> ; end)"
    showProtocol(protocol)
    val global = Parser(protocol)
    showGlobal(global.toString)
    val wellFormed = WellCommunicated(global) && WellBounded(global) && WellChannelled(global) && WellBranched(global) && DependentlyGuarded(global)
    showAnalysis(wellFormed)
    for agent -> local <- projectionWithAgent(global) yield
      showLocal(agent,local.toString)
    val environment = getEnvironment(global)(using Map())
    val locals      = projection(global)
    networkMSTraverse(locals,Multiset(),Nil)(using environment)
  end main
end Main