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
import org.fest.util.Lists;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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

        SearchScope searchScope = new LocalSearchScope(uniqueMatchingTestClass);
        Query<PsiReference> references = ReferencesSearch.search(method, searchScope);

        List<PsiMethod> testMethods = Lists.newArrayList();
        for (PsiReference reference : references) {
            PsiElement element = reference.getElement();
            PsiMethod callingTestMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
            if (callingTestMethod != null) {
                testMethods.add(callingTestMethod);
            }
        }

        return testMethods;
    }
}
