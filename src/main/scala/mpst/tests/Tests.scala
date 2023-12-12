package mpst.tests

import mpst.syntax.Parser
import mpst.projection.Projection
import mpst.wellformedness.{FreeVariables, Projectability, Linearity}

object Tests:
  // Standard Testing //
  private val standardList: List[String] = List (
    // Expected Output: accepted //
    "m>wA:Work ; m>wB:Work ; (wA>m:Done || wB>m:Done) ; end",
  )

  // FreeVariables Testing //
  private val FVList: List[String] = List (
    // Expected Output: rejected - Y   belongs to FVs //
    "rec X ; (m>wA:Work || m>wB:Done) ; Y",
    // Expected Output: rejected - X   belongs to FVs //
    "(m>wA:Work || m>wB:Work) ; X",
    // Expected Output: rejected - X_2 belongs to FVs //
    "rec X ; (m>wA:Work || m>wB:Work) ; X ; X",
  )

  // Parallel Race Condition Testing //
  private val RCList: List[String] = List (
    // Expected Output: rejected - possible race condition //
    "rec X ; (m>wA:Work ; X  || m>wB:Work ; X)",
    // Expected Output: accepted //
    "rec X ; (m>wA:Work || m>wB:Work) ; X",
    // Expected Output: accepted //
    "(rec X ; m>wA:Work ; X) || (rec Y ; m>wB:Work ; Y) ; end",
  )

  // Projectability Testing //
  private val PList: List[String] = List (
    // Expected Output: rejected - "buyer>seller:Msg" in both branches //
    "(broker>buyer:Notify ; buyer>seller:Msg ; seller>buyer:Pay + broker>buyer:Quit ; buyer>seller:Msg) ; end",
    // Expected Output: accepted //
    "(broker>buyer:Notify ; buyer>seller:Price ; seller>buyer:Pay + broker>buyer:Quit ; buyer>seller:Stop) ; end",
  )

  // Linearity Testing //
  private val LList: List[String] = List (
    // Expected Output: rejected - "wB>m:None" in both branches //
    "m>wA:Work ; m>wB:Work ; (wA>m:Done ; wB>m:None || wA>m:None ; wB>m:None) ; end",
  )

  // Recursion Testing // NOT DEVELOPED YET
  private val RList: List[String] = List(
    // Expected Output: rejected - fixed point variable unused //
    "rec X ; (m>wA:Work ; m>wB:Work) ; end",
    // Expected Output: rejected - bad fixed point variable reuse (2) //
    "rec X ; (m>wA:Work ; (rec X ; m>wB:Work ; X) ; X)",
    // Expected Output: debatable??? //
    "rec X ; ((m>wA:Work ; X) + m>wB:Work)",
  )

  private def test(protocolList: List[String]): Unit =
    for protocol <- protocolList yield
      println(s"INPUT: $protocol")
      val global = Parser(protocol)
      println(s"GLOBAL TYPE: $global")
      for role <- Projection.roles(global) yield
        val local = Projection(global, role)
        if FreeVariables(local) && Projectability(local) && Linearity(local)
        then println(s"LOCAL TYPE - ROLE $role:\t$local")
        else println(s"LOCAL TYPE - ROLE $role:\tILL FORMED!")
      println()
  end test

  def apply(): Unit =
    println("STANDARD TESTING")
    test(standardList)
    println()
    println("FREE VARIABLES TESTING")
    test(FVList)
    println()
    println("RACE CONDITIONS TESTING")
    test(RCList)
    println()
    println("PROJECTABILITY TESTING")
    test(PList)
    println()
    println("LINEARITY TESTING")
    test(LList)
  end apply
end Tests