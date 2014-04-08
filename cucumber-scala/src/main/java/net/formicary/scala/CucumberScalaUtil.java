package net.formicary.scala;

import org.jetbrains.plugins.scala.lang.psi.api.base.ScLiteral;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScArgumentExprList;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScExpression;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScMethodCall;

/**
 * @author hani
 *         Date: 4/7/14
 *         Time: 10:34 PM
 */
public class CucumberScalaUtil {
  
  public static boolean isStepDefinition(ScMethodCall method) {
    ScExpression invokedExpr = method.getInvokedExpr();
    //todo surely there's a better way to check that these are methods from the DSL rather than just string checks
    if(invokedExpr.getText().equals("When") || invokedExpr.getText().equals("Then") || invokedExpr.getText().equals("And") || invokedExpr.getText().equals("Given")) {
      return true;
    } else {
      return false;
    }
  }

  public static String getPatternFromStepDefinition(ScMethodCall method) {
    ScArgumentExprList args = method.args();
    ScExpression[] expressions = args.exprsArray();
    if(expressions.length == 1 && expressions[0] instanceof ScLiteral) {
      return (String)((ScLiteral)expressions[0]).getValue();
    }
    return null;
  }
}
