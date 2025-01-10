package beacon;

import aggressor.AggressorClient;
import beacon.methods.PsExec;
import beacon.methods.WMI;
import beacon.methods.WinRM;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BeaconRemoteExecMethods {
    public Map descriptions = new HashMap();

    public Map exploits = new HashMap();

    public void registerDefaults(AggressorClient aggressorClient) {
        new WMI(aggressorClient);
        new WinRM(aggressorClient);
        new PsExec(aggressorClient);
    }

    public RemoteExecMethod getRemoteExecMethod(String string) {
        synchronized (this) {
            return (RemoteExecMethod) this.exploits.get(string);
        }
    }

    public void register(String string1, String string2, RemoteExecMethod paramRemoteExecMethod) {
        this.descriptions.put(string1, string2);
        this.exploits.put(string1, paramRemoteExecMethod);
    }

    public boolean isRemoteExecMethod(String string) {
        synchronized (this) {
            return this.exploits.containsKey(string);
        }
    }

    public List methods() {
        synchronized (this) {
            LinkedList linkedList = new LinkedList(this.descriptions.keySet());
            Collections.sort(linkedList);
            return linkedList;
        }
    }

    public String getDescription(String string) {
        synchronized (this) {
            return this.descriptions.get(string) + "";
        }
    }

    public static interface RemoteExecMethod {
        void remoteexec(String string1, String string2, String string3);
    }
}
