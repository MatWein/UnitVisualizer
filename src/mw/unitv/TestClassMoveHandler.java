package mw.unitv;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.impl.AbstractProjectViewPane;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
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
import mw.unitv.utils.TestClassDetector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaSourceRootType;

import java.util.*;

public class TestClassMoveHandler implements MoveClassHandler {
	private Queue<PsiClass> testClassesToMove = new LinkedList<>();
	
	@Override
	public void prepareMove(@NotNull PsiClass psiClassBeforeMove) {
		prepareMove(psiClassBeforeMove, testClassesToMove);
	}
	
	static void prepareMove(@NotNull PsiClass psiClassBeforeMove, Queue<PsiClass> testClassesToMove) {
		Project project = psiClassBeforeMove.getProject();
		PluginConfig pluginConfig = PluginConfig.getInstance(project);
		if (pluginConfig == null) {
			return;
		}
		
		if (!pluginConfig.isAutoMoveTestClasses()) {
			return;
		}
		
		PsiClass testClassToMove = TestClassDetector.findUniqueMatchingTestClass(psiClassBeforeMove);
		testClassesToMove.add(testClassToMove);
	}
	
	@Override
	public void finishMoveClass(@NotNull PsiClass psiClassAfterMove) {
		finishMoveClass(psiClassAfterMove, testClassesToMove);
	}
	
	static void finishMoveClass(@NotNull PsiClass psiClassAfterMove, Queue<PsiClass> testClassesToMove) {
		Project project = psiClassAfterMove.getProject();
		
		PluginConfig pluginConfig = PluginConfig.getInstance(project);
		if (pluginConfig == null) {
			return;
		}
		
		if (!pluginConfig.isAutoMoveTestClasses()) {
			return;
		}
		
		PsiClass testClassToMove = testClassesToMove.poll();
		if (testClassToMove == null) {
			return;
		}
		
		DumbService dumbService = DumbService.getInstance(project);
		if (dumbService == null) {
			return;
		}
		
		dumbService.runWhenSmart(() -> dumbService.smartInvokeLater(() -> {
			Module targetModule = ModuleUtilCore.findModuleForPsiElement(psiClassAfterMove);
			if (targetModule == null) {
				return;
			}
			
			if (!(psiClassAfterMove.getContainingFile() instanceof PsiJavaFile)) {
				return;
			}
			
			Optional<String> packageName = PackageDetector.detectPackage(psiClassAfterMove);
			if (!packageName.isPresent()) {
				return;
			}
			
			JavaRefactoringFactory factory = JavaRefactoringFactory.getInstance(project);
			if (factory == null) {
				return;
			}
			
			VirtualFile destinationSourceRoot = findTestSourceRoot(targetModule);
			if (destinationSourceRoot == null) {
				return;
			}
			
			MoveDestination moveDestination = factory.createSourceRootMoveDestination(packageName.get(), destinationSourceRoot);
			if (moveDestination == null) {
				return;
			}
			
			MoveClassesOrPackagesRefactoring moveClassesOrPackages = factory.createMoveClassesOrPackages(new PsiElement[]{testClassToMove}, moveDestination);
			if (moveClassesOrPackages == null) {
				return;
			}
			
			moveClassesOrPackages.run();
			
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
	
	private static VirtualFile findTestSourceRoot(Module targetModule) {
		ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(targetModule);
		if (moduleRootManager == null) {
			return null;
		}

		List<VirtualFile> allSourceRoots = moduleRootManager.getSourceRoots(JavaSourceRootType.TEST_SOURCE);

		Set<VirtualFile> dependentTestSourceRoots = Sets.newHashSet();
		if (allSourceRoots.size() == 1) {
			return Iterables.getOnlyElement(allSourceRoots);
		} else if (allSourceRoots.isEmpty()) {
			List<Module> dependentModules = ModuleUtilCore.getAllDependentModules(targetModule);
			for (Module dependentModule : dependentModules) {
				dependentTestSourceRoots.add(findTestSourceRoot(dependentModule));
			}

			if (dependentTestSourceRoots.size() == 1) {
				return Iterables.getOnlyElement(dependentTestSourceRoots);
			}
		}

		return null;
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
