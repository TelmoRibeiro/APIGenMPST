package mpst.web_animator

import mpst.syntax.{Protocol, Parser}
import mpst.syntax.Protocol._
import mpst.projection.Projection
import mpst.operational_semantic.local_semantic.SSFI_semantic
import org.scalajs.dom
import org.scalajs.dom.document
import org.scalajs.dom.html


object Main:
  private def reduce(reductions: List[(Action,State)], reductionIndex: Int): Unit =
    val action -> (environment -> local) = reductions(reductionIndex)
    val nextReductions = SSFI_semantic.reduce(environment, local)
    for action -> state <- nextReductions yield println(s"$action : $state")
    readAction(nextReductions)
  end reduce

  private def readAction(reductions: List[(Action, State)]): Unit =
    val actionInput = document.getElementById("action-input").asInstanceOf[html.Input]
    actionInput.addEventListener("change", {(event: dom.Event) =>
      val actionIndex: Int = actionInput.value.toInt
      reduce(reductions, actionIndex)
    })
  end readAction

  private def processRole(role: String, global: Protocol): Unit =
    println(s"ROLE: $role")
    val local: Protocol = Projection(role, global)
    println(s"LOCAL ($role): $local")
    // allow for semantic picking //
    val reductions: List[(Action,State)] = SSFI_semantic.reduce(Map(), local)
    for action -> state <- reductions yield println(s"$action : $state")
    readAction(reductions)
  end processRole

  private def readRole(global: Protocol): Unit =
    val roleInput = document.getElementById("role-input").asInstanceOf[html.Input]
    roleInput.addEventListener("change", {(event: dom.Event) =>
      val role: String = roleInput.value
      processRole(role, global)
    })
  end readRole

  private def processProtocol(protocol: String): Unit =
    // swap protocolTemp for protocol //
    val protocolTemp: String = "def X in (m>wA:Work ; X || m>wB:Work ; X)"
    println(s"PROTOCOL: $protocolTemp")
    val global: Protocol = Parser(protocolTemp)
    println(s"GLOBAL: $global")
    println("ROLES:")
    for role <- Protocol.roles(global) yield println(role)
    readRole(global)
  end processProtocol

  private def readDSL(): Unit =
    val protocolInput = document.getElementById("protocol-input").asInstanceOf[html.Input]
    protocolInput.addEventListener("change", {(event: dom.Event) =>
      val protocol: String = protocolInput.value
      processProtocol(protocol)
    })
  end readDSL

  private def setupIndex(): Unit =
    document.getElementById("title").innerHTML = "JointMPST"
  end setupIndex

  def main(args: Array[String]): Unit =
    setupIndex()
    readDSL()
  end main
end Main