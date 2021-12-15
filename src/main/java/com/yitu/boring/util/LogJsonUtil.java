package com.yitu.boring.util;

import com.google.common.collect.Sets;
import com.intellij.psi.PsiArrayType;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiUtil;

import java.util.Set;

/**
 * @author : ⚡️
 * @date : Created in 2021/12/15 11:36 上午
 * description :
 * modified :
 */
public class LogJsonUtil {

    public static final Set<String> NORMAL_TYPES = Sets.newHashSet();
    static {
        NORMAL_TYPES.add("Boolean");
        NORMAL_TYPES.add("Byte");
        NORMAL_TYPES.add("Short");
        NORMAL_TYPES.add("Integer");
        NORMAL_TYPES.add("Long");
        NORMAL_TYPES.add("Float");
        NORMAL_TYPES.add("Double");
        NORMAL_TYPES.add("String");
        NORMAL_TYPES.add("BigDecimal");
        NORMAL_TYPES.add("Date");
        NORMAL_TYPES.add("Timestamp");
        NORMAL_TYPES.add("LocalDate");
        NORMAL_TYPES.add("LocalTime");
        NORMAL_TYPES.add("LocalDateTime");
    }

    public static String logParameterStr(PsiParameter psiParameter){
        if (need2Json(psiParameter.getType())){
            return String.format("JSON.toJSONString(%s)", psiParameter.getName());
        }
        return psiParameter.getName();
    }

    public static boolean need2Json(PsiType psiType){
        if (psiType instanceof PsiPrimitiveType){
            return false;
        }
        if (NORMAL_TYPES.contains(psiType.getPresentableText())){
            return false;
        }
        if (psiType instanceof PsiArrayType){
            return true;
        }
        if (psiType.getPresentableText().matches("List<(.*)>")){
            return true;
        }
        if (psiType.getPresentableText().matches("Set<(.*)>")){
            return true;
        }
        if (psiType.getPresentableText().matches("Map<(.*)>")){
            return true;
        }
        if (PsiUtil.resolveClassInClassTypeOnly(psiType).isEnum()){
            return false;
        }
        return true;
    }

}
