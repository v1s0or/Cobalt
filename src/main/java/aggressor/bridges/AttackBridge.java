package aggressor.bridges;

import common.CommonUtils;
import cortana.Cortana;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import sleep.bridges.BridgeUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class AttackBridge implements Function, Loadable {
    protected List ids = new LinkedList();

    protected Map data = new HashMap();

    public void scriptLoaded(ScriptInstance scriptInstance) {
        Cortana.put(scriptInstance, "&attack_tactics", this);
        Cortana.put(scriptInstance, "&attack_name", this);
        Cortana.put(scriptInstance, "&attack_describe", this);
        Cortana.put(scriptInstance, "&attack_mitigate", this);
        Cortana.put(scriptInstance, "&attack_detect", this);
        Cortana.put(scriptInstance, "&attack_url", this);
    }

    public void loadAttackMatrix() {
        if (this.ids.size() > 0)
            return;
        List<Map> list = SleepUtils.getListFromArray((Scalar) CommonUtils.readObjectResource("resources/attack.bin"));
        for (Map map : list) {
            String str = (String) map.get("id");
            this.ids.add(str);
            this.data.put(str, map);
        }
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        loadAttackMatrix();
        if ("&attack_tactics".equals(string)) {
            return SleepUtils.getArrayWrapper(this.ids);
        }
        String str = BridgeUtilities.getString(stack, "");
        Map map = (Map) this.data.get(str);
        if (map == null) {
            throw new RuntimeException("ATT&CK Technique '" + str + "' was not found.");
        }
        if ("&attack_name".equals(string)) {
            return SleepUtils.getScalar((String) map.get("name"));
        }
        if ("&attack_describe".equals(string)) {
            return SleepUtils.getScalar((String) map.get("describe"));
        }
        if ("&attack_mitigate".equals(string)) {
            return SleepUtils.getScalar((String) map.get("mitigate"));
        }
        if ("&attack_detect".equals(string)) {
            return SleepUtils.getScalar((String) map.get("detect"));
        }
        if ("&attack_url".equals(string)) {
            return SleepUtils.getScalar("https://attack.mitre.org/wiki/Technique/" + str);
        }
        return SleepUtils.getEmptyScalar();
    }
}
