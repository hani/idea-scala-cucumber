package net.formicary.scala;

import org.jetbrains.plugins.scala.lang.psi.api.expr.ScExpression;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScMethodCall;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScReferenceExpression;

/**
 * @author hani
 *         Date: 4/7/14
 *         Time: 10:34 PM */
public class CucumberScalaUtil {
  
  public static boolean isStepDefinition(ScMethodCall method) {
    ScExpression invokedExpr = method.getInvokedExpr();
    if(invokedExpr instanceof ScReferenceExpression) {
    //todo surely there's a better way to check that these are methods from the DSL rather than just string checks
      if(invokedExpr.getText().equals("When") 
        || invokedExpr.getText().equals("Then") 
        || invokedExpr.getText().equals("And") 
        || invokedExpr.getText().equals("Given")) {
        return true;
      }
    }
    return false;
  }
}
