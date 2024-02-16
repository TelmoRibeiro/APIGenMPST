package mpst.encoding

import mpst.syntax.Protocol
// import mpst.operational_semantic.SSFI_semantic
import mpst.operational_semantic.WSFI_semantic

object NoEncoding:
  // lazy implementation to traverse the semantic output //
  private def lazyTraverse(nextStates: List[(Map[String,Protocol],(Protocol,Protocol))], visitedStates: Set[(Map[String,Protocol],(Protocol,Protocol))]): Unit =
    if nextStates.isEmpty then
      println("done traversing")
      return
    val state = nextStates.head
    val stateEnvironment -> (stateAction -> stateLocal) = state
    println(s"Action: $stateAction")
    println(s"Local:  $stateLocal")
    if visitedStates contains state then
      println("state was visited already\n")
      lazyTraverse(nextStates.tail, visitedStates)
      return
    println()
    val  children = WSFI_semantic.reduce(stateEnvironment, stateLocal)
    lazyTraverse(children ++ nextStates.tail, visitedStates + nextStates.head)
  end lazyTraverse

  def apply(local: Protocol): Unit = lazyTraverse(WSFI_semantic.reduce(Map(), local), Set())
end NoEncoding