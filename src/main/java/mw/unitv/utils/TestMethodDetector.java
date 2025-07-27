package mw.unitv.utils;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Query;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TestMethodDetector {
    @Nullable
    public static List<PsiMethod> findMatchingTestMethods(PsiMethod method) {
        if (method.isConstructor()) {
            return null;
        }

        PsiClass containingClass = method.getContainingClass();
        if (containingClass == null) {
            return null;
        }

        if (SourceRootDetector.isInTestSourceRoot(containingClass)) {
            return null;
        }

        PsiClass[] matchingTestClasses = TestClassDetector.findMatchingTestClasses(containingClass);
        if (matchingTestClasses == null || matchingTestClasses.length == 0) {
            return null;
        }

        List<PsiMethod> testMethods = new ArrayList<>();

        for (PsiClass matchingTestClass : matchingTestClasses) {
            SearchScope searchScope = new LocalSearchScope(matchingTestClass);
            Query<PsiReference> references = ReferencesSearch.search(method, searchScope);

            for (PsiReference reference : references.findAll()) {
                PsiElement element = reference.getElement();
                PsiMethod callingTestMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
                if (callingTestMethod != null) {
                    testMethods.add(callingTestMethod);
                }
            }
        }

        return testMethods;
    }
}
