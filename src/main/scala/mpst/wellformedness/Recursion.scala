package mpst.wellformedness

import mpst.syntax.Protocol
// import mpst.syntax.Protocol._

object Recursion:
  // reject recursive points without calls
  // reject rec X ; (something ; X + something) ; X ; end
  def apply(local: Protocol): Boolean = true
end Recursion