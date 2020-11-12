package mw.unitv;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import mw.unitv.cfg.PluginConfig;
import mw.unitv.utils.TestMethodDetector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.List;

public class TestedMethodLineMarkerProvider implements LineMarkerProvider {
    private static final Icon ICON = IconLoader.getIcon("/UnitTestedMethod.png");

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement psiElement) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(
    		@NotNull List<PsiElement> list,
		    @NotNull Collection<LineMarkerInfo> collection) {
    	
        for (PsiElement psiElement : list) {
            LineMarkerInfo lineMarkerInfo = calculateSingleLineMarkerInfo(psiElement);
            if (lineMarkerInfo != null) {
                collection.add(lineMarkerInfo);
            }
        }
    }

    @Nullable
    private LineMarkerInfo calculateSingleLineMarkerInfo(@NotNull PsiElement psiElement) {
        Project project = psiElement.getProject();

        PluginConfig pluginConfig = PluginConfig.getInstance(project);
        if (pluginConfig == null) {
            return null;
        }

        if (!pluginConfig.isUseLayeredIconsOnMethods()) {
            return null;
        }

        if (psiElement instanceof PsiIdentifier && psiElement.getParent() instanceof PsiMethod) {
            PsiMethod method = (PsiMethod) psiElement.getParent();

            List<PsiMethod> matchingTestMethods = TestMethodDetector.findMatchingTestMethods(method);
            if (matchingTestMethods == null || matchingTestMethods.isEmpty()) {
                return null;
            }

            return NavigationGutterIconBuilder
                    .create(ICON)
                    .setTargets(matchingTestMethods)
                    .setTooltipText("Navigate to test method")
                    .createLineMarkerInfo(psiElement);
        }

        return null;
    }
}
