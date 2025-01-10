package aggressor;

import common.CommonUtils;
import common.Keys;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.MenuSelectionManager;

import ui.ColorPanel;
import ui.QueryRows;

public class ColorManager implements ActionListener {
    protected ColorPanel colors = new ColorPanel();

    protected String prefix;

    protected AggressorClient client;

    protected QueryRows rows;

    public ColorManager(AggressorClient aggressorClient, QueryRows queryRows, String string) {
        this.colors.addActionListener(this);
        this.prefix = string;
        this.client = aggressorClient;
        this.rows = queryRows;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        Map[] arrmap = this.rows.getSelectedRows();
        Map<String, String> hashMap = new HashMap();
        hashMap.put("_accent", actionEvent.getActionCommand());
        for (int i = 0; i < arrmap.length; i++) {
            this.client.getConnection().call(this.prefix + ".update", CommonUtils.args(Keys.ToKey(this.prefix, arrmap[i]), hashMap));
        }
        this.client.getConnection().call(this.prefix + ".push");
        MenuSelectionManager.defaultManager().clearSelectedPath();
    }

    public JComponent getColorPanel() {
        return this.colors;
    }
}
