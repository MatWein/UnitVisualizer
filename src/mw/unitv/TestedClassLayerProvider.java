package mw.unitv;

import com.intellij.ide.IconLayerProvider;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiInvalidElementAccessException;
import mw.unitv.cfg.PluginConfig;
import mw.unitv.utils.TestClassDetector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TestedClassLayerProvider implements IconLayerProvider, DumbAware {
	private static final Icon ICON = IconLoader.getIcon("/UnitTested.png");
	
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
	
	@NotNull
	@Override
	public String getLayerDescription() {
		return LAYER_DESCRIPTION;
	}
}
