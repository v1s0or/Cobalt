package beacon;

import common.AssertUtils;
import common.CommonUtils;
import common.Packer;

public class Settings {
    public static final int PATCH_SIZE = 4096;

    public static final int MAX_SETTINGS = 64;

    public static final int TYPE_NONE = 0;

    public static final int TYPE_SHORT = 1;

    public static final int TYPE_INT = 2;

    public static final int TYPE_PTR = 3;

    protected Packer patch = new Packer();

    public void addShort(int n1, int n2) {
        AssertUtils.TestRange(n1, 0, 64);
        this.patch.addShort(n1);
        this.patch.addShort(1);
        this.patch.addShort(2);
        this.patch.addShort(n2);
    }

    public void addInt(int n1, int n2) {
        AssertUtils.TestRange(n1, 0, 64);
        this.patch.addShort(n1);
        this.patch.addShort(2);
        this.patch.addShort(4);
        this.patch.addInt(n2);
    }

    public void addData(int n1, byte[] arrby, int n2) {
        AssertUtils.TestRange(n1, 0, 64);
        this.patch.addShort(n1);
        this.patch.addShort(3);
        this.patch.addShort(n2);
        this.patch.addString(arrby, n2);
    }

    public void addString(int n1, String string, int n2) {
        addData(n1, CommonUtils.toBytes(string), n2);
    }

    public byte[] toPatch() {
        return toPatch(4096);
    }

    public byte[] toPatch(int n) {
        this.patch.addShort(0);
        byte[] arrby1 = this.patch.getBytes();
        AssertUtils.Test((arrby1.length < n), "Patch " + arrby1.length + " bytes is too large! Beacon will crash");
        byte[] arrby2 = CommonUtils.randomData(n - arrby1.length);
        this.patch.addString(arrby2, arrby2.length);
        return this.patch.getBytes();
    }
}
