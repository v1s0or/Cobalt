package aggressor.bridges;

import aggressor.AggressorClient;
import common.AddressList;
import common.CommonUtils;
import common.PowerShellUtils;
import common.RangeList;
import cortana.Cortana;
import dialog.DialogUtils;
import encoders.Base64;

import java.io.File;
import java.util.Stack;

import sleep.bridges.BridgeUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class UtilityBridge implements Function, Loadable {

    protected AggressorClient client;

    public UtilityBridge(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public void scriptLoaded(ScriptInstance scriptInstance) {
        Cortana.put(scriptInstance, "&url_open", this);
        Cortana.put(scriptInstance, "&licenseKey", this);
        Cortana.put(scriptInstance, "&format_size", this);
        Cortana.put(scriptInstance, "&script_resource", this);
        Cortana.put(scriptInstance, "&base64_encode", this);
        Cortana.put(scriptInstance, "&base64_decode", this);
        Cortana.put(scriptInstance, "&str_encode", this);
        Cortana.put(scriptInstance, "&str_decode", this);
        Cortana.put(scriptInstance, "&powershell_encode_stager", this);
        Cortana.put(scriptInstance, "&powershell_encode_oneliner", this);
        Cortana.put(scriptInstance, "&powershell_command", this);
        Cortana.put(scriptInstance, "&powershell_compress", this);
        Cortana.put(scriptInstance, "&gzip", this);
        Cortana.put(scriptInstance, "&gunzip", this);
        Cortana.put(scriptInstance, "&add_to_clipboard", this);
        Cortana.put(scriptInstance, "&range", this);
        Cortana.put(scriptInstance, "&iprange", this);
        Cortana.put(scriptInstance, "&str_xor", this);
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        if (string.equals("&url_open")) {
            DialogUtils.gotoURL(BridgeUtilities.getString(stack, "")).actionPerformed(null);
        } else {
            if (string.equals("&licenseKey")) {
                String str = CommonUtils.bString(CommonUtils.readFile((new File(System.getProperty("user.home"), ".cobaltstrike.license")).getAbsolutePath())).trim();
                return SleepUtils.getScalar(str);
            }
            if (string.equals("&format_size")) {
                long l = BridgeUtilities.getLong(stack, 0L);
                String str = "b";
                if (l > 1024L) {
                    l /= 1024L;
                    str = "kb";
                }
                if (l > 1024L) {
                    l /= 1024L;
                    str = "mb";
                }
                if (l > 1024L) {
                    l /= 1024L;
                    str = "gb";
                }
                return SleepUtils.getScalar(l + str);
            }
            if (string.equals("&script_resource"))
                return SleepUtils.getScalar((new File((new File(scriptInstance.getName())).getParent(), BridgeUtilities.getString(stack, ""))).getAbsolutePath());
            if (string.equals("&base64_encode")) {
                byte[] arrby = CommonUtils.toBytes(BridgeUtilities.getString(stack, ""));
                return SleepUtils.getScalar(Base64.encode(arrby));
            }
            if (string.equals("&base64_decode")) {
                String str = BridgeUtilities.getString(stack, "");
                return SleepUtils.getScalar(Base64.decode(str));
            }
            if (string.equals("&powershell_encode_stager")) {
                byte[] arrby = CommonUtils.toBytes(BridgeUtilities.getString(stack, ""));
                return SleepUtils.getScalar((new PowerShellUtils(this.client)).encodePowerShellCommand(arrby));
            }
            if (string.equals("&powershell_encode_oneliner")) {
                String str = BridgeUtilities.getString(stack, "");
                return SleepUtils.getScalar(CommonUtils.EncodePowerShellOneLiner(str));
            }
            if (string.equals("&powershell_command")) {
                String str = BridgeUtilities.getString(stack, "");
                boolean bool = SleepUtils.isTrueScalar(BridgeUtilities.getScalar(stack));
                return SleepUtils.getScalar((new PowerShellUtils(this.client)).format(str, bool));
            }
            if (string.equals("&powershell_compress")) {
                String str = BridgeUtilities.getString(stack, "");
                return SleepUtils.getScalar((new PowerShellUtils(this.client)).PowerShellCompress(CommonUtils.toBytes(str)));
            }
            if (string.equals("&gzip")) {
                byte[] arrby = CommonUtils.toBytes(BridgeUtilities.getString(stack, ""));
                return SleepUtils.getScalar(CommonUtils.gzip(arrby));
            }
            if (string.equals("&gunzip")) {
                byte[] arrby = CommonUtils.toBytes(BridgeUtilities.getString(stack, ""));
                return SleepUtils.getScalar(CommonUtils.gunzip(arrby));
            }
            if (string.equals("&add_to_clipboard")) {
                String str = BridgeUtilities.getString(stack, "");
                DialogUtils.addToClipboard(str);
            } else {
                if (string.equals("&range")) {
                    String str = BridgeUtilities.getString(stack, "");
                    RangeList rangeList = new RangeList(str);
                    if (rangeList.hasError())
                        throw new RuntimeException(rangeList.getError());
                    return SleepUtils.getArrayWrapper(rangeList.toList());
                }
                if (string.equals("&iprange")) {
                    String str = BridgeUtilities.getString(stack, "");
                    AddressList addressList = new AddressList(str);
                    if (addressList.hasError())
                        throw new RuntimeException(addressList.getError());
                    return SleepUtils.getArrayWrapper(addressList.toList());
                }
                if (string.equals("&str_encode")) {
                    String str1 = BridgeUtilities.getString(stack, "");
                    String str2 = BridgeUtilities.getString(stack, "");
                    return SleepUtils.getScalar(CommonUtils.toBytes(str1, str2));
                }
                if (string.equals("&str_decode")) {
                    byte[] arrby = CommonUtils.toBytes(BridgeUtilities.getString(stack, ""));
                    String str = BridgeUtilities.getString(stack, "");
                    return SleepUtils.getScalar(CommonUtils.bString(arrby, str));
                }
                if (string.equals("&str_xor")) {
                    byte[] arrby1 = CommonUtils.toBytes(BridgeUtilities.getString(stack, ""));
                    byte[] arrby2 = CommonUtils.toBytes(BridgeUtilities.getString(stack, ""));
                    return SleepUtils.getScalar(CommonUtils.XorString(arrby1, arrby2));
                }
            }
        }
        return SleepUtils.getEmptyScalar();
    }
}
