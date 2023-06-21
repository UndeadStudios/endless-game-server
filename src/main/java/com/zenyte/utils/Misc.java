package com.zenyte.utils;

import com.google.common.base.Preconditions;

public class Misc {
    public static String replaceBracketsWithArguments(String string, Object...args) {
    for (Object arg : args) {
        int index = string.indexOf("{}");
        Preconditions.checkState(index != -1, "Invalid number of parameters for string replace.");
        string = string.replaceFirst("\\{}", arg == null ? "null" : arg.toString());
    }
    return string;
}
}
