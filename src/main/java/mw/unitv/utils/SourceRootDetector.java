package mw.unitv.utils;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;

public class SourceRootDetector {
    public static VirtualFile findTestSourceRoot(Module targetModule, PsiClass testClassToMove) {
        if (targetModule == null || testClassToMove == null) {
            return null;
        }

        PsiFile psiFile = testClassToMove.getContainingFile();
        if (psiFile == null) {
            return null;
        }

        VirtualFile classFile = psiFile.getVirtualFile();
        if (classFile == null) {
            return null;
        }

        ProjectFileIndex fileIndex = ProjectFileIndex.getInstance(targetModule.getProject());
        if (fileIndex == null) {
            return null;
        }

        ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(targetModule);
        if (moduleRootManager == null) {
            return null;
        }

        for (VirtualFile root : moduleRootManager.getSourceRoots(true)) {
            if (classFile.getPath().startsWith(root.getPath())) {
                return root;
            }
        }

        return null;
    }

    public static boolean isInTestSourceRoot(PsiClass psiClass) {
        PsiFile psiFile = psiClass.getContainingFile();
        if (psiFile == null) {
            return false;
        }

        VirtualFile virtualFile = psiFile.getVirtualFile();
        if (virtualFile == null) {
            return false;
        }

        Project project = psiClass.getProject();
        if (project == null) {
            return false;
        }

        ProjectFileIndex projectFileIndex = ProjectFileIndex.getInstance(project);
        if (projectFileIndex == null) {
            return false;
        }

        return projectFileIndex.isInTestSourceContent(virtualFile);
    }
}
