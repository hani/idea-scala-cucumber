package net.formicary.scala;

import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.impl.DirectoryIndex;
import com.intellij.openapi.roots.impl.DirectoryInfo;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.StepDefinitionCreator;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;
import org.jetbrains.plugins.cucumber.java.steps.JavaStepDefinitionCreator;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.scala.lang.psi.api.ScalaFile;

/**
 * @author hani
 *         Date: 4/4/14
 *         Time: 9:55 AM
 */
public class ScStepDefinitionCreator implements StepDefinitionCreator {
  @NotNull
  @Override
  public PsiFile createStepDefinitionContainer(@NotNull PsiDirectory psiDirectory, @NotNull String s) {
    return null;
  }

  @Override
  public boolean createStepDefinition(@NotNull GherkinStep gherkinStep, @NotNull PsiFile psiFile) {
    return false;
  }

  @Override
  public boolean validateNewStepDefinitionFileName(@NotNull Project project, @NotNull String name) {
    if(name.length() == 0) return false;
    if (! Character.isJavaIdentifierStart(name.charAt(0))) return false;
    for (int i = 1; i < name.length(); i++) {
      if (! Character.isJavaIdentifierPart(name.charAt(i))) return false;
    }
    return true;
  }

  @NotNull
  @Override
  public PsiDirectory getDefaultStepDefinitionFolder(@NotNull final GherkinStep step) {
    PsiFile featureFile = step.getContainingFile();
    if (featureFile != null) {
      PsiDirectory directory = featureFile.getContainingDirectory();
      if (directory != null && directory.getManager() != null) {
        PsiManager manager = directory.getManager();
        DirectoryIndex directoryIndex = DirectoryIndex.getInstance(manager.getProject());
        DirectoryInfo info = directoryIndex.getInfoForDirectory(directory.getVirtualFile());
        if (info != null) {
          VirtualFile sourceRoot = info.getSourceRoot();
          //noinspection ConstantConditions
          final Module module = ProjectRootManager.getInstance(step.getProject()).getFileIndex().getModuleForFile(featureFile.getVirtualFile());
          if (module != null) {
            final VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();
            if (sourceRoot != null && sourceRoot.getName().equals("resources")) {
              final VirtualFile resourceParent = sourceRoot.getParent();
              for (VirtualFile vFile : sourceRoots) {
                if (vFile.getPath().startsWith(resourceParent.getPath()) && vFile.getName().equals("scala")) {
                  sourceRoot = vFile;
                  break;
                }
              }
            }
            else {
              if (sourceRoots.length > 0) {
                sourceRoot = sourceRoots[sourceRoots.length - 1];
              }
            }
          }
          String packageName = "";
          if (sourceRoot != null) {
            packageName = CucumberJavaUtil.getPackageOfStepDef(step);
          }

          final String packagePath = packageName.replace('.', '/');
          final String path = sourceRoot != null ? sourceRoot.getPath() : directory.getVirtualFile().getPath();
          // ToDo: I shouldn't create directories, only create VirtualFile object.
          final Ref<PsiDirectory> resultRef = new Ref<PsiDirectory>();
          new WriteAction() {
            protected void run(@NotNull Result result) throws Throwable {
              final VirtualFile packageFile = VfsUtil.createDirectoryIfMissing(path + '/' + packagePath);
              if (packageFile != null) {
                resultRef.set(PsiDirectoryFactory.getInstance(step.getProject()).createDirectory(packageFile));
              }
            }
          }.execute();
          return resultRef.get();
        }
      }
    }

    assert featureFile != null;
    return ObjectUtils.assertNotNull(featureFile.getParent());
  }

  @NotNull
  @Override
  public String getStepDefinitionFilePath(@NotNull PsiFile psiFile) {
    final VirtualFile vFile = psiFile.getVirtualFile();
    if (psiFile instanceof ScalaFile && vFile != null) {
      String packageName = ((ScalaFile)psiFile).getPackageName();
      if (StringUtil.isEmptyOrSpaces(packageName)) {
        return vFile.getNameWithoutExtension();
      }
      else {
        return vFile.getNameWithoutExtension() + " (" + packageName + ")";
      }
    }
    return psiFile.getName();
  }

  @NotNull
  @Override
  public String getDefaultStepFileName() {
    return JavaStepDefinitionCreator.STEP_DEFINITION_SUFFIX;
  }
}
