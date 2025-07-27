package mw.unitv;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.impl.AbstractProjectViewPane;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.refactoring.JavaRefactoringFactory;
import com.intellij.refactoring.MoveClassesOrPackagesRefactoring;
import com.intellij.refactoring.MoveDestination;
import com.intellij.refactoring.move.moveClassesOrPackages.MoveClassHandler;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.IncorrectOperationException;
import mw.unitv.cfg.PluginConfig;
import mw.unitv.utils.PackageDetector;
import mw.unitv.utils.SourceRootDetector;
import mw.unitv.utils.TestClassDetector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

public class TestClassMoveHandler implements MoveClassHandler {
	private final Multimap<PsiClass, PsiClass> testClassesToMove = ArrayListMultimap.create();
	
	@Override
	public void prepareMove(@NotNull PsiClass psiClassBeforeMove) {
		prepareMove(psiClassBeforeMove, testClassesToMove);
	}
	
	static void prepareMove(@NotNull PsiClass psiClassBeforeMove, Multimap<PsiClass, PsiClass> testClassesToMove) {
		Project project = psiClassBeforeMove.getProject();
		PluginConfig pluginConfig = PluginConfig.getInstance(project);
		if (pluginConfig == null) {
			return;
		}
		
		if (!pluginConfig.isAutoMoveTestClasses()) {
			return;
		}
		
		PsiClass[] foundTestClassesToMove = TestClassDetector.findMatchingTestClasses(psiClassBeforeMove);
		if (foundTestClassesToMove == null || foundTestClassesToMove.length == 0) {
			return;
		}

		for (PsiClass testClassToMove : foundTestClassesToMove) {
			testClassesToMove.put(psiClassBeforeMove, testClassToMove);
		}
	}
	
	@Override
	public void finishMoveClass(@NotNull PsiClass psiClassAfterMove) {
		finishMoveClass(psiClassAfterMove, testClassesToMove);
	}
	
	static void finishMoveClass(@NotNull PsiClass psiClassAfterMove, Multimap<PsiClass, PsiClass> testClassesToMove) {
		Project project = psiClassAfterMove.getProject();
		
		PluginConfig pluginConfig = PluginConfig.getInstance(project);
		if (pluginConfig == null) {
			return;
		}
		
		if (!pluginConfig.isAutoMoveTestClasses()) {
			return;
		}
		
		Collection<PsiClass> foundTestClassesToMove = testClassesToMove.get(psiClassAfterMove);
		if (foundTestClassesToMove == null || foundTestClassesToMove.isEmpty()) {
			return;
		}
		
		DumbService dumbService = DumbService.getInstance(project);
		if (dumbService == null) {
			return;
		}

		dumbService.runWhenSmart(() -> ApplicationManager.getApplication().invokeLater(() -> {
            Module targetModule = ModuleUtilCore.findModuleForPsiElement(psiClassAfterMove);
            if (targetModule == null) {
                return;
            }

            if (!(psiClassAfterMove.getContainingFile() instanceof PsiJavaFile)) {
                return;
            }

            Optional<String> packageName = PackageDetector.detectPackage(psiClassAfterMove);
            if (packageName.isEmpty()) {
                return;
            }

            JavaRefactoringFactory factory = JavaRefactoringFactory.getInstance(project);
            if (factory == null) {
                return;
            }

            for (PsiClass testClassToMove : foundTestClassesToMove) {
                VirtualFile destinationSourceRoot = SourceRootDetector.findTestSourceRoot(targetModule, testClassToMove);
                if (destinationSourceRoot == null) {
                    continue;
                }

                MoveDestination moveDestination = factory.createSourceRootMoveDestination(packageName.get(), destinationSourceRoot);
                if (moveDestination == null) {
                    continue;
                }

                MoveClassesOrPackagesRefactoring moveClassesOrPackages = factory.createMoveClassesOrPackages(new PsiElement[]{testClassToMove}, moveDestination);
                if (moveClassesOrPackages == null) {
                    continue;
                }

                moveClassesOrPackages.run();
            }

            ProjectView projectView = ProjectView.getInstance(project);
            if (projectView == null) {
                return;
            }

            AbstractProjectViewPane currentProjectViewPane = projectView.getCurrentProjectViewPane();
            if (currentProjectViewPane == null) {
                return;
            }

            currentProjectViewPane.updateFrom(psiClassAfterMove, false, true);
        }));
	}
	
	@Nullable
	@Override
	public PsiClass doMoveClass(@NotNull PsiClass psiClass, @NotNull PsiDirectory psiDirectory) throws IncorrectOperationException {
		return null;
	}
	
	@Override
	public String getName(PsiClass psiClass) {
		return null;
	}
	
	@Override
	public void preprocessUsages(Collection<UsageInfo> collection) { }
}
