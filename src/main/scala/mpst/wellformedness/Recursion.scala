package mpst.wellformedness

import mpst.syntax.GlobalType
import mpst.syntax.GlobalType._

object Recursion:
  // reject recursive points without calls
  // reject rec X ; (something ; X + something) ; X ; end
  def apply(local: GlobalType): Boolean = true
end Recursion