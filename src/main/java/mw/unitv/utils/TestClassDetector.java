package mw.unitv.utils;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class TestClassDetector {
	private static final String[] TEST_CLASS_NAME_PATTERNS = new String[] { "%sTest", "%sTests" };
	private static final String PACKAGE_CLASS_PATTERN = "%s.%s";

	@Nullable
	public static PsiClass findUniqueMatchingTestClass(@NotNull PsiClass psiClass) {
		PsiClass[] matchingClasses = findMatchingTestClasses(psiClass);
		if (matchingClasses != null && matchingClasses.length == 1) {
			return matchingClasses[0];
		}
		
		return null;
	}
	
	@Nullable
	public static PsiClass[] findMatchingTestClasses(@NotNull PsiClass psiClass) {
		Project project = psiClass.getProject();
		
		DumbService dumbService = DumbService.getInstance(project);
		if (dumbService == null || dumbService.isDumb()) {
			return null;
		}
		
		Module module = ModuleUtilCore.findModuleForPsiElement(psiClass);
		if (module == null) {
			return null;
		}
		
		Optional<String> sourcePackage = PackageDetector.detectPackage(psiClass);
		if (!sourcePackage.isPresent()) {
			return null;
		}
		
		String psiClassName = String.format(PACKAGE_CLASS_PATTERN, sourcePackage.get(), psiClass.getName());
		final Stream<String> testClassNames = Arrays.stream(TEST_CLASS_NAME_PATTERNS)
												  .map(pattern -> String.format(pattern, psiClassName));

		JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
		if (javaPsiFacade == null) {
			return null;
		}
		
		GlobalSearchScope globalSearchScope = GlobalSearchScope.moduleTestsWithDependentsScope(module);
		return testClassNames.flatMap(testClassName -> Arrays.stream(javaPsiFacade.findClasses(testClassName, globalSearchScope)))
							 .toArray(PsiClass[]::new);
	}
}
