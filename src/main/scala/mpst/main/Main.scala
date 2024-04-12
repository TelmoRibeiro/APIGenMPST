package mpst.main

import mpst.operational_semantic.local_semantic.SyncSemantic
import mpst.syntax.{Parser, Protocol}
import mpst.syntax.Protocol.*
import mpst.projectability.Projectability
import mpst.projection.Projection

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
    val protocol = "def X in (m>wA:Work<void> || m>wB:Work<void>) ; X"
    println(s"PROTOCOL: $protocol")
    val global = Parser(protocol)
    println(s"GLOBAL TYPE: $global")
    var states: Set[State] = Set()
    if !Projectability(global)
    then println(s"PROJECTION REJECTED!")
    else for role <- Protocol.roles(global) yield
      val local = Projection(role, global)
      println(s"LOCAL TYPE ($role): $local")
      println(SyncSemantic.reduce(Map() -> local))
  end main
end Main