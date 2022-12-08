package com.yitu.boring.action;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import com.yitu.boring.util.LogJsonUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

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
        return "BoringDevIdeaPlugin";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        if (element.getParent() instanceof PsiMethod) {
            return true;
        }
        return false;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        Document document = psiDocumentManager.getDocument(element.getContainingFile());
        PsiMethod psiMethod = (PsiMethod) element.getParent();
        String indentStr = calculateIndentStr(psiMethod, document);
        insertStartLog(psiMethod, document, indentStr);
        psiDocumentManager.doPostponedOperationsAndUnblockDocument(document);
        psiDocumentManager.commitDocument(document);
        insertFinishLog(psiMethod, document, indentStr);
        FileDocumentManager.getInstance().saveDocument(document);
    }

    @NotNull
    @Override
    public String getText() {
        return "generate method log";
    }

    private static void insertStartLog(PsiMethod psiMethod, Document document, String indentStr) {
        String logNonParameterFormat = "log.info(\"start %s\");";
        String logFormat = "log.info(\"start %s, %s\", %s);";
        String startLog;
        // String logFormat="log.info(\"start "+psiMethod.getName();
        PsiParameterList parameterList = psiMethod.getParameterList();
        PsiParameter[] parameters = parameterList.getParameters();
        if (parameters.length == 0) {
            startLog = String.format(logNonParameterFormat, psiMethod.getName());
        } else if (parameters.length == 1) {
            startLog = String.format(logFormat, psiMethod.getName(), parameters[0].getName() + ":[{}]", LogJsonUtil.logParameterStr(parameters[0]));
        } else if (parameters.length > 1) {
            StringBuilder parametersLogText = new StringBuilder(parameters[0].getName() + ":[{}]");
            StringBuilder parametersVariableText = new StringBuilder(LogJsonUtil.logParameterStr(parameters[0]));
            for (int i = 1; i < parameters.length; i++) {
                parametersLogText.append(",").append(parameters[i].getName() + ":[{}]");
                parametersVariableText.append(", ").append(LogJsonUtil.logParameterStr(parameters[i]));
            }
            startLog = String.format(logFormat, psiMethod.getName(), parametersLogText, parametersVariableText);
        } else {
            return;
        }
        document.insertString(psiMethod.getBody().getTextOffset() + 1, "\n" + indentStr + startLog);
    }

    private static void insertFinishLog(PsiMethod psiMethod, Document document, String indentStr) {
        String logFormat = "log.info(\"finish %s, response:[{}]\", %s);";
        String returnTypeName = psiMethod.getReturnType().getPresentableText();
        if (returnTypeName.contains("<")) {
            returnTypeName = returnTypeName.substring(0, returnTypeName.indexOf("<"));
        }
        if (Character.isUpperCase(returnTypeName.charAt(0))) {
            returnTypeName = Character.toLowerCase(returnTypeName.charAt(0)) + returnTypeName.substring(1);
        }
        String finishLog = String.format(logFormat, psiMethod.getName(), returnTypeName);
        if ("void".equals(returnTypeName)) {
            logFormat = "log.info(\"finish %s\");";
            finishLog = String.format(logFormat, psiMethod.getName());
        }
        int methodBodyEndOffset = psiMethod.getBody().getTextRange().getEndOffset();
        String text;
        do {
            methodBodyEndOffset--;
            text = document.getText(new TextRange(methodBodyEndOffset, methodBodyEndOffset + 1));
        } while (!text.equals("\n"));
        document.insertString(methodBodyEndOffset, "\n" + indentStr + finishLog);
    }

    private static String calculateIndentStr(PsiMethod psiMethod, Document document) {
        int methodStartIndex = psiMethod.getTextRange().getStartOffset();
        int indentOffset = 0;
        String text;
        do {
            indentOffset++;
            text = document.getText(new TextRange(methodStartIndex - indentOffset, methodStartIndex - indentOffset + 1));
        } while (!text.equals("\n"));
        return document.getText(new TextRange(methodStartIndex - indentOffset + 1, methodStartIndex)) + "    ";
    }

}
