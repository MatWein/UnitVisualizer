package mw.unitv;

import com.intellij.ide.IconLayerProvider;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TestedClassLayerProvider implements IconLayerProvider, DumbAware {
	private static final Icon ICON = IconLoader.getIcon("/UnitTested.png");
	
	private static final String TEST_CLASS_NAME_PATTERN = "%sTest";
	private static final String LAYER_DESCRIPTION = "Tested class";
	
	@Nullable
	@Override
	public Icon getLayerIcon(@NotNull Iconable iconable, boolean isLocked) {
		try {
			if (iconable instanceof PsiClass) {
				return calculateLayerIcon((PsiClass)iconable);
			}
		} catch (IndexNotReadyException | ProcessCanceledException | PsiInvalidElementAccessException ignored) {}
		
		return null;
	}
	
	@Nullable
	private Icon calculateLayerIcon(@NotNull PsiClass psiClass) {
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
		
		PsiClass[] matchingClasses = javaPsiFacade.findClasses(testClassName, globalSearchScope);
		if (matchingClasses.length > 0) {
			return ICON;
		}
		
		return null;
	}
	
	@NotNull
	@Override
	public String getLayerDescription() {
		return LAYER_DESCRIPTION;
	}
}
