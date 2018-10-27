package mw.unitv.utils;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TestMethodDetector {
    @Nullable
    public static List<PsiMethod> findMatchingTestMethods(PsiMethod method) {
        PsiClass containingClass = method.getContainingClass();
        if (containingClass == null) {
            return null;
        }

        PsiClass uniqueMatchingTestClass = TestClassDetector.findUniqueMatchingTestClass(containingClass);
        if (uniqueMatchingTestClass == null) {
            return null;
        }

        String originalMethodName = method.getName().toLowerCase();

        return Arrays.stream(uniqueMatchingTestClass.getAllMethods())
                .filter((m) -> m.getName().toLowerCase().contains(originalMethodName))
                .collect(Collectors.toList());
    }
}
