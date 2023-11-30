package mpst.tests

import mpst.syntax.Parser
import mpst.projection.Projection
import mpst.analysis.WellFormedness

object Tests:
  private val protocolList: List[String] = List(
    "m>wA:Work ; m>wB:Work ; (wA>m:Done ; end || wB>m:Done ; end)",                                                                                                 // master-workers example
    "m>wA:Work ; m>wB:Work ; rec X ; ((wA>m:Done ; end || wB>m:Done ; end) ; Y)",                                                                                   // ill formed: fixed point = X    | call = Y
    "m>wA:Work ; m>wB:Work ; ((wA>m:Done ; end || wB>m:Done ; end) ; X)",                                                                                           // ill formed: fixed point = None | call = X
    "rec X ; ((m>wA:Work ; m>wB:Work ; (wA>wB:Work ; wB>wA:Done ; (wA>m:Done ; end || wB>m:Done ; end) + wA>wB:None ; (wA>m:Done ; end || wB>m:Done ; end))); X)"   // well formed recursion example
  )

  def apply(): Unit =
    for protocol <- protocolList yield
      println(s"INPUT: $protocol")
      val global = Parser(protocol)
      println(s"GLOBAL TYPE: $global")
      for local <- Projection(global) yield
        if WellFormedness(local)
        then println(s"LOCAL TYPE: $local")
        else println(s"LOCAL TYPE: NOT WELL FORMED!")
      println()
  end apply
end Tests