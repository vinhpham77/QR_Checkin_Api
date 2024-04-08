package org.vinhpham.qrcheckinapi.common;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public class Utils {

    public static String getMessage(MessageSource messageSource, String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }

}
