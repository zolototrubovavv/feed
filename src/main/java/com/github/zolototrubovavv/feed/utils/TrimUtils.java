package com.github.zolototrubovavv.feed.utils;

import lombok.experimental.UtilityClass;

@UtilityClass 
public class TrimUtils {

    public String trim(String s) {
        return s == null ? null : s.trim();
    }
}
