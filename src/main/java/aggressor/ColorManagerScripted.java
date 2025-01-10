package aggressor;

import common.CommonUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.MenuSelectionManager;

import ui.ColorPanel;

public class ColorManagerScripted implements ActionListener {

    protected ColorPanel colors = new ColorPanel();

    protected String prefix;

    protected AggressorClient client;

    protected String[] ids;

    public ColorManagerScripted(AggressorClient aggressorClient, String string, String[] arrstring) {
        this.colors.addActionListener(this);
        this.prefix = string;
        this.client = aggressorClient;
        this.ids = arrstring;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        Map<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("_accent", actionEvent.getActionCommand());
        for (String id : this.ids) {
            this.client.getConnection().call(this.prefix + ".update", CommonUtils.args(id, hashMap));
        }
        this.client.getConnection().call(this.prefix + ".push");
        MenuSelectionManager.defaultManager().clearSelectedPath();
    }

    public JComponent getColorPanel() {
        return this.colors;
    }
}
