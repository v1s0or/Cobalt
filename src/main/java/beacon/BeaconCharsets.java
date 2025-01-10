package beacon;

import common.CommonUtils;
import common.MudgeSanity;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class BeaconCharsets {
    protected Map charsets_ansi = new HashMap();

    protected Map charsets_oem = new HashMap();

    public String process(String string, byte[] arrby) {
        return process(this.charsets_ansi, string, arrby);
    }

    public String processOEM(String string, byte[] arrby) {
        return process(this.charsets_oem, string, arrby);
    }

    public String process(Map map, String string, byte[] arrby) {
        Charset charset = get(map, string);
        if (charset == null)
            return CommonUtils.bString(arrby);
        try {
            return charset.decode(ByteBuffer.wrap(arrby)).toString();
        } catch (Exception exception) {
            MudgeSanity.logException("could not convert text for id " + string + " with " + charset, exception, false);
            return CommonUtils.bString(arrby);
        }
    }

    public Charset get(Map map, String string) {
        synchronized (this) {
            return (Charset) map.get(string);
        }
    }

    public void register(String string1, String string2, String string3) {
        register(this.charsets_ansi, string1, string2);
        register(this.charsets_oem, string1, string3);
    }

    public void register(Map map, String string1, String string2) {
        if (string2 == null)
            return;
        try {
            Charset charset = Charset.forName(string2);
            synchronized (this) {
                map.put(string1, charset);
            }
        } catch (Exception exception) {
            MudgeSanity.logException("Could not find charset '" + string2 + "' for Beacon ID " + string1, exception, false);
        }
    }
}
