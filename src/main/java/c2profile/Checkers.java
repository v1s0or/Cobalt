package c2profile;

import common.CommonUtils;

import java.util.Set;

public class Checkers {
    public static final boolean isComment(String string) {
        return (string.charAt(0) == '#' && string.charAt(string.length() - 1) == '\n');
    }

    public static final boolean isBlock(String string) {
        return (string.charAt(0) == '{' && string.charAt(string.length() - 1) == '}');
    }

    public static final boolean isString(String string) {
        return (string.charAt(0) == '"' && string.charAt(string.length() - 1) == '"');
    }

    public static final boolean isBoolean(String string) {
        return (string.equals("true") || string.equals("false"));
    }

    public static final boolean isStatement(String string1, String string2) {
        return string2.equals("EOT");
    }

    public static final boolean isSetStatement(String string1, String string2, String string3, String string4) {
        return (string1.equals("set") && isStatementArg(string2, string3, string4));
    }

    public static final boolean isIndicator(String string1, String string2, String string3, String string4) {
        return ((string1.equals("header") || string1.equals("parameter") || string1.equals("strrep")) && isString(string2) && isString(string3) && isStatement(string3, string4));
    }

    public static final boolean isStatementArg(String string1, String string2, String string3) {
        return (isString(string2) && isStatement(string1, string3));
    }

    public static final boolean isStatementArgBlock(String string1, String string2, String string3) {
        return (isString(string2) && isBlock(string3));
    }

    public static final boolean isStatementBlock(String string1, String string2) {
        return isBlock(string2);
    }

    public static final boolean isDate(String string) {
        return CommonUtils.isDate(string, "dd MMM yyyy HH:mm:ss");
    }

    public static final boolean isNumber(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    public static final boolean isHTTPVerb(String string) {
        Set set = CommonUtils.toSet("GET, POST");
        return set.contains(string);
    }

    public static final boolean isAllocator(String string) {
        Set set = CommonUtils.toSet("VirtualAllocEx, NtMapViewOfSection");
        return set.contains(string);
    }
}
