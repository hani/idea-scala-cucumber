package net.formicary.scala.search;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberUtil;
import org.jetbrains.plugins.cucumber.steps.search.CucumberStepSearchUtil;
import org.jetbrains.plugins.scala.lang.psi.api.base.patterns.ScReferencePattern;

public class ScalaJavaStepDefinitionSearch implements QueryExecutor<PsiReference, ReferencesSearch.SearchParameters> {
  @Override
  public boolean execute(@NotNull final ReferencesSearch.SearchParameters queryParameters, @NotNull final Processor<PsiReference> consumer) {
    final PsiElement myElement = queryParameters.getElementToSearch();
    if (!(myElement instanceof ScReferencePattern)){
      return true;
    }
    final ScReferencePattern method = (ScReferencePattern) myElement;
    Boolean isStepDefinition = ApplicationManager.getApplication().runReadAction(new Computable<Boolean>() {
      @Override
      public Boolean compute() {
        PsiClass containingClass = method.getContainingClass();
        if(containingClass != null) {
          String qualifiedName = containingClass.getQualifiedName();
          return qualifiedName != null && qualifiedName.startsWith("cucumber.api.scala");          
        }
        return false;
      }
    });
    if (!isStepDefinition) {
      return true;
    }

    String regexp = null;
//    final String regexp = CucumberScalaUtil.getPatternFromStepDefinition(method);
    if (regexp == null) {
      return true;
    }
    final String word = CucumberUtil.getTheBiggestWordToSearchByIndex(regexp);
    if (StringUtil.isEmpty(word)) {
      return true;
    }

    final SearchScope searchScope = CucumberStepSearchUtil.restrictScopeToGherkinFiles(new Computable<SearchScope>() {
      public SearchScope compute() {
        return queryParameters.getEffectiveSearchScope();
      }
    });
    // As far as default CacheBasedRefSearcher doesn't look for references in string we have to write out own to handle this correctly
    final TextOccurenceProcessor processor = new TextOccurenceProcessor() {
      @Override
      public boolean execute(PsiElement element, int offsetInElement) {
        PsiElement parent = element.getParent();
        if (parent == null) return true;

        for (PsiReference ref : parent.getReferences()) {
          if (ref != null && ref.isReferenceTo(myElement)) {
            if (!consumer.process(ref)) {
              return false;
            }
          }
        }
        return true;
      }
    };

    short context = UsageSearchContext.IN_STRINGS | UsageSearchContext.IN_CODE;
    return PsiSearchHelper.SERVICE.getInstance(myElement.getProject()).
      processElementsWithWord(processor, searchScope, word, context, true);
  }
}