package aggressor.viz;

import aggressor.AggressorClient;
import common.AObject;
import common.BeaconEntry;
import common.Callback;
import common.CommonUtils;
import common.TabScreenshot;
import dialog.DialogUtils;
import graph.GraphPopup;
import graph.NetworkGraph;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.swing.AbstractAction;
import javax.swing.JComponent;

import sleep.runtime.SleepUtils;

public class PivotGraph extends AObject implements Callback, GraphPopup {

    protected AggressorClient client = null;

    protected NetworkGraph graph = new NetworkGraph();

    protected long last = 0L;

    public PivotGraph(AggressorClient aggressorClient) {
        this.client = aggressorClient;
        this.graph.setGraphPopup(this);
        this.graph.addActionForKey("ctrl pressed P", new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                Image image = PivotGraph.this.graph.getScreenshot();
                byte[] arrby = DialogUtils.toImage((BufferedImage) image, "png");
                PivotGraph.this.client.getConnection().call("aggressor.screenshot", CommonUtils.args(new TabScreenshot("Pivot Graph", arrby)));
                DialogUtils.showInfo("Pushed screenshot to team server");
            }
        });
    }

    public void ready() {
        this.client.getData().subscribe("beacons", this);
    }

    public void showGraphPopup(String[] arrstring, MouseEvent mouseEvent) {
        if (arrstring.length > 0) {
            DialogUtils.showSessionPopup(this.client, mouseEvent, arrstring);
        } else {
            Stack stack = new Stack();
            stack.push(SleepUtils.getScalar(getContent()));
            this.client.getScriptEngine().getMenuBuilder().installMenu(mouseEvent, "pgraph", stack);
        }
    }

    public void showPopup(MouseEvent mouseEvent) {
        Stack stack = new Stack();
        this.client.getScriptEngine().getMenuBuilder().installMenu(mouseEvent, "beacon", stack);
    }

    public JComponent getContent() {
        return this.graph;
    }

    public void result(String string, Object object) {
        if (!this.graph.isShowing())
            return;
        Map<String, BeaconEntry> map = (Map<String, BeaconEntry>) object;
        long l = CommonUtils.dataIdentity(map);
        if (l == this.last)
            return;
        this.last = l;
        this.graph.start();
        if (map.size() > 0)
            this.graph.addNode("", "", "", DialogUtils.TargetVisualization("firewall", 0.0D, false, false), "", "");
        for (BeaconEntry beaconEntry : map.values()) {
            Image image = null;
            if (beaconEntry.isEmpty()) {
                image = DialogUtils.TargetVisualization("unknown", 0.0D, false, false);
            } else {
                image = DialogUtils.TargetVisualization(beaconEntry.getOperatingSystem().toLowerCase(), beaconEntry.getVersion(), beaconEntry.isAdmin(), !beaconEntry.isAlive());
            }
            if (beaconEntry.isEmpty()) {
                if ("".equals(beaconEntry.getNote())) {
                    this.graph.addNode(beaconEntry.getId(), "[unknown]", "", image, "", beaconEntry.getAccent());
                } else {
                    this.graph.addNode(beaconEntry.getId(), "[unknown]\n" + beaconEntry.getNote(), "", image, "", beaconEntry.getAccent());
                }
            } else if (beaconEntry.isSSH()) {
                if ("".equals(beaconEntry.getNote())) {
                    this.graph.addNode(beaconEntry.getId(), beaconEntry.getComputer(), beaconEntry.getUser(), image, beaconEntry.getInternal(), beaconEntry.getAccent());
                } else {
                    this.graph.addNode(beaconEntry.getId(), beaconEntry.getComputer() + "\n" + beaconEntry.getNote(), beaconEntry.getUser(), image, beaconEntry.getInternal(), beaconEntry.getAccent());
                }
            } else if ("".equals(beaconEntry.getNote())) {
                this.graph.addNode(beaconEntry.getId(), beaconEntry.getComputer() + " @ " + beaconEntry.getPid(), beaconEntry.getUser(), image, beaconEntry.getInternal(), beaconEntry.getAccent());
            } else {
                this.graph.addNode(beaconEntry.getId(), beaconEntry.getComputer() + " @ " + beaconEntry.getPid() + "\n" + beaconEntry.getNote(), beaconEntry.getUser(), image, beaconEntry.getInternal(), beaconEntry.getAccent());
            }
            if (beaconEntry.getParentId().length() == 0) {
                if ("".equals(beaconEntry.getExternal())) {
                    this.graph.addEdge("", beaconEntry.getId(), "#FFFF00", "4", "true", "", 2);
                    continue;
                }
                this.graph.addEdge("", beaconEntry.getId(), "#00FF00", "4", "true", "", 2);
            }
        }
        for (BeaconEntry beaconEntry : map.values()) {
            if (beaconEntry.getParentId().length() > 0) {
                byte b;
                String str1 = "";
                String str2 = "";
                if (beaconEntry.getPivotHint().isForward()) {
                    b = 1;
                } else {
                    b = 2;
                }
                if (beaconEntry.getPivotHint().isTCP()) {
                    if (beaconEntry.getLinkState() == 1) {
                        str1 = "#00FFA5";
                        str2 = "";
                    } else {
                        str1 = "#A500FF";
                        str2 = "DISCONNECTED";
                    }
                } else if (beaconEntry.getLinkState() == 1) {
                    str1 = "#FFA500";
                    str2 = "";
                } else {
                    str1 = "#FF0000";
                    str2 = "DISCONNECTED";
                }
                this.graph.addEdge(beaconEntry.getParentId(), beaconEntry.getId(), str1, "4", "false", str2, b);
            }
        }
        this.graph.deleteNodes();
        this.graph.end();
    }
}
