package mpst.tests

import mpst.syntax.GlobalType
import mpst.syntax.Parser
import mpst.projection.Projection

object FullTests:
  /*  To Do:
      1) Extend the List
      2) Make "" functional
   */
  private val exampleList: List[String] = List(
    "m>wA:Work ; m>wB:Work ; (wA>m:Done ; end || wB>m:Done ; end)",
    "m>wA:Work ; m>wB:Work ; rec X ; ((wA>m:Done ; end || wB>m:Done ; end) ; X)"
  )
  
  private def parse(protocol: String): GlobalType =
    Parser(protocol) match  
      case Parser.Success(global, _) => global
      case Parser.NoSuccess(_, _)    => throw new RuntimeException("Parsing Failed!")

  def apply(): Unit =
    for example <- exampleList yield
      println(s"INPUT: $example")
      val global = parse(example)
      println(s"GLOBAL TYPE: $global")
      for local <- Projection(global) yield println(s"LOCAL TYPE: $local")
      println("")
end FullTests