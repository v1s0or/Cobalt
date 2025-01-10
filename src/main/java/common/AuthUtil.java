package common;

public class AuthUtil {

    protected int watermark = 0;

    protected String licensekey = "";

    protected String validto = "";

    protected String error = null;

    protected boolean valid = false;

    public AuthUtil(String string) {
        String str = string;
        byte[] arrby1 = CommonUtils.readFile(str);
        if (arrby1.length == 0) {
            CommonUtils.print_error("Could not read " + str);
            return;
        }
        CommonUtils.print_stat("Read: " + str);
        AuthCrypto authCrypto = new AuthCrypto();
        byte[] arrby2 = authCrypto.decrypt(arrby1);
        if (arrby2.length == 0) {
            CommonUtils.print_error(authCrypto.error());
            return;
        }
        String[] arrstring = CommonUtils.toArray(CommonUtils.bString(arrby2));
        if (arrstring.length < 4) {
            CommonUtils.print_error("auth content is only " + arrstring.length + " items");
            return;
        }
        this.licensekey = arrstring[0];
        if ("forever".equals(arrstring[1])) {
            this.validto = arrstring[1];
            CommonUtils.print_info("valid to: perpetual");
        } else {
            this.validto = "20" + arrstring[1];
            CommonUtils.print_info("valid to: " + CommonUtils.formatDateAny("MMMMM d, YYYY", getExpirationDate()));
        }
        this.watermark = CommonUtils.toNumber(arrstring[2], 0);
        CommonUtils.print_info("id " + this.watermark + "");
    }

    public long getExpirationDate() {
        return CommonUtils.parseDate(this.validto, "yyyyMMdd");
    }

    public static void main(String[] arrstring) {
        new AuthUtil(arrstring[0]);
    }
}
