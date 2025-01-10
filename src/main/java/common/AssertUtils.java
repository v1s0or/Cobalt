package common;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

public class AssertUtils {

    public static boolean Test(boolean bl, String string) {
        if (!bl) {
            return TestFail(string);
        }
        return true;
        // return !bl ? TestFail(string) : true;
    }

    public static boolean TestFail(String string) {
        CommonUtils.print_error("Assertion failed: " + string);
        Thread.dumpStack();
        return false;
    }

    public static boolean TestNotNull(Object object, String string) {
        if (object == null) {
            CommonUtils.print_error("Assertion failed: " + string + " is null");
            Thread.dumpStack();
            return false;
        }
        return true;
    }

    public static boolean TestUnique(Object object, Collection collection) {
        if (collection.contains(object)) {
            CommonUtils.print_error("Assertion failed: '" + object + "' is not unique in: " + collection);
            Thread.dumpStack();
            return false;
        }
        return true;
    }

    public static boolean TestSetValue(String string1, String string2) {
        Set set = CommonUtils.toSet(string2);
        if (set.contains(string1))
            return true;
        CommonUtils.print_error("Assertion failed: '" + string1 + "' is not in: " + string2);
        Thread.dumpStack();
        return false;
    }

    public static boolean TestArch(String string) {
        return TestSetValue(string, "x86, x64");
    }

    public static boolean TestPID(int n) {
        return TestRange(n, 0, 2147483647);
    }

    public static boolean TestPort(int n) {
        return TestRange(n, 0, 65535);
    }

    public static boolean TestPatchI(byte[] arrby, int n1, int n2) {
        try {
            DataParser dataParser = new DataParser(arrby);
            dataParser.jump(n2);
            int i = dataParser.readInt();
            if (i == n1)
                return true;
            CommonUtils.print_error("Assertion failed: 0x"
                    + CommonUtils.toHex(i) + " at " + n2 + " is not 0x" + CommonUtils.toHex(n1));
            Thread.dumpStack();
            return false;
        } catch (IOException iOException) {
            CommonUtils.print_error("Assertion failed: jump to " + n2 + " exception: " + iOException.getMessage());
            Thread.dumpStack();
            return false;
        }
    }

    public static boolean TestPatchS(byte[] arrby, int n1, int n2) {
        try {
            DataParser dataParser = new DataParser(arrby);
            dataParser.jump(n2);
            int i = dataParser.readShort();
            if (i == n1)
                return true;
            CommonUtils.print_error("Assertion failed: 0x" + CommonUtils.toHex(i) + " at " + n2 + " is not 0x" + CommonUtils.toHex(n1));
            Thread.dumpStack();
            return false;
        } catch (IOException iOException) {
            CommonUtils.print_error("Assertion failed: jump to " + n2 + " exception: " + iOException.getMessage());
            Thread.dumpStack();
            return false;
        }
    }

    public static boolean TestPatch(String string1, String string2, int n) {
        String str = string1.substring(n, n + string2.length());
        if (str.equals(string2))
            return true;
        CommonUtils.print_error("Assertion failed: " + CommonUtils.toHexString(CommonUtils.toBytes(str)) + " at " + n + " is not " + CommonUtils.toHexString(CommonUtils.toBytes(string2)));
        Thread.dumpStack();
        return false;
    }

    public static boolean TestRange(int n1, int n2, int n3) {
        if (n1 >= n2 && n1 <= n3)
            return true;
        CommonUtils.print_error("Assertion failed: " + n2 + " <= " + n1 + " (value) <= " + n3 + " does not hold");
        Thread.dumpStack();
        return false;
    }
}
