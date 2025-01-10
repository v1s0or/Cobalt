package ui;

import aggressor.ui.UseSynthetica;
import dialog.DialogManager;
import dialog.DialogUtils;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class Navigator extends JComponent implements ListSelectionListener {
    protected CardLayout options = new CardLayout();

    protected JList navigator = new JList();

    protected JPanel switcher = new JPanel();

    protected Map icons = new HashMap();

    public Navigator() {
        this.switcher.setLayout(this.options);
        this.navigator.setFixedCellWidth(125);
        setLayout(new BorderLayout());
        add(DialogUtils.wrapComponent(new JScrollPane(this.navigator), 5), "West");
        add(DialogUtils.wrapComponent(this.switcher, 5), "Center");
        this.navigator.setCellRenderer(new Navigator.CellRenderer());
        this.navigator.addListSelectionListener(this);
        this.navigator.setModel(new DefaultListModel());
    }

    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        this.options.show(this.switcher, (String) this.navigator.getSelectedValue());
    }

    public void set(String string) {
        this.navigator.setSelectedValue(string, true);
        this.options.show(this.switcher, string);
    }

    public void addPage(String string1, Icon paramIcon, String string2, JComponent jComponent) {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());
        jPanel.add(DialogUtils.description(string2), "North");
        jPanel.add(DialogUtils.top(jComponent), "Center");
        this.icons.put(string1, paramIcon);
        DefaultListModel defaultListModel = (DefaultListModel) this.navigator.getModel();
        defaultListModel.addElement(string1);
        this.switcher.add(jPanel, string1);
    }

    public static void main(String[] arrstring) throws IOException {
        new UseSynthetica().setup();
        JFrame jFrame = DialogUtils.dialog("Hello World", 640, 480);
        Navigator navigator1 = new Navigator();
        DialogManager dialogManager = new DialogManager(jFrame);
        dialogManager.startGroup("console");
        dialogManager.text("user", "User:", 20);
        dialogManager.text("pass", "Password:", 20);
        dialogManager.text("host", "Host:", 20);
        dialogManager.text("port", "Port:", 10);
        dialogManager.endGroup();
        navigator1.addPage("Console", new ImageIcon("./resources/cc/black/png/monitor_icon&16.png"), "This is your opportunity to edit console preferences", dialogManager.layout("console"));
        dialogManager.startGroup("console2");
        dialogManager.text("user", "User A:", 20);
        dialogManager.text("pass", "Password:", 20);
        dialogManager.text("host", "Host:", 20);
        dialogManager.text("port", "Port:", 10);
        dialogManager.text("port", "Port:", 10);
        dialogManager.text("port", "Port:", 10);
        dialogManager.endGroup();
        navigator1.addPage("Console II", new ImageIcon("./resources/cc/black/png/monitor_icon&16.png"), "This is another opportunity to edit stuff. I think you know the drill by now.", dialogManager.layout("console2"));
        jFrame.add(navigator1, "Center");
        jFrame.add(DialogUtils.center(dialogManager.action("Close")), "South");
        jFrame.setVisible(true);
    }

    private class CellRenderer extends JLabel implements ListCellRenderer {
        private CellRenderer() {
        }

        public Component getListCellRendererComponent(JList param1JList, Object object, int n, boolean bl, boolean bl2) {
            String str = object.toString();
            setText(str);
            setIcon((Icon) Navigator.this.icons.get(object));
            if (bl) {
                setBackground(param1JList.getSelectionBackground());
                setForeground(param1JList.getSelectionForeground());
            } else {
                setBackground(param1JList.getBackground());
                setForeground(param1JList.getForeground());
            }
            setEnabled(param1JList.isEnabled());
            setFont(param1JList.getFont());
            setOpaque(true);
            return this;
        }
    }
}
