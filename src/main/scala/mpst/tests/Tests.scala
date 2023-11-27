package mpst.tests

import mpst.syntax.Parser
import mpst.projection.Projection

object Tests:
  /*  To Do:
      1) Extend the List
      2) Make "" functional
  */
  private val protocolList: List[String] = List(
    "m>wA:Work ; m>wB:Work ; (wA>m:Done ; end || wB>m:Done ; end)",                                                                                                 // simple master:workers example
    "m>wA:Work ; m>wB:Work ; rec X ; ((wA>m:Done ; end || wB>m:Done ; end) ; Y)",                                                                                   // bad recursion example
    "rec X ; ((m>wA:Work ; m>wB:Work ; (wA>wB:Work ; wB>wA:Done ; (wA>m:Done ; end || wB>m:Done ; end) + wA>wB:None ; (wA>m:Done ; end || wB>m:Done ; end))); X)"   // good recursion example
  )

  def apply(): Unit =
    for protocol <- protocolList yield
      println(s"INPUT: $protocol")
      val global = Parser(protocol)
      println(s"GLOBAL TYPE: $global")
      for local <- Projection(global) yield println(s"LOCAL TYPE: $local")
      println("")
  end apply
end Tests