package mpst.encoding

import mpst.syntax.Protocol
import mpst.operational_semantics.SSFI_semantic

object NoEncoding:
  // a lazy implementation to transverse the semantics of a local type //
  private def noFormat(environment: Map[String, Protocol], local: Protocol, depth: Integer = 5): Unit =
    if depth <= 0
    then
      println("--------------------\n")
      return
    val nextList: List[(Map[String, Protocol],(Protocol, Protocol))] = SSFI_semantic.reduce(environment, local)
    for nextEnvironment -> (nextLabel -> nextLocal) <- nextList
      yield
        println(s"Label: $nextLabel")
        println(s"Local: $nextLocal")
        println()
        noFormat(nextEnvironment, nextLocal, depth-1)
  end noFormat

  def apply(local: Protocol): Unit = noFormat(Map(), local)
end NoEncoding