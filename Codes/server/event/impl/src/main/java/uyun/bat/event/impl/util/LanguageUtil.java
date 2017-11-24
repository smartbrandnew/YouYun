package uyun.bat.event.impl.util;

import uyun.bat.common.config.Config;

public class LanguageUtil {
    private static final String LANG = Config.getInstance().get("uyun.lang", "");

    public static boolean isChinese() {
        return LANG.equals("zh_CN");
    }

    public static  boolean isEnglish() {
        return LANG.equals("en_US");
    }


}
