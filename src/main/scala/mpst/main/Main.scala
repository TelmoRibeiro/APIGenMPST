package mpst.main

import mpst.tests.Tests

object Main extends App:
  Tests()

/*  To Do:
    1) prove LocalEnd + clean is enough to assure correct projection
    2) develop show (pretty print)
    3) extend example's list
    4) make "" a functional example
    5) tail recursion
*/

/*  Notes:
    Interactions can be (reasonably) extended in order to allow for Asserted Multiparty Session Types:
    actorA>actorB:l(x: S){A} where
      l     => branch label
      x : S => interaction variable (x) of sort (S)
      A     => assertion
    example: C>A:Login(x: string){tt}
*/