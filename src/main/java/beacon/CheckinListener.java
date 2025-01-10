package beacon;

import common.BeaconEntry;
import common.BeaconOutput;
import common.Download;
import common.Keystrokes;
import common.ScListener;
import common.Screenshot;

import java.io.Serializable;
import java.util.Map;

public interface CheckinListener {
    void checkin(ScListener scListener, BeaconEntry beaconEntry);

    void output(BeaconOutput beaconOutput);

    void update(String string1, long l, String string2, boolean bl);

    void screenshot(Screenshot screenshot);

    void keystrokes(Keystrokes keystrokes);

    void download(Download download);

    void push(String string, Serializable serializable);

    BeaconEntry resolve(String string);

    BeaconEntry resolveEgress(String string);

    Map<String, BeaconEntry> buildBeaconModel();
}
