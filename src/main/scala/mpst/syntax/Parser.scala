package mpst.syntax

import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers

import mpst.syntax.GlobalType._

object Parser extends RegexParsers:
  override val whiteSpace: Regex = "( |\t|\r|\f|\n|//.*)+".r
  override def skipWhitespace: Boolean = true

  private def identifier: Parser[String] =
    """[a-zA-Z]+""".r

  private def globalType: Parser[GlobalType] =
    maybeParallel ~ opt(choice) ^^ {
      case maybeParallelSyntax ~ Some(choiceSyntax) => choiceSyntax(maybeParallelSyntax)
      case maybeParallelSyntax ~ None               => maybeParallelSyntax
    }

  private def choice: Parser[GlobalType => GlobalType] =
    "+" ~ maybeParallel ~ opt(choice) ^^ {
      case "+" ~ maybeParallelSyntax ~ Some(choiceSyntax) => (globalSyntax: GlobalType) => choiceSyntax(Choice(globalSyntax, maybeParallelSyntax))
      case "+" ~ maybeParallelSyntax ~ None               => (globalSyntax: GlobalType) => Choice(globalSyntax, maybeParallelSyntax)
    }

  private def maybeParallel: Parser[GlobalType] =
    maybeSequence ~ opt(parallel) ^^ {
      case maybeSequenceSyntax ~ Some(parallelSyntax) => parallelSyntax(maybeSequenceSyntax)
      case maybeSequenceSyntax ~ None                 => maybeSequenceSyntax
    }

  private def parallel: Parser[GlobalType => GlobalType] =
    "||" ~ maybeSequence ~ opt(parallel) ^^ {
      case "||" ~ maybeSequenceSyntax ~ Some(parallelSyntax) => (globalSyntax: GlobalType) => parallelSyntax(Parallel(globalSyntax, maybeSequenceSyntax))
      case "||" ~ maybeSequenceSyntax ~ None                 => (globalSyntax: GlobalType) => Parallel(globalSyntax, maybeSequenceSyntax)
    }

  private def maybeSequence: Parser[GlobalType] =
    atomGlobalType ~ opt(sequence) ^^ {
      case atomGlobalTypeSyntax ~ Some(sequenceSyntax) => sequenceSyntax(atomGlobalTypeSyntax)
      case atomGlobalTypeSyntax ~ None                 => atomGlobalTypeSyntax
    }

  private def sequence: Parser[GlobalType => GlobalType] =
    ";" ~ atomGlobalType ~ opt(sequence) ^^ {
      case ";" ~ atomGlobalTypeSyntax ~ Some(sequenceSyntax) => (globalSyntax: GlobalType) => sequenceSyntax(Sequence(globalSyntax, atomGlobalTypeSyntax))
      case ";" ~ atomGlobalTypeSyntax ~ None                 => (globalSyntax: GlobalType) => Sequence(globalSyntax, atomGlobalTypeSyntax)
    }  

  private def atomGlobalType: Parser[GlobalType] =
    recursionFixedPoint | literal | recursionCall

  private def recursionFixedPoint: Parser[GlobalType] =
    "rec" ~ identifier ~ ";" ~ globalType ^^ {
      case "rec" ~ recursionVariableID ~ ";" ~ globalSyntax => RecursionFixedPoint(recursionVariableID, globalSyntax)
    }

  private def recursionCall: Parser[GlobalType] =
    identifier ^^ (
      recursionVariableID => RecursionCall(recursionVariableID)
    )

  private def literal: Parser[GlobalType] =
    parentheses | end | message

  private def parentheses: Parser[GlobalType] =
    "(" ~> globalType <~ ")"

  private def end: Parser[GlobalType] =
    "end" ^^^ End

  private def message: Parser[GlobalType] =
    identifier ~ ">" ~ identifier ~ ":" ~ identifier ^^ {
      case idA ~ ">" ~ idB ~ ":" ~ t => Interaction(idA, idB, t)
    }

  def apply(input: String): ParseResult[GlobalType] = parseAll(globalType, input)
end Parser