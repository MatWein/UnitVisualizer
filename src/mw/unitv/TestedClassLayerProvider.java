package mw.unitv;

import com.intellij.ide.IconLayerProvider;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.PsiMethod;
import mw.unitv.cfg.PluginConfig;
import mw.unitv.utils.TestClassDetector;
import mw.unitv.utils.TestMethodDetector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class TestedClassLayerProvider implements IconLayerProvider {
	private static final Icon ICON = IconLoader.getIcon("/UnitTested.png");
	
	private static final String LAYER_DESCRIPTION = "Tested class";
	
	@Nullable
	@Override
	public Icon getLayerIcon(@NotNull Iconable iconable, boolean isLocked) {
		try {
			if (iconable instanceof PsiClass) {
				return calculateLayerIcon((PsiClass)iconable);
			} else if (iconable instanceof PsiMethod) {
				return calculateLayerIcon((PsiMethod)iconable);
			}
		} catch (IndexNotReadyException | ProcessCanceledException | PsiInvalidElementAccessException ignored) {}
		
		return null;
	}
	
	@Nullable
	private Icon calculateLayerIcon(@NotNull PsiClass psiClass) {
		Project project = psiClass.getProject();
		
		PluginConfig pluginConfig = PluginConfig.getInstance(project);
		if (pluginConfig == null) {
			return null;
		}
		
		if (!pluginConfig.isUseLayeredIcons()) {
			return null;
		}
		
		PsiClass matchingTestClass = TestClassDetector.findUniqueMatchingTestClass(psiClass);
		if (matchingTestClass != null) {
			return ICON;
		}
		
		return null;
	}

	@Nullable
	private Icon calculateLayerIcon(@NotNull PsiMethod psiMethod) {
		Project project = psiMethod.getProject();

		PluginConfig pluginConfig = PluginConfig.getInstance(project);
		if (pluginConfig == null) {
			return null;
		}

		if (!pluginConfig.isUseLayeredIcons()) {
			return null;
		}

		List<PsiMethod> matchingTestMethods = TestMethodDetector.findMatchingTestMethods(psiMethod);
		if (matchingTestMethods == null || matchingTestMethods.isEmpty()) {
			return null;
		}

		return ICON;
	}
	
	@NotNull
	@Override
	public String getLayerDescription() {
		return LAYER_DESCRIPTION;
	}
}
