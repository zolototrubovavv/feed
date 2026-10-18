package com.github.zolototrubovavv.feed.utils;

// Если предполагается, что класс утильный и в нем будут только статические методы, то
// стоит его сделать final и повесить приватный конструктор. Либо тоже самое сделать через аннотацию lombok @UtilityClass

// Вариант 1 (аннотация экспериментальная и могут быть проблемы, хотя сам не сталкивался):
// https://projectlombok.org/features/experimental/UtilityClass
// @UtilityClass

// Вариант 2 (более очевидный, в отличии от @UtilityClass):
// - приватный конструктор
// @NoArgsConstructor(access = AccessLevel.PRIVATE)

// - и final для класса
//public final class TrimUtils {

public class TrimUtils {

    public static String trim(String s) {
        return s == null ? null : s.trim();
    }
}
