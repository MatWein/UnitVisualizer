package mw.unitv;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.move.moveClassesOrPackages.MoveJavaFileHandler;
import com.intellij.util.IncorrectOperationException;
import mw.unitv.utils.UniqueClassFromFileDetector;

import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

public class TestClassMoveFileHandler extends MoveJavaFileHandler {
	private Queue<PsiClass> testClassesToMove = new LinkedList<>();
	
	@Override
	public void prepareMovedFile(PsiFile psiFileBeforeMove, PsiDirectory psiDirectory, Map<PsiElement, PsiElement> map) {
		Optional<PsiClass> psiClassBeforeMove = UniqueClassFromFileDetector.getUniqueClass(psiFileBeforeMove);
		
		if (psiClassBeforeMove.isPresent()) {
			TestClassMoveHandler.prepareMove(psiClassBeforeMove.get(), testClassesToMove);
		}
		
		super.prepareMovedFile(psiFileBeforeMove, psiDirectory, map);
	}
	
	@Override
	public void updateMovedFile(PsiFile psiFileAfterMove) throws IncorrectOperationException {
		super.updateMovedFile(psiFileAfterMove);
		
		Optional<PsiClass> psiClassAfterMove = UniqueClassFromFileDetector.getUniqueClass(psiFileAfterMove);
		
		if (psiClassAfterMove.isPresent()) {
			TestClassMoveHandler.finishMoveClass(psiClassAfterMove.get(), testClassesToMove);
		}
	}
}
