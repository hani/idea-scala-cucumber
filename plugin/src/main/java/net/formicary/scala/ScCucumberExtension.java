package net.formicary.scala;

/**
 * @author hani
 *         Date: 4/4/14
 *         Time: 9:32 AM
 */

import java.util.*;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberJvmExtensionPoint;
import org.jetbrains.plugins.cucumber.StepDefinitionCreator;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.psi.GherkinRecursiveElementVisitor;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.cucumber.steps.NotIndexedCucumberExtension;
import org.jetbrains.plugins.scala.ScalaFileType;
import org.jetbrains.plugins.scala.lang.psi.api.ScalaFile;
import org.jetbrains.plugins.scala.lang.psi.api.base.ScLiteral;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScArgumentExprList;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScExpression;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScMethodCall;

public class ScCucumberExtension extends NotIndexedCucumberExtension implements CucumberJvmExtensionPoint {
  @Override
  public boolean isStepLikeFile(@NotNull PsiElement child, @NotNull PsiElement parent) {
    return child instanceof ScalaFile && ((ScalaFile)child).getName().endsWith(".scala");
  }

  @Override
  public boolean isWritableStepLikeFile(@NotNull PsiElement child, @NotNull PsiElement parent) {
    return isStepLikeFile(child, parent);
  }

  @NotNull
  @Override
  public FileType getStepFileType() {
    return ScalaFileType.SCALA_FILE_TYPE;
  }

  @NotNull
  @Override
  public StepDefinitionCreator getStepDefinitionCreator() {
    return new ScStepDefinitionCreator();
  }

  @Nullable
  public String getGlue(@NotNull GherkinStep step) {
    for (PsiReference ref : step.getReferences()) {
      PsiElement refElement = ref.resolve();
      if (refElement != null && refElement instanceof ScMethodCall) {
        ScalaFile groovyFile = (ScalaFile)refElement.getContainingFile();
        VirtualFile vfile = groovyFile.getVirtualFile();
        if (vfile != null) {
          VirtualFile parentDir = vfile.getParent();
          return PathUtil.getLocalPath(parentDir);
        }
      }
    }
    return null;
  }

  @NotNull
  @Override
  public Collection<String> getGlues(@NotNull GherkinFile file, Set<String> gluesFromOtherFiles) {
    if (gluesFromOtherFiles == null) {
      gluesFromOtherFiles = ContainerUtil.newHashSet();
    }
    final Set<String> glues = gluesFromOtherFiles;

    file.accept(new GherkinRecursiveElementVisitor() {
      @Override
      public void visitStep(GherkinStep step) {
        final String glue = getGlue(step);
        if (glue != null) {
          glues.add(glue);
        }
      }
    });

    return glues;
  }

  @Override
  protected void loadStepDefinitionRootsFromLibraries(Module module, List<PsiDirectory> roots, Set<String> directories) {

  }

  @NotNull
  @Override
  public List<AbstractStepDefinition> getStepDefinitions(@NotNull PsiFile psiFile) {
    final List<AbstractStepDefinition> newDefs = new ArrayList<AbstractStepDefinition>();
    if (psiFile instanceof ScalaFile) {
      Collection<ScMethodCall> methodCalls = PsiTreeUtil.findChildrenOfType(psiFile, ScMethodCall.class);
      for(ScMethodCall methodCall : methodCalls) {
        ScExpression invokedExpr = methodCall.getInvokedExpr();
        if(invokedExpr.getText().equals("When") || invokedExpr.getText().equals("Then") || invokedExpr.getText().equals("And") || invokedExpr.getText().equals("Given")) {
          System.out.println(methodCall.getClass() +  " text:" + methodCall.getText() + " invoking " + invokedExpr.getText());
          ScArgumentExprList args = methodCall.args();
          ScExpression[] expressions = args.exprsArray();
          if(expressions.length == 1 && expressions[0] instanceof ScLiteral) {
            newDefs.add(ScStepDefinition.getStepDefinition(methodCall));
          }
        }
      }
    }
    return newDefs;
  }

  @Override
  protected void collectAllStepDefsProviders(@NotNull List<VirtualFile> providers, @NotNull Project project) {
    final Module[] modules = ModuleManager.getInstance(project).getModules();
    for (Module module : modules) {
      if (ModuleType.get(module) instanceof JavaModuleType) {
        final VirtualFile[] roots = ModuleRootManager.getInstance(module).getContentRoots();
        ContainerUtil.addAll(providers, roots);
      }
    }
  }


  @Override
  public void findRelatedStepDefsRoots(@NotNull final Module module, @NotNull final PsiFile featureFile,
                                       List<PsiDirectory> newStepDefinitionsRoots, Set<String> processedStepDirectories) {
    // ToDo: check if inside test folder
    for (VirtualFile sfDirectory : ModuleRootManager.getInstance(module).getSourceRoots()) {
      if (sfDirectory.isDirectory()) {
        PsiDirectory sourceRoot = PsiDirectoryFactory.getInstance(module.getProject()).createDirectory(sfDirectory);
        if (!processedStepDirectories.contains(sourceRoot.getVirtualFile().getPath())) {
          newStepDefinitionsRoots.add(sourceRoot);
        }
      }
    }
  }
}
