package mpst.testing

import mpst.syntax.{Protocol, Parser}
import mpst.projectability.Projectability
import mpst.projection.Projection
import mpst.encoding.NoEncoding

object Tests:
  // Standard Testing //
  private val testList: List[(String, List[String])] =
    List (
      ("STANDARD LIST",
      List (
        // Expected Output: accepted //
        "m>wA:Work ; m>wB:Work ; (wA>m:Done || wB>m:Done)",
      )),
      ("FREE VARIABLES LIST",
        List (  
          // Expected Output: rejected - Y   belongs to FVs //
          "rec X ; (m>wA:Work || m>wB:Done) ; Y",
          // Expected Output: rejected - X   belongs to FVs //
          "(m>wA:Work || m>wB:Work) ; X",
          // Expected Output: rejected - X_2 belongs to FVs //
          "rec X ; (m>wA:Work || m>wB:Work) ; X ; X",
        )),
      ("RACE CONDITIONS",
        List (
          // Expected Output: rejected - possible race condition //
          "rec X ; (m>wA:Work ; X  || m>wB:Work ; X)",
          // Expected Output: accepted //
          "rec X ; (m>wA:Work || m>wB:Work) ; X",
          // Expected Output: accepted //
          "(rec X ; m>wA:Work ; X) || (rec Y ; m>wB:Work ; Y)",
        )),
      ("DISAMBIGUATION",
        List (
          // Expected Output: rejected - "buyer>seller:Msg" in both branches //
          "(broker>buyer:Notify ; buyer>seller:Msg ; seller>buyer:Pay + broker>buyer:Quit ; buyer>seller:Msg)",
          // Expected Output: accepted //
          "(broker>buyer:Notify ; buyer>seller:Price ; seller>buyer:Pay + broker>buyer:Quit ; buyer>seller:Stop)",
        )),
      ("LINEARITY",
        List (
          // Expected Output: rejected - "wB>m:None" in both branches //
          "m>wA:Work ; m>wB:Work ; (wA>m:Done ; wB>m:None || wA>m:None ; wB>m:None)",
        )),
      ("SELF COMMUNICATION",
        List(
          // Expected Output: rejected - no self communication //
          "m>wA:Work ; wA>wA:Done"
        )),
    )
  end testList
  
  private def test(protocolList: List[String]): Unit =
    for protocol <- protocolList yield
      println(s"PROTOCOL: $protocol")
      val global: Protocol = Parser(protocol)
      println(s"GLOBAL TYPE: $global")
      if   !Projectability(global)
      then println(s"PROJECTION REJECTED!")
      else for role <- Protocol.roles(global) yield
        val local: Protocol = Projection(role, global)
        println(s"LOCAL TYPE ($role): $local")
        NoEncoding(local)
        println()
      println()
    println()
    println()
  end test

  def apply(): Unit =
    println("SOME HACKY TESTS:")
    for (_, testCases) <- testList yield
      test(testCases)
  end apply
end Tests