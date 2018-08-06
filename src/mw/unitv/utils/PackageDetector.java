package mw.unitv.utils;

import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiPackage;

import java.util.Optional;

public class PackageDetector {
	public static Optional<String> detectPackage(PsiClass psiClass) {
		JavaDirectoryService directoryService = JavaDirectoryService.getInstance();
		if (directoryService == null) {
			return Optional.empty();
		}
		
		PsiDirectory containingDirectory = psiClass.getContainingFile().getContainingDirectory();
		PsiPackage sourcePackage = directoryService.getPackage(containingDirectory);
		if (sourcePackage == null) {
			return Optional.empty();
		}
		
		return Optional.ofNullable(sourcePackage.getName());
	}
}
