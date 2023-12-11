package mpst.tests

import mpst.syntax.Parser
import mpst.projection.Projection
import mpst.wellformedness.{FreeVariables, Projectability}

object Tests:
  private val protocolList: List[String] = List(
    "m>wA:Work ; m>wB:Work ; (wA>m:Done ; end || wB>m:Done ; end)",                                                                                                  // master-workers
    "rec X ; ((m>wA:Work ; m>wB:Work ; (wA>wB:Work ; wB>wA:Done ; (wA>m:Done ; end || wB>m:Done ; end) + wA>wB:None ; (wA>m:Done ; end || wB>m:Done ; end))); X)",   // master-workers recursive

    // FreeVariables Testing //
    "m>wA:Work ; m>wB:Work ; rec X ; ((wA>m:Done ; end || wB>m:Done ; end) ; Y) ; end", // ill formed: Y belongs to FVs
    "m>wA:Work ; m>wB:Work ; ((wA>m:Done ; end || wB>m:Done ; end) ; X) ; end",         // ill formed: X belongs to FVs
    "rec X ; (m>wA:Work ; m>wB:Work) ; X ; X ; end",                                    // ill formed: X_2 belongs to FVs

    // Parallel Race Condition Testing //
    "rec X ; (m>wA:Work ; X  || m>wB:Work ; X) ; end",          // ill  formed: possible parallel race condition
    "rec X ; (m>wA:Work || m>wB:Work) ; X ; end",               // well formed
    "(rec X ; m>wA:Work ; X) || (rec Y ; m>wB:Work ; Y) ; end", // well formed

    // Recursion Testing //
    "rec X ; (m>wA:Work ; m>wB:Work) ; end",                    // ill formed: fixed point variable not used      - Accepting
    "rec X ; (m>wA:Work ; (rec X ; m>wB:Work ; X) ; X) ; end",  // ill formed: bad fixed point variable reuse (2) - Accepting
    "rec X ; ((m>wA:Work ; X) + m>wB:Work) ; end",              // well formed:                                   - Accepting but bad cleanup

    // Projectability Testing //
    "(broker>buyer:Notify ; buyer>seller:Msg ; seller>buyer:Pay + broker>buyer:Quit ; buyer>seller:Msg) ; end",     // ill  formed: buyer>seller:Msg in both branches
    "(broker>buyer:Notify ; buyer>seller:Price ; seller>buyer:Pay + broker>buyer:Quit ; buyer>seller:Stop) ; end",  // well formed

    // TO DO: ill formed: no end in bad position //
  )

  def apply(): Unit =
    for protocol <- protocolList yield
      println(s"INPUT: $protocol")
      val global = Parser(protocol)
      println(s"GLOBAL TYPE: $global")
      for local <- Projection(global) yield
        if   FreeVariables(local) && Projectability(local)
        then println(s"LOCAL TYPE: $local")
        else println(s"LOCAL TYPE: NOT WELL FORMED!")
      println()
  end apply
end Tests