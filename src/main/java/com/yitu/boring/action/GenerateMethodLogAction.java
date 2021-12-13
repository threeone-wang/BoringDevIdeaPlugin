package com.yitu.boring.action;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.util.IncorrectOperationException;

/**
 * author : ⚡️
 * description :
 * date : Created in 2021/12/8 23:41
 * modified : 💧💨🔥
 */
public class GenerateMethodLogAction extends PsiElementBaseIntentionAction {

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return "boring-dev-tool";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        if (element.getParent() instanceof PsiMethod){
            return true;
        }
        return false;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        Document document = psiDocumentManager.getDocument(element.getContainingFile());
        PsiMethod psiMethod = (PsiMethod) element.getParent();
        String prefixSpaceText = generatePrefixSpaceText(psiMethod, document);
        String logFormat="log.info(\"start "+psiMethod.getName();
        StringBuilder logFormatSb=new StringBuilder(logFormat);
        String parameterNameFormat=",%s:[{}]";
        PsiParameterList parameterList = psiMethod.getParameterList();
        PsiParameter[] parameters = parameterList.getParameters();
        for (PsiParameter parameter : parameters) {
            logFormatSb.append(String.format(parameterNameFormat,parameter.getName()));
        }
        logFormatSb.append("\"");
        for (PsiParameter parameter : parameters) {
            logFormatSb.append(",").append(parameter.getName());
        }
        logFormatSb.append(");");
        document.insertString(psiMethod.getBody().getTextOffset() + 1, prefixSpaceText+logFormatSb.toString());
        psiDocumentManager.doPostponedOperationsAndUnblockDocument(document);
        psiDocumentManager.commitDocument(document);
        FileDocumentManager.getInstance().saveDocument(document);
    }

    @NotNull
    @Override
    public String getText() {
        return "generate method log";
    }

    private static String generatePrefixSpaceText(PsiMethod method, Document document) {
        int startOffset = method.getTextRange().getStartOffset();
        int lastLine = startOffset - 1;
        String text = document.getText(new TextRange(lastLine, lastLine + 1));
        boolean isTabChar = false;
        while (!text.equals("\n")) {
            if (text.equals('\t')) {
                isTabChar = true;
            }
            lastLine--;
            text = document.getText(new TextRange(lastLine, lastLine + 1));
        }
        String methodStartToLastLineText = document.getText(new TextRange(lastLine, startOffset));
        String prefixSpaceText = null;
        if (isTabChar) {
            prefixSpaceText += methodStartToLastLineText + "\t";
        } else {
            prefixSpaceText = methodStartToLastLineText + "    ";
        }
        return prefixSpaceText;
    }

}
