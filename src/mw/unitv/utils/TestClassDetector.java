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

public class TestClassDetector {
	private static final String TEST_CLASS_NAME_PATTERN = "%sTest";
	
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
		
		String psiClassName = psiClass.getQualifiedName();
		if (psiClassName == null) {
			return null;
		}
		
		String testClassName = String.format(TEST_CLASS_NAME_PATTERN, psiClassName);
		
		JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
		if (javaPsiFacade == null) {
			return null;
		}
		
		GlobalSearchScope globalSearchScope = GlobalSearchScope.moduleScope(module);
		
		return javaPsiFacade.findClasses(testClassName, globalSearchScope);
	}
}
