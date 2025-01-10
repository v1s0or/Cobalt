package aggressor.windows;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import aggressor.browsers.Beacons;
import common.AObject;
import common.BeaconEntry;
import common.CommonUtils;
import dialog.DialogUtils;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class BeaconBrowser extends AObject implements ActionListener {

    protected AggressorClient client = null;

    protected Beacons browser;

    public BeaconBrowser(AggressorClient aggressorClient) {
        this.client = aggressorClient;
        this.browser = new Beacons(aggressorClient, true);
    }

    public ActionListener cleanup() {
        return this.browser.cleanup();
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if ("Interact".equals(actionEvent.getActionCommand())) {
            String str = this.browser.getSelectedValue() + "";
            BeaconEntry beaconEntry = DataUtils.getBeacon(this.client.getData(), str);
            BeaconConsole beaconConsole = new BeaconConsole(str, this.client);
            this.client.getTabManager().addTab(beaconEntry.title(), beaconConsole.getConsole(), beaconConsole.cleanup(), "Beacon console");
        } else if ("Remove".equals(actionEvent.getActionCommand())) {
            Object[] arrobject = this.browser.getSelectedValues();
            for (byte b = 0; b < arrobject.length; b++)
                this.client.getConnection().call("beacons.remove", CommonUtils.args(arrobject[b]));
        }
    }

    public JComponent getContent() {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());
        JButton jButton1 = new JButton("Interact");
        JButton jButton2 = new JButton("Remove");
        JButton jButton3 = new JButton("Help");
        jButton1.addActionListener(this);
        jButton2.addActionListener(this);
        jButton3.addActionListener(DialogUtils.gotoURL("https://www.cobaltstrike.com/help-beacon"));
        jPanel.add(this.browser.getContent(), "Center");
        jPanel.add(DialogUtils.center(jButton1, jButton2, jButton3), "South");
        return jPanel;
    }
}
