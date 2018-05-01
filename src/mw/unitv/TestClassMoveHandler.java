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
import mw.unitv.utils.TestClassDetector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class TestClassMoveHandler implements MoveClassHandler {
	private Queue<PsiClass> testClassesToMove = new LinkedList<>();
	
	@Override
	public void prepareMove(@NotNull PsiClass psiClassBeforeMove) {
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
			
			PsiJavaFile containingFile = (PsiJavaFile) psiClassAfterMove.getContainingFile();
			String packageName = containingFile.getPackageName();
			
			JavaRefactoringFactory factory = JavaRefactoringFactory.getInstance(project);
			if (factory == null) {
				return;
			}
			
			VirtualFile destinationSourceRoot = findTestSourceRoot(targetModule);
			if (destinationSourceRoot == null) {
				return;
			}
			
			MoveDestination moveDestination = factory.createSourceRootMoveDestination(packageName, destinationSourceRoot);
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
	
	private VirtualFile findTestSourceRoot(Module targetModule) {
		Set<VirtualFile> allSourceRoots = Sets.newHashSet(ModuleRootManager.getInstance(targetModule).getSourceRoots(true));
		Set<VirtualFile> sourceRoots = Sets.newHashSet(ModuleRootManager.getInstance(targetModule).getSourceRoots(false));
		Set<VirtualFile> testSourceRoots = Sets.difference(allSourceRoots, sourceRoots);
		
		return Iterables.getFirst(testSourceRoots, null);
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
