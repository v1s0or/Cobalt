package aggressor.bridges;

import aggressor.AggressorClient;
import cortana.Cortana;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;
import dialog.SafeDialogs;

import java.util.Stack;

import sleep.bridges.BridgeUtilities;
import sleep.bridges.SleepClosure;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class SafeDialogBridge implements Function, Loadable {

    protected AggressorClient client;

    public SafeDialogBridge(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public void scriptLoaded(ScriptInstance scriptInstance) {
        Cortana.put(scriptInstance, "&show_message", this);
        Cortana.put(scriptInstance, "&show_error", this);
        Cortana.put(scriptInstance, "&prompt_confirm", this);
        Cortana.put(scriptInstance, "&prompt_text", this);
        Cortana.put(scriptInstance, "&prompt_file_open", this);
        Cortana.put(scriptInstance, "&prompt_directory_open", this);
        Cortana.put(scriptInstance, "&prompt_file_save", this);
    }

    public SafeDialogCallback popCallback(Stack stack, ScriptInstance scriptInstance) {
        final SleepClosure f = BridgeUtilities.getFunction(stack, scriptInstance);
        return new SafeDialogCallback() {
            public void dialogResult(String string) {
                if (string == null)
                    return;
                Stack stack = new Stack();
                stack.push(SleepUtils.getScalar(string));
                SleepUtils.runCode(f, "dialogResult", null, stack);
            }
        };
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        if ("&show_message".equals(string)) {
            String str = BridgeUtilities.getString(stack, "");
            DialogUtils.showInfo(str);
        } else if ("&show_error".equals(string)) {
            String str = BridgeUtilities.getString(stack, "");
            DialogUtils.showError(str);
        } else if ("&prompt_confirm".equals(string)) {
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            SafeDialogs.askYesNo(str1, str2, popCallback(stack, scriptInstance));
        } else if ("&prompt_text".equals(string)) {
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            SafeDialogs.ask(str1, str2, popCallback(stack, scriptInstance));
        } else if ("&prompt_file_open".equals(string)) {
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            boolean bool = SleepUtils.isTrueScalar(BridgeUtilities.getScalar(stack));
            SafeDialogs.openFile(str1, str2, null, bool, false, popCallback(stack, scriptInstance));
        } else if ("&prompt_directory_open".equals(string)) {
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            boolean bool = SleepUtils.isTrueScalar(BridgeUtilities.getScalar(stack));
            SafeDialogs.openFile(str1, str2, null, bool, true, popCallback(stack, scriptInstance));
        } else if ("&prompt_file_save".equals(string)) {
            String str = BridgeUtilities.getString(stack, "");
            SafeDialogs.saveFile(null, str, popCallback(stack, scriptInstance));
        }
        return SleepUtils.getEmptyScalar();
    }
}
