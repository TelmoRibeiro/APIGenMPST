package mpst.utilities

import mpst.syntax.Protocol

object Types:
  // recursion variables //
  type Variable = String
  // communication //
  type Agent   = String
  type Message = String
  type Sort    = String
  // actions //
  type Action = Protocol
  // states //
  type State  = (Map[String,Protocol],Protocol)
end Types