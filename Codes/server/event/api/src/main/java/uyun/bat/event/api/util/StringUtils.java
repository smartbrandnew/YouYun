package uyun.bat.event.api.util;

import java.io.UnsupportedEncodingException;

public class StringUtils {

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static String getLimitLengthString(String str,int len ){
        int counterOfDoubleByte  = 0;
        byte b[] ;
        String charSet="GBK";
        try {
            b = str.getBytes(charSet);
            if(b.length <= len)
                return str;
            for(int i = 0; i < len; i++){
                if(b[i] < 0)
                    counterOfDoubleByte++;
            }
            if(counterOfDoubleByte % 2 == 0)
                return new String(b, 0, len, charSet);
            else
                return new String(b, 0, len - 1, charSet);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("String limit error!");
        }
    }

    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }
}
