package net.formicary.scala;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.intellij.ide.util.EditSourceUtil;
import com.intellij.openapi.editor.Document;
import com.intellij.pom.Navigatable;
import com.intellij.pom.PomNamedTarget;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.scala.lang.psi.api.base.ScLiteral;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScArgumentExprList;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScExpression;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScMethodCall;

/**
 * @author hani
 *         Date: 4/4/14
 *         Time: 9:57 AM
 */
public class ScStepDefinition extends AbstractStepDefinition implements PomNamedTarget {
  public ScStepDefinition(ScMethodCall stepDefinition) {
    super(stepDefinition);
  }

  public static ScStepDefinition getStepDefinition(final ScMethodCall statement) {
    return CachedValuesManager.getCachedValue(statement, new CachedValueProvider<ScStepDefinition>() {
      @Nullable
      @Override
      public Result<ScStepDefinition> compute() {
        final Document document = PsiDocumentManager.getInstance(statement.getProject()).getDocument(statement.getContainingFile());
        return Result.create(new ScStepDefinition(statement), document);
      }
    });
  }

  @Override
  public List<String> getVariableNames() {
    PsiElement element = getElement();
    if (element instanceof ScMethodCall) {
      ScArgumentExprList args = ((ScMethodCall)element).args();
      ScExpression[] parameters = args.exprsArray();
      ArrayList<String> result = new ArrayList<String>();
      for (ScExpression parameter : parameters) {
        result.add(parameter.getText());
      }

      return result;
    }
    return Collections.emptyList();
  }

  @Nullable
  @Override
  protected String getCucumberRegexFromElement(PsiElement element) {
    if (!(element instanceof ScMethodCall)) {
      return null;
    }
    ScLiteral literal = (ScLiteral)((ScMethodCall)element).args().exprsArray()[0];
    return literal.getValue().toString().replace("\\\\", "\\").replace("\\\"", "\"");
  }

  @Override
  public String getName() {
    return getCucumberRegex();
  }

  @Override
  public boolean isValid() {
    final PsiElement element = getElement();
    return element != null && element.isValid();
  }

  @Override
  public void navigate(boolean requestFocus) {
    Navigatable descr = EditSourceUtil.getDescriptor(getElement());
    if (descr != null) descr.navigate(requestFocus);
  }

  @Override
  public boolean canNavigate() {
    return EditSourceUtil.canNavigate(getElement());
  }

  @Override
  public boolean canNavigateToSource() {
    return canNavigate();
  }}
