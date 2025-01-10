package console;

import javax.swing.JPanel;

public class AssociatedPanel extends JPanel implements Associated {
    protected String apanel_bid = "";

    public AssociatedPanel() {
    }

    public AssociatedPanel(String string) {
        setBeaconID(string);
    }

    public void setBeaconID(String string) {
        this.apanel_bid = string;
    }

    public String getBeaconID() {
        return this.apanel_bid;
    }
}
