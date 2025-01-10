package common;

import dns.SleeveSecurity;

public class SleevedResource {

    private static SleevedResource singleton;

    private SleeveSecurity data = new SleeveSecurity();

    public static void Setup(byte[] arrby) {
        singleton = new SleevedResource(arrby);
    }

    public static byte[] readResource(String string) {
        return singleton._readResource(string);
    }

    private SleevedResource(byte[] arrby) {
        this.data.registerKey(arrby);
    }

    private byte[] _readResource(String string) {
        String str = CommonUtils.strrep(string, "resources/", "sleeve/");
        byte[] arrby1 = CommonUtils.readResource(str);
        if (arrby1.length > 0) {
            long l = System.currentTimeMillis();
            return this.data.decrypt(arrby1);
        }
        byte[] arrby2 = CommonUtils.readResource(string);
        if (arrby2.length == 0) {
            CommonUtils.print_error("Could not find sleeved resource: " + string + " [ERROR]");
        } else {
            CommonUtils.print_stat("Used internal resource: " + string);
        }
        return arrby2;
    }
}
