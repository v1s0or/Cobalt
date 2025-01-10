package kerberos;

import common.CommonUtils;
import common.DataParser;

import java.io.IOException;

public class KeyBlock {
    protected int keytype;

    protected int etype;

    protected int keylen;

    protected byte[] keyvalue;

    public KeyBlock(DataParser paramDataParser) throws IOException {
        this.keytype = paramDataParser.readShort();
        this.etype = paramDataParser.readShort();
        this.keylen = paramDataParser.readShort();
        this.keyvalue = paramDataParser.readBytes(this.keylen);
    }

    public String toString() {
        return "KeyBlock: " + this.keytype + "/" + this.etype + " " + CommonUtils.toHexString(this.keyvalue);
    }
}
