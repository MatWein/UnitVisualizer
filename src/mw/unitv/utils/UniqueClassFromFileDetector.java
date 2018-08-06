package mw.unitv.utils;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;

import java.util.Optional;

public class UniqueClassFromFileDetector {
	public static Optional<PsiClass> getUniqueClass(PsiFile psiFile) {
		if (!(psiFile instanceof PsiJavaFile)) {
			return Optional.empty();
		}
		
		PsiClass[] classes = ((PsiJavaFile) psiFile).getClasses();
		if (classes.length != 1) {
			return Optional.empty();
		}
		
		return Optional.ofNullable(classes[0]);
	}
}
