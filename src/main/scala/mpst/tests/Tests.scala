package mpst.tests

import mpst.syntax.GlobalType
import mpst.syntax.Parser
import mpst.projection.Projection

object Tests:
  /*  To Do:
      1) Extend the List
      2) Make "" functional
  */
  private val protocolList: List[String] = List(
    "m>wA:Work ; m>wB:Work ; (wA>m:Done ; end || wB>m:Done ; end)",
    "m>wA:Work ; m>wB:Work ; rec X ; ((wA>m:Done ; end || wB>m:Done ; end) ; X)",
    "rec X ; ((m>wA:Work ; m>wB:Work ; (wA>wB:Work ; wB>wA:Done ; (wA>m:Done ; end || wB>m:Done ; end) + wA>wB:None ; (wA>m:Done ; end || wB>m:Done ; end))); X)"
  )

  private def parse(protocol: String): GlobalType =
    Parser(protocol) match
      case Parser.Success(global, _)    => global
      case Parser.NoSuccess(message, _) => throw new RuntimeException(s"Parsing Failed!\n MSG: $message")
  end parse

  def apply(): Unit =
    for protocol <- protocolList yield
      println(s"INPUT: $protocol")
      val global = parse(protocol)
      println(s"GLOBAL TYPE: $global")
      for local <- Projection(global) yield println(s"LOCAL TYPE: $local")
      println("")
  end apply
end Tests