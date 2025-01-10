package beacon;

import common.BeaconEntry;
import common.CommonUtils;
import common.StringStack;

public class Registry {
    public static final short HKLM = 0;

    public static final short HKCR = 1;

    public static final short HKCC = 2;

    public static final short HKCU = 3;

    public static final short HKU = 4;

    private static String[] shortNames = {"HKLM\\", "HKCR\\", "HKCC\\", "HKCU\\", "HKU\\"};

    private static String[] longNames = {"HKEY_LOCAL_MACHINE\\", "HKEY_CLASSES_ROOT\\", "HKEY_CURRENT_CONFIG\\", "HKEY_CURRENT_USER\\", "HKEY_USERS\\"};

    public static final short KEY_WOW64_64KEY = 256;

    public static final short KEY_WOW64_32KEY = 512;

    protected String pathv = "";

    protected String value = "";

    protected short hive = -1;

    protected String arch = "";

    protected boolean hasvalue = false;

    protected void parseHive(String[] arrstring) {
        for (byte b = 0; b < arrstring.length; b++) {
            if (this.pathv.startsWith(arrstring[b])) {
                this.pathv = this.pathv.substring(arrstring[b].length());
                this.hive = (short) b;
                break;
            }
        }
    }

    public String getPath() {
        return this.pathv;
    }

    public String getValue() {
        return this.value;
    }

    public short getHive() {
        return this.hive;
    }

    public Registry(String string1, String string2, boolean bl) {
        this.arch = string1;
        this.hasvalue = bl;
        if (bl) {
            StringStack stringStack = new StringStack(string2, " ");
            this.value = stringStack.pop();
            this.pathv = stringStack.toString();
        } else {
            this.pathv = string2;
        }
        parseHive(shortNames);
        parseHive(longNames);
    }

    public int getFlags(BeaconEntry beaconEntry) {
        if (beaconEntry != null && "x86".equals(this.arch) && !beaconEntry.is64()) {
            CommonUtils.print_stat("Windows 2000 flag rule for " + beaconEntry);
            return 0;
        }
        if ("x86".equals(this.arch)) {
            return 512;
        }
        return 256;
    }

    public boolean isValid() {
        return (getError() == null);
    }

    public String getError() {
        return (this.hasvalue && ("".equals(this.value) || "".equals(this.pathv))) ? "specify a value name too (e.g., HKLM\\foo\\bar Baz)" : ((this.hive == -1) ? "path must start with HKLM, HKCR, HKCC, HKCU, or HKU" : null);
    }

    public String toString() {
        return !isValid() ? "[invalid]" : ("".equals(this.value) ? (shortNames[getHive()] + this.pathv + " (" + this.arch + ")") : (shortNames[getHive()] + this.pathv + " /v " + this.value + " (" + this.arch + ")"));
    }
}
