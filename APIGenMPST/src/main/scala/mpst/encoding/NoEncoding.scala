package mpst.encoding

import mpst.operational_semantic.AsyncSemantic
import mpst.syntax.Protocol
import mpst.utilities.Environment.*
import mpst.utilities.Types.*

object NoEncoding:
  private def lazyTraverse(next:Set[(Action,Protocol)],visited:Set[(Action,Protocol)])(using environment:Map[Variable,Protocol]):Unit =
    if next.isEmpty then
      println("done traversing")
      return
    val nextAction -> nextLocal = next.head
    println(s"Action: $nextAction")
    println(s" Local: $nextLocal")
    if visited contains next.head then
      println("reduction was visited already\n")
      lazyTraverse(next.tail,visited)
      return
    println()
    val children = AsyncSemantic.next(nextLocal)
    lazyTraverse(children ++ next.tail,visited + next.head)
  end lazyTraverse

  def apply(local:Protocol):Unit =
    val environment = getEnvironment(local)(using Map())
    lazyTraverse(AsyncSemantic.next(local)(using environment),Set())(using environment)
  end apply
end NoEncoding