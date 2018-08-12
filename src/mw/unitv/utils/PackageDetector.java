package mw.unitv.utils;

import com.intellij.psi.*;

import java.util.Optional;

public class PackageDetector {
	public static Optional<String> detectPackage(PsiClass psiClass) {
		JavaDirectoryService directoryService = JavaDirectoryService.getInstance();
		if (directoryService == null) {
			return Optional.empty();
		}

		PsiFile containingFile = psiClass.getContainingFile();
		if (containingFile == null) {
			return Optional.empty();
		}

		PsiDirectory containingDirectory = containingFile.getContainingDirectory();
		if (containingDirectory == null) {
			return Optional.empty();
		}

		PsiPackage sourcePackage = directoryService.getPackage(containingDirectory);
		if (sourcePackage == null) {
			return Optional.empty();
		}
		
		return Optional.of(sourcePackage.getQualifiedName());
	}
}
