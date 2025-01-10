package kerberos;

import common.CommonUtils;
import common.MudgeSanity;

import java.util.Hashtable;
import java.util.Stack;

import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.ScriptLoader;
import sleep.runtime.SleepUtils;

public class KerberosUtils {
    private static ScriptInstance converter = null;

    public static byte[] ConvertCCacheToKrbCred(String string) {
        synchronized (KerberosUtils.class) {
            if (converter == null)
                try {
                    ScriptLoader scriptLoader = new ScriptLoader();
                    converter = scriptLoader.loadScript("ccache_krbcred.sl", CommonUtils.resource("resources/ccache_krbcred.sl"), new Hashtable());
                    converter.runScript();
                } catch (Exception exception) {
                    MudgeSanity.logException("compile converter", exception, false);
                    return new byte[0];
                }
            Stack stack = new Stack();
            stack.push(SleepUtils.getScalar(string));
            Scalar scalar = converter.callFunction("&convert", stack);
            return CommonUtils.toBytes(scalar.toString());
        }
    }
}
