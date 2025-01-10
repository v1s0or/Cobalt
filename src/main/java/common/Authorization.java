package common;

import java.io.File;

public class Authorization {

    protected int watermark = 0;

    protected String validto = "";

    protected String error = null;

    // todo alter Authorization
    // protected boolean valid = false;
    protected boolean valid = true;

    public Authorization() {
        String str = CommonUtils.canonicalize("cobaltstrike.auth");
        if (!new File(str).exists()) {
            try {
                File file = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
                if (file.getName().toLowerCase().endsWith(".jar")) {
                    file = file.getParentFile();
                }
                str = new File(file, "cobaltstrike.auth").getAbsolutePath();
            } catch (Exception exception) {
                MudgeSanity.logException("trouble locating auth file", exception, false);
            }
        }
        byte[] arrby1 = CommonUtils.readFile(str);
        if (arrby1.length == 0) {
            this.error = "Could not read " + str;
            return;
        }
        AuthCrypto authCrypto = new AuthCrypto();
        byte[] arrby2 = authCrypto.decrypt(arrby1);
        if (arrby2.length == 0) {
            this.error = authCrypto.error();
            return;
        }
        try {
            DataParser dataParser = new DataParser(arrby2);
            dataParser.big();
            int i = dataParser.readInt();
            this.watermark = dataParser.readInt();
            byte b1 = dataParser.readByte();
            byte b2 = dataParser.readByte();
            byte[] arrby = dataParser.readBytes(b2);
            if (b1 < 40) {
                this.error = "Authorization file is not for Cobalt Strike 4.0+";
                return;
            }
            if (29999999 == i) {
                this.validto = "forever";
                MudgeSanity.systemDetail("valid to", "perpetual");
            } else {
                this.validto = "20" + i;
                CommonUtils.print_stat("Valid to is: '" + this.validto + "'");
                MudgeSanity.systemDetail("valid to", CommonUtils.formatDateAny("MMMMM d, YYYY", getExpirationDate()));
            }
            this.valid = true;
            MudgeSanity.systemDetail("id", this.watermark + "");
            SleevedResource.Setup(arrby);
        } catch (Exception exception) {
            MudgeSanity.logException("auth file parsing", exception, false);
        }
    }

    public boolean isPerpetual() {
        return "forever".equals(this.validto);
    }

    public boolean isValid() {
        return this.valid;
    }

    public String getError() {
        return this.error;
    }

    public String getWatermark() {
        return this.watermark + "";
    }

    public long getExpirationDate() {
        return CommonUtils.parseDate(this.validto, "yyyyMMdd");
    }

    public boolean isExpired() {
        return (System.currentTimeMillis() > getExpirationDate() + CommonUtils.days(1));
    }

    public String whenExpires() {
        long l = (getExpirationDate() + CommonUtils.days(1) - System.currentTimeMillis()) / CommonUtils.days(1);
        return (l == 1L) ? ("1 day (" + CommonUtils.formatDateAny("MMMMM d, YYYY", getExpirationDate()) + ")") : ((l <= 0L) ? ("TODAY (" + CommonUtils.formatDateAny("MMMMM d, YYYY", getExpirationDate()) + ")") : (l + " days (" + CommonUtils.formatDateAny("MMMMM d, YYYY", getExpirationDate()) + ")"));
    }

    public boolean isAlmostExpired() {
        long l = System.currentTimeMillis() + CommonUtils.days(30);
        return (l > getExpirationDate());
    }
}
