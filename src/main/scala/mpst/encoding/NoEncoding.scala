package mpst.encoding

import mpst.syntax.Protocol
import mpst.syntax.Protocol.*
import mpst.operational_semantic.WSFI_semantic

object NoEncoding:
  // lazy implementation to traverse the semantic output //
  private def lazyTraverse(nextReductions: List[(Action,State)], visitedReductions: Set[(Action,State)]): Unit =
    if nextReductions.isEmpty then
      println("done traversing")
      return
    val reduction = nextReductions.head
    val reductionAction -> (reductionEnvironment -> reductionLocal) = reduction
    println(s"Action: $reductionAction")
    println(s" Local: $reductionLocal")
    if visitedReductions contains reduction then
      println("reduction was visited already\n")
      lazyTraverse(nextReductions.tail, visitedReductions)
      return
    println()
    val  children = WSFI_semantic.reduce(reductionEnvironment, reductionLocal)
    lazyTraverse(children ++ nextReductions.tail, visitedReductions + nextReductions.head)
  end lazyTraverse

  def apply(local: Protocol): Unit = lazyTraverse(WSFI_semantic.reduce(Map(), local), Set())
end NoEncoding