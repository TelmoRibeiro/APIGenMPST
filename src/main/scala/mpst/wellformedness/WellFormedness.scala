package mpst.wellformedness

import mpst.syntax.Protocol

object WellFormedness:
  def apply(global: Protocol): Boolean =
    FreeVariables(global) &&
      Projectability(global) &&
        Linearity(global) &&
            SelfCommunication(global)
end WellFormedness