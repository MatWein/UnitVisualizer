package mw.unitv;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.move.moveClassesOrPackages.MoveDirectoryWithClassesHelper;
import mw.unitv.utils.UniqueClassFromFileDetector;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TestClassMoveDirectoryHandler extends MoveDirectoryWithClassesHelper.Default {
    private final Multimap<PsiClass, PsiClass> testClassesToMove = ArrayListMultimap.create();

    @Override
    public void beforeMove(PsiFile psiFileBeforeMove) {
        Optional<PsiClass> psiClassBeforeMove = UniqueClassFromFileDetector.getUniqueClass(psiFileBeforeMove);

        if (psiClassBeforeMove.isPresent()) {
            TestClassMoveHandler.prepareMove(psiClassBeforeMove.get(), testClassesToMove);
        }
    }

    @Override
    public void afterMove(PsiElement psiElement) {
        if (psiElement instanceof PsiClass) {
            TestClassMoveHandler.finishMoveClass((PsiClass)psiElement, testClassesToMove);
        }
    }
}
