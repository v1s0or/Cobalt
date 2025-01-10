package aggressor.browsers;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import common.AdjustData;
import common.CommonUtils;
import dialog.DialogUtils;
import filter.DataFilter;

import java.util.LinkedList;
import java.util.Map;

public class Beacons extends Sessions implements AdjustData {

    protected DataFilter filter = new DataFilter();

    public Beacons(AggressorClient aggressorClient, boolean bl) {
        super(aggressorClient, bl);
        this.filter.checkBeacon("id", false);
    }

    public Map format(String string, Object object) {
        return !this.filter.test((Map) object) ? null : (Map) object;
    }

    public void result(String string, Object object) {
        if (!this.table.isShowing())
            return;
        LinkedList linkedList = new LinkedList(DataUtils.getBeaconModelFromResult(object));
        linkedList = CommonUtils.apply(string, linkedList, this);
        DialogUtils.setTable(this.table, this.model, linkedList);
    }
}
