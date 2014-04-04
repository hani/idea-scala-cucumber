package scaladefs

import cucumber.api.scala.{EN, ScalaDsl}
import cucumber.api.Scenario
import calculator.RpnCalculator

class CalculatorStepDefs extends ScalaDsl with EN {
  val calc = new RpnCalculator

  When( """^I add (\d+) and (\d+)$""") {
    (arg1: Double, arg2: Double) =>
      calc push arg1
      calc push arg2
      calc push "+"
  }

  Then("^the result is (\\d+)$") {
    expected: Double =>
      assertEquals(expected, calc.value, 0.001)
  }
}
