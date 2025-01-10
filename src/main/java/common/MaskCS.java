package common;

import dns.SleeveSecurity;

import java.io.File;

public class MaskCS {

    protected SleeveSecurity crypto;

    public MaskCS() {
        byte[] arrby = CommonUtils.toBytes("aliens");
        this.crypto = new SleeveSecurity();
        this.crypto.registerKey(arrby);
        processFile("./cobaltstrike.jar");
    }

    public void processFile(String string) {
        byte[] arrby1 = CommonUtils.readFile(string);
        byte[] arrby2 = CommonUtils.MD5(arrby1);
        byte[] arrby3 = this.crypto.encrypt(arrby1);
        CommonUtils.writeToFile(new File(string + ".mask"), arrby3);
        CommonUtils.print_stat("Wrote '" + string + "' " + arrby3.length + " bytes");
    }

    public static void main(String[] arrstring) {
        new MaskCS();
    }
}
