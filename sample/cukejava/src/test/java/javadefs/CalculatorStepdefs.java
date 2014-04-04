package javadefs;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import static org.junit.Assert.assertEquals;

public class CalculatorStepdefs {

    private int result = 0;

    @When("^I add (\\d+) and (\\d+)$")
    public void addTwoNumbers(int arg1, int arg2) throws Throwable {
        result = arg1 + arg2;
    }

    @Then("^the result is (\\d+)$")
    public void assertResult(int expected) throws Throwable {
        assertEquals(expected, result);
    }
}
