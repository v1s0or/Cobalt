package aggressor.bridges;

import aggressor.AggressorClient;
import aggressor.Prefs;
import cortana.Cortana;

import java.util.List;
import java.util.Stack;

import sleep.bridges.BridgeUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class PreferencesBridge implements Function, Loadable {

    protected AggressorClient client;

    public PreferencesBridge(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public void scriptLoaded(ScriptInstance scriptInstance) {
        Cortana.put(scriptInstance, "&pref_set", this);
        Cortana.put(scriptInstance, "&pref_set_list", this);
        Cortana.put(scriptInstance, "&pref_get", this);
        Cortana.put(scriptInstance, "&pref_get_list", this);
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        if ("&pref_set".equals(string)) {
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            Prefs.getPreferences().set(str1, str2);
            Prefs.getPreferences().save();
        } else if ("&pref_set_list".equals(string)) {
            String str = BridgeUtilities.getString(stack, "");
            List list = SleepUtils.getListFromArray(BridgeUtilities.getScalar(stack));
            Prefs.getPreferences().setList(str, list);
            Prefs.getPreferences().save();
        } else {
            if ("&pref_get".equals(string)) {
                String str1 = BridgeUtilities.getString(stack, "");
                String str2 = BridgeUtilities.getString(stack, "");
                return SleepUtils.getScalar(Prefs.getPreferences().getString(str1, str2));
            }
            if ("&pref_get_list".equals(string)) {
                String str = BridgeUtilities.getString(stack, "");
                return SleepUtils.getArrayWrapper(Prefs.getPreferences().getList(str));
            }
        }
        return SleepUtils.getEmptyScalar();
    }
}
