package mpst.tests

import mpst.syntax.{Protocol, Parser}
import mpst.projection.Projection
import mpst.wellformedness.WellFormedness

object Tests:
  // Standard Testing //
  private val standardList: List[String] = List (
    // Expected Output: accepted //
    "m>wA:Work ; m>wB:Work ; (wA>m:Done || wB>m:Done)",
  )

  // FreeVariables Testing //
  private val freeVariablesList: List[String] = List (
    // Expected Output: rejected - Y   belongs to FVs //
    "rec X ; (m>wA:Work || m>wB:Done) ; Y",
    // Expected Output: rejected - X   belongs to FVs //
    "(m>wA:Work || m>wB:Work) ; X",
    // Expected Output: rejected - X_2 belongs to FVs //
    "rec X ; (m>wA:Work || m>wB:Work) ; X ; X",
  )

  // Parallel Race Condition Testing //
  private val raceConditionsList: List[String] = List (
    // Expected Output: rejected - possible race condition //
    "rec X ; (m>wA:Work ; X  || m>wB:Work ; X)",
    // Expected Output: accepted //
    "rec X ; (m>wA:Work || m>wB:Work) ; X",
    // Expected Output: accepted //
    "(rec X ; m>wA:Work ; X) || (rec Y ; m>wB:Work ; Y)",
  )

  // Projectability Testing //
  private val projectabilityList: List[String] = List (
    // Expected Output: rejected - "buyer>seller:Msg" in both branches //
    "(broker>buyer:Notify ; buyer>seller:Msg ; seller>buyer:Pay + broker>buyer:Quit ; buyer>seller:Msg)",
    // Expected Output: accepted //
    "(broker>buyer:Notify ; buyer>seller:Price ; seller>buyer:Pay + broker>buyer:Quit ; buyer>seller:Stop)",
  )

  // Linearity Testing //
  private val linearityList: List[String] = List (
    // Expected Output: rejected - "wB>m:None" in both branches //
    "m>wA:Work ; m>wB:Work ; (wA>m:Done ; wB>m:None || wA>m:None ; wB>m:None)",
  )

  // Self Communication Testing //
  private val selfCommunicationList: List[String] = List (
    // Expected Output: rejected - no self communication //
    "m>wA:Work ; wA>wA:Done"
  )

  private def test(protocolList: List[String]): Unit =
    for protocol <- protocolList yield
      println(s"PROTOCOL: $protocol")
      val global: Protocol = Parser(protocol)
      println(s"GLOBAL TYPE: $global")
      if   !WellFormedness(global)
      then println(s"PROJECTION REJECTED!")
      else for role <- Protocol.roles(global) yield
        val local: Protocol = Projection(global, role)
        println(s"LOCAL TYPE ($role): $local")
      println()
    println()
    println()
  end test

  def apply(): Unit =
    println("SOME HACKY TESTS:")
    test(standardList)
    test(freeVariablesList)
    test(raceConditionsList)
    test(projectabilityList)
    test(linearityList)
    test(selfCommunicationList)
  end apply
end Tests