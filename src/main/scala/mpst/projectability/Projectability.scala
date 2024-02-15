package mpst.projectability

import mpst.syntax.Protocol

object Projectability:
  def apply(global: Protocol): Boolean =
    FreeVariables(global) &&
      Disambiguation(global) &&
        Linearity(global) &&
            SelfCommunication(global)
  end apply
end Projectability