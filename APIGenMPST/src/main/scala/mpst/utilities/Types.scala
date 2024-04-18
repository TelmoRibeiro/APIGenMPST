package mpst.utilities

import mpst.syntax.Protocol

object Types:
  // recursion variables //
  type Variable = String
  // agent/roles //
  type Agent   = String
  type Message = String
  // actions //
  type Action = Protocol
  // states //
  type State  = (Map[String,Protocol],Protocol)
end Types