package syntax

import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers

object Parser extends RegexParsers:
  override val whiteSpace: Regex = "( |\t|\r|\f|\n|//.*)+".r
  override def skipWhitespace: Boolean = true

  private def globalType: Parser[GlobalType] =
    (choice | parallel) | sequence | parentheses | message | end

  // identifier is working as a POC, it shall be expanded
  // both actors and globalTypes are currently using identifier
  private def identifier: Parser[String] =
    """[a-zA-Z]+""".r

  private def choice: Parser[Choice] = (end | message | parentheses | parallel | sequence) ~ "+" ~ globalType ^^ {
    case e ~ "+" ~ g  => Choice(e, g)
  }


  private def parallel: Parser[Parallel] = (end | message | sequence | parentheses | choice)  ~ "||" ~ globalType ^^ {
    case e ~ "||" ~ g => Parallel(e, g)
  }

  private def sequence: Parser[Sequence]  = (end | message | parentheses | choice   | parallel) ~ ";"  ~ globalType ^^ {
    case e ~ ";" ~ g  => Sequence(e, g)
  }

  private def parentheses: Parser[GlobalType] =
    "(" ~> globalType <~ ")"

  private def message: Parser[Message]    = identifier ~ ">" ~ identifier ~ ":" ~ identifier ^^ {
    case idA ~ ">" ~ idB ~ ":" ~ t => Message(idA, idB, t)
  }

  private def end: Parser[End.type]       = "End" ^^^ End

  def apply(input: String): ParseResult[GlobalType] = parseAll(globalType, input)
end Parser