package mw.unitv;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.impl.AbstractProjectViewPane;
import com.intellij.lang.Language;
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
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaSourceRootType;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class TestClassMoveHandler implements MoveClassHandler {
	private final Map<PsiClass, PsiClass> testClassesToMove = new HashMap<>();
	
	@Override
	public void prepareMove(@NotNull PsiClass psiClassBeforeMove) {
		prepareMove(psiClassBeforeMove, testClassesToMove);
	}
	
	static void prepareMove(@NotNull PsiClass psiClassBeforeMove, Map<PsiClass, PsiClass> testClassesToMove) {
		Project project = psiClassBeforeMove.getProject();
		PluginConfig pluginConfig = PluginConfig.getInstance(project);
		if (pluginConfig == null) {
			return;
		}
		
		if (!pluginConfig.isAutoMoveTestClasses()) {
			return;
		}
		
		PsiClass testClassToMove = TestClassDetector.findUniqueMatchingTestClass(psiClassBeforeMove);
		if (testClassToMove == null) {
			return;
		}

		testClassesToMove.put(psiClassBeforeMove, testClassToMove);
	}
	
	@Override
	public void finishMoveClass(@NotNull PsiClass psiClassAfterMove) {
		finishMoveClass(psiClassAfterMove, testClassesToMove);
	}
	
	static void finishMoveClass(@NotNull PsiClass psiClassAfterMove, Map<PsiClass, PsiClass> testClassesToMove) {
		Project project = psiClassAfterMove.getProject();
		
		PluginConfig pluginConfig = PluginConfig.getInstance(project);
		if (pluginConfig == null) {
			return;
		}
		
		if (!pluginConfig.isAutoMoveTestClasses()) {
			return;
		}
		
		PsiClass testClassToMove = testClassesToMove.get(psiClassAfterMove);
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
			if (packageName.isEmpty()) {
				return;
			}
			
			JavaRefactoringFactory factory = JavaRefactoringFactory.getInstance(project);
			if (factory == null) {
				return;
			}
			
			VirtualFile destinationSourceRoot = findTestSourceRoot(targetModule, testClassToMove);
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
	
	private static VirtualFile findTestSourceRoot(Module targetModule, PsiClass testClassToMove) {
		ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(targetModule);
		if (moduleRootManager == null) {
			return null;
		}

		List<VirtualFile> allSourceRoots = moduleRootManager.getSourceRoots(JavaSourceRootType.TEST_SOURCE);
		return findTestSourceRootRecursive(targetModule, testClassToMove, allSourceRoots);
	}
	
	private static VirtualFile findTestSourceRootRecursive(Module targetModule, PsiClass testClassToMove, List<VirtualFile> allSourceRoots) {
		Set<VirtualFile> dependentTestSourceRoots = new HashSet<>();
		if (allSourceRoots.size() == 1) {
			return allSourceRoots.get(0);
		} else if (allSourceRoots.isEmpty()) {
			List<Module> dependentModules = ModuleUtilCore.getAllDependentModules(targetModule);
			for (Module dependentModule : dependentModules) {
				VirtualFile testSourceRoot = findTestSourceRoot(dependentModule, testClassToMove);
				
				if (testSourceRoot != null) {
					dependentTestSourceRoots.add(testSourceRoot);
				}
			}

			if (dependentTestSourceRoots.size() == 1) {
				return dependentTestSourceRoots.iterator().next();
			}
		} else {
			if (testClassToMove.getLanguage().is(Language.findLanguageByID("JAVA"))) {
				List<VirtualFile> javaSourceRoots = allSourceRoots.stream()
						.filter(sourceRoot -> StringUtils.containsAnyIgnoreCase(sourceRoot.getPath(), "/java", "\\java"))
						.collect(Collectors.toList());
				return findTestSourceRootRecursive(targetModule, testClassToMove, javaSourceRoots);
			} else if (testClassToMove.getLanguage().is(Language.findLanguageByID("Groovy"))) {
				List<VirtualFile> javaSourceRoots = allSourceRoots.stream()
						.filter(sourceRoot -> StringUtils.containsAnyIgnoreCase(sourceRoot.getPath(), "/groovy", "\\groovy"))
						.collect(Collectors.toList());
				return findTestSourceRootRecursive(targetModule, testClassToMove, javaSourceRoots);
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
