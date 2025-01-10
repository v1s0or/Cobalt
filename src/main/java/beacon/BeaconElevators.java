package beacon;

import aggressor.AggressorClient;
import beacon.elevators.BypassUACCMSTPLUA;
import beacon.elevators.BypassUACToken;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BeaconElevators {
    public Map descriptions = new HashMap();

    public Map exploits = new HashMap();

    public void registerDefaults(AggressorClient aggressorClient) {
        new BypassUACToken(aggressorClient);
        new BypassUACCMSTPLUA(aggressorClient);
    }

    public Elevator getCommandElevator(String string) {
        synchronized (this) {
            return (Elevator) this.exploits.get(string);
        }
    }

    public void register(String string1, String string2, Elevator paramElevator) {
        this.descriptions.put(string1, string2);
        this.exploits.put(string1, paramElevator);
    }

    public boolean isElevator(String string) {
        synchronized (this) {
            return this.exploits.containsKey(string);
        }
    }

    public List elevators() {
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

    public static interface Elevator {
        void runasadmin(String string1, String string2);
    }
}
