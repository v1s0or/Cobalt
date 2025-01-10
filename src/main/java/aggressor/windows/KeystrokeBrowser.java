package aggressor.windows;

import aggressor.DataManager;
import aggressor.DataUtils;
import common.AObject;
import common.AdjustData;
import common.BeaconEntry;
import common.CommonUtils;
import common.Keystrokes;
import common.TeamQueue;
import console.Colors;
import console.Display;
import cortana.Cortana;
import dialog.DialogUtils;

import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import javax.swing.JComponent;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;

import ui.DataBrowser;
import ui.DataSelectionListener;

public class KeystrokeBrowser extends AObject implements AdjustData, DataSelectionListener {
    protected TeamQueue conn = null;

    protected Cortana engine = null;

    protected DataManager data = null;

    protected DataBrowser browser = null;

    protected Display content = null;

    protected Map sessions = new HashMap();

    protected Colors colors = new Colors(new Properties());

    public KeystrokeBrowser(DataManager dataManager, Cortana cortana, TeamQueue teamQueue) {
        this.engine = cortana;
        this.conn = teamQueue;
        this.data = dataManager;
    }

    public ActionListener cleanup() {
        return this.data.unsubOnClose("keystrokes", this);
    }

    public JComponent getContent() {
        this.data.populateAndSubscribe("keystrokes", this);
        LinkedList linkedList = new LinkedList(this.sessions.values());
        this.content = new Display(new Properties());
        this.browser = DataBrowser.getBeaconDataBrowser(this.engine, "document", this.content, linkedList);
        this.browser.addDataSelectionListener(this);
        DialogUtils.setupDateRenderer(this.browser.getTable(), "when");
        return this.browser;
    }

    public void selected(Object object) {
        if (object != null) {
            StyledDocument styledDocument = (StyledDocument) object;
            this.content.swap(styledDocument);
            this.content.getConsole().setCaretPosition(styledDocument.getLength());
        } else {
            this.content.clear();
        }
    }

    @Override
    public Map format(String string, Object object) {
        final Keystrokes keys = (Keystrokes) object;
        if (!this.sessions.containsKey(keys.id())) {
            BeaconEntry beaconEntry = DataUtils.getBeacon(this.data, keys.id());
            if (beaconEntry == null) {
                return null;
            }
            Map map1 = beaconEntry.toMap();
            map1.put("_accent", "");
            map1.put("document", new DefaultStyledDocument());
            this.sessions.put(keys.id(), map1);
        }
        Map map = (Map) this.sessions.get(keys.id());
        final StyledDocument document = (StyledDocument) map.get("document");
        CommonUtils.runSafe(new Runnable() {
            @Override
            public void run() {
                colors.append(document, keys.getKeystrokes());
                if (content != null && document == content.getConsole().getDocument()) {
                    content.getConsole().scrollRectToVisible(
                            new Rectangle(0, content.getConsole().getHeight() + 1, 1, 1));
                }
            }
        });
        map.put("when", keys.time());
        return map;
    }

    @Override
    public void result(String string, Object object) {
        format(string, object);
        if (this.browser == null) {
            return;
        }
        this.browser.setTable(this.sessions.values());
    }
}
