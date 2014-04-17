package net.formicary.scala;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import org.jetbrains.plugins.scala.lang.psi.api.base.patterns.ScReferencePattern;
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
      PsiElement resolved = ((ScReferenceExpression)invokedExpr).resolve();
      if(resolved instanceof ScReferencePattern) {
        return CucumberScalaUtil.isCucumberClass(((ScReferencePattern)resolved).getContainingClass());
      }
    }
    return false;
  }
  
  public static boolean isCucumberClass(PsiClass containingClass) {
    if(containingClass != null) {
      String qualifiedName = containingClass.getQualifiedName();
      return qualifiedName != null && qualifiedName.startsWith("cucumber.api.scala");          
    }
    return false;
  }
}
