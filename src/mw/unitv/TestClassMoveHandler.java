package mw.unitv;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
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

public class TestClassMoveHandler implements MoveClassHandler, DumbAware {
	private Queue<PsiClass> testClassesToMove = new LinkedList<>();
	
	@Override
	public void prepareMove(@NotNull PsiClass psiClass) {
		Project project = psiClass.getProject();
		PluginConfig pluginConfig = PluginConfig.getInstance(project);
		if (pluginConfig == null) {
			return;
		}
		
		if (!pluginConfig.isAutoMoveTestClasses()) {
			return;
		}
		
		PsiClass testClassToMove = TestClassDetector.findUniqueMatchingTestClass(psiClass);
		testClassesToMove.add(testClassToMove);
	}
	
	@Override
	public void finishMoveClass(@NotNull PsiClass psiClass) {
		Project project = psiClass.getProject();
		
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
		
		Module targetModule = ModuleUtilCore.findModuleForPsiElement(psiClass);
		if (targetModule == null) {
			return;
		}
		
		if (!(psiClass.getContainingFile() instanceof PsiJavaFile)) {
			return;
		}
		
		PsiJavaFile containingFile = (PsiJavaFile)psiClass.getContainingFile();
		String packageName = containingFile.getPackageName();
		
		JavaRefactoringFactory factory = JavaRefactoringFactory.getInstance(project);
		if (factory == null) {
			return;
		}
		
		VirtualFile destinationSourceRoot = findTestSourceRoot(targetModule, testClassToMove);
		if (destinationSourceRoot == null) {
			return;
		}
		
		MoveDestination moveDestination = factory.createSourceRootMoveDestination(packageName, destinationSourceRoot);
		if (moveDestination == null) {
			return;
		}
		
		MoveClassesOrPackagesRefactoring moveClassesOrPackages = factory.createMoveClassesOrPackages(new PsiElement[] { testClassToMove }, moveDestination);
		if (moveClassesOrPackages == null) {
			return;
		}
		
		DumbService.getInstance(project).smartInvokeLater(moveClassesOrPackages::run);
	}
	
	private VirtualFile findTestSourceRoot(Module targetModule, PsiClass testClassToMove) {
		VirtualFile[] sourceRoots = ModuleRootManager.getInstance(targetModule).getSourceRoots();
		for (VirtualFile sourceRoot : sourceRoots) {
			PsiFile containingFile = testClassToMove.getContainingFile();
			if (containingFile == null) {
				continue;
			}
			
			PsiDirectory containingDirectory = containingFile.getContainingDirectory();
			if (containingDirectory == null) {
				continue;
			}
			
			VirtualFile virtualFile = containingDirectory.getVirtualFile();
			
			String canonicalPath = virtualFile.getCanonicalPath();
			if (canonicalPath == null) {
				continue;
			}
			
			String sourceRootCanonicalPath = sourceRoot.getCanonicalPath();
			if (sourceRootCanonicalPath == null) {
				continue;
			}
			
			if (canonicalPath.startsWith(sourceRootCanonicalPath)) {
				return sourceRoot;
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
	public void preprocessUsages(Collection<UsageInfo> collection) {
	}
}
