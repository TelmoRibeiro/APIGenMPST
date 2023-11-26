package mpst.tests

import scala.annotation.tailrec

import mpst.syntax.Parser

object ParsingTests:
  private val basicParsing: List[String] = List(
    "m>wA:Work ; m>wB:Work ; (wA>m:Done ; end || wB>m:Done ; end)"
    // need to add more cases //
  )
  private val recursionParsing: List[String] = List(
    "m>wA:Work ; m>wB:Work ; rec X ; ((wA>m:Done ; end || wB>m:Done ; end) ; X)"
    // need to add more cases //
  )
  private val edgeCasesParsing: List[String] = List (
    ""
    // need to add more cases //
  )

  @tailrec
  private def test(parsingList: List[String]): Unit =
    parsingList match
      case Nil          =>
      case head :: tail => Parser(head) match
        case Parser.Success(ast, _)      => println(s"Parsing Succeeded!\nAST:   $ast")
        case Parser.NoSuccess(msg, miss) => println(s"Parsing Failed!   \nError: $msg \nMissing: $miss")
        test(tail)

  def apply(): Unit =
    test(basicParsing)
    test(recursionParsing)
    // test(edgeCasesParsing) // need to make "end" be mandatory at the end of a chain
    println("SUCCESSFULLY CONCLUDED ALL TESTS!")
end ParsingTests