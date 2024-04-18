package mpst.main

import mpst.analysis.WellBranched
import mpst.projection.Projection
import mpst.syntax.Parser
import mpst.syntax.Protocol
import mpst.utilities.Types.*

object Main:
  /*
  @tailrec
  private def reductions(pending: List[(Action,State)], trace: List[Action]): List[(Action,State)] =
    if pending.isEmpty then return Nil
    for action -> (environment -> local) <- pending yield
      println(s"action:\t$action")
      println(s"local:\t$local")
      println()
    val index  = readInt()
    val action -> (environment -> local) = pending(index)
    println(s"p_action:\t$action ")
    println(s"p_local:$local")
    println(s"p_trace:${trace ::: List(action)}")
    println()
    val reducePending = SSFI_semantic.reduce(environment, local)
    reductions(reducePending ++ pending.patch(index, Nil, 1), trace ::: List(action))
  end reductions
  */
  def main(args: Array[String]): Unit =
    // Tests()
    // val protocol = "(m>wA:Done<void> + m>wB:Done<void>)" // not accepted by me or oven but accepted by choreo
    // val protocol = "(m>wA:Done<void> + (m>wA:NotDone<void> ; (m>wB:Done<void> + m>wB:NotDone<void>)))" accepted by me but not oven
    // val protocol = "s>b:Descr<void> ; s>b:Price<void> ; (s>b:Acc<void> + s>b:Rej<void>) ; end" // should and it is accepted
    val protocol = "m>wA:Work<void> ; m>wB:Work<void> ; (wA>m:Done<void> ; end || wB>m:Done<void> ; end)" // should and it is accepted
    println(s"PROTOCOL: $protocol")
    val global = Parser(protocol)
    println(s"GLOBAL TYPE: $global")
    var states: Set[State] = Set()
    //if !Projectability(global)
    // then println(s"PROJECTION REJECTED!")
    // else
    WellBranched(global)
    for agent -> local <- Projection.projectionWithAgent(global) yield
      println(s"LOCAL TYPE ($agent): $local")
      // println(s"RENAMED LOCAL TYPE($role): ${RenameRecursion(local)}")
      // val renamedLocal = RenameRecursion(local)
      // NoEncoding(local)
  end main
end Main
