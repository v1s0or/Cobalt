package aggressor.windows;

import aggressor.DataManager;
import aggressor.DataUtils;
import common.AObject;
import common.AdjustData;
import common.BeaconEntry;
import common.Screenshot;
import common.TeamQueue;
import cortana.Cortana;
import dialog.DialogUtils;

import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

import ui.DataBrowser;
import ui.DataSelectionListener;
import ui.ZoomableImage;

public class ScreenshotBrowser extends AObject implements AdjustData, DataSelectionListener {
    protected TeamQueue conn = null;

    protected Cortana engine = null;

    protected DataManager data = null;

    protected DataBrowser browser = null;

    protected ZoomableImage viewer = null;

    public ScreenshotBrowser(DataManager dataManager, Cortana cortana, TeamQueue teamQueue) {
        this.engine = cortana;
        this.conn = teamQueue;
        this.data = dataManager;
    }

    public ActionListener cleanup() {
        return this.data.unsubOnClose("screenshots", this);
    }

    public JComponent getContent() {
        LinkedList linkedList = this.data.populateAndSubscribe("screenshots", this);
        this.viewer = new ZoomableImage();
        this.browser = DataBrowser.getBeaconDataBrowser(this.engine, "data", new JScrollPane(this.viewer), linkedList);
        this.browser.addDataSelectionListener(this);
        DialogUtils.setupDateRenderer(this.browser.getTable(), "when");
        return this.browser;
    }

    public void selected(Object object) {
        if (object != null) {
            this.viewer.setIcon(((Screenshot) object).getImage());
        } else {
            this.viewer.setIcon(null);
        }
    }

    public Map format(String string, Object object) {
        Screenshot screenshot = (Screenshot) object;
        BeaconEntry beaconEntry = DataUtils.getBeacon(this.data, screenshot.id());
        if (beaconEntry == null)
            return null;
        Map map = beaconEntry.toMap();
        map.put("when", screenshot.time());
        map.put("data", screenshot);
        map.put("_accent", "");
        return map;
    }

    public void result(String string, Object object) {
        if (this.browser == null)
            return;
        Map map = format(string, object);
        if (map != null)
            this.browser.addEntry(map);
    }
}
