package syntax

object Main extends App:
  /*
    1) need to fix operator precedence:
      || > ; and as such, inputA should behave as inputB
    2) need to make end be mandatory at the end of a chain
  */
  private val inputA: String = "m>wA:Work ; m>wB:Work ; (wA>m:Done ; End || wB>m:Done ; End)"
  Parser(inputA) match
    case Parser.Success(ast, _)  => println(s"Parsing Succeeded!\nAST:   $ast")
    case Parser.NoSuccess(msg,_) => println(s"Parsing Failed!   \nError: $msg")
  private val inputB: String = "m>wA:Work ; m>wB:Work ; ((wA>m:Done ; End) || (wB>m:Done ; End))"
  Parser(inputB) match
    case Parser.Success(ast, _) => println(s"Parsing Succeeded!\nAST:   $ast")
    case Parser.NoSuccess(msg, _) => println(s"Parsing Failed!   \nError: $msg")
