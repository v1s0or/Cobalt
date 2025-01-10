package dialog;

import common.CommonUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class FontDialog implements ItemListener, DialogListener {

    protected Font font;

    protected JLabel preview;

    protected JComboBox size;

    protected JComboBox family;

    protected JComboBox style;

    protected LinkedList<SafeDialogCallback> listeners = new LinkedList();

    protected JFrame dialog;

    public FontDialog(Font paramFont) {
        this.font = paramFont;
    }

    public void addFontChooseListener(SafeDialogCallback safeDialogCallback) {
        this.listeners.add(safeDialogCallback);
    }

    public JComboBox act(DialogManager.DialogRow dialogRow) {
        JComboBox jComboBox = (JComboBox) dialogRow.c[1];
        jComboBox.addItemListener(this);
        return jComboBox;
    }

    public String getResult() {
        return this.family.getSelectedItem() + "-"
                + this.style.getSelectedItem().toString().toUpperCase() + "-" + this.size.getSelectedItem();
    }

    public void itemStateChanged(ItemEvent itemEvent) {
        Font font1 = Font.decode(getResult());
        this.preview.setFont(font1);
        this.preview.revalidate();
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        for (SafeDialogCallback safeDialogCallback : this.listeners) {
            safeDialogCallback.dialogResult(getResult());
        }
        this.dialog.dispose();
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog("Choose a font", 640, 240);
        this.dialog.setLayout(new BorderLayout());
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        dialogManager.set("size", this.font.getSize() + "");
        dialogManager.set("family", this.font.getFamily());
        String str = "Plain";
        if (this.font.isItalic()) {
            str = "Italic";
        } else if (this.font.isBold()) {
            str = "Bold";
        }
        dialogManager.set("style", str);
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
        this.family = act(dialogManager.combobox("family", "Family",
                graphicsEnvironment.getAvailableFontFamilyNames()));
        this.style = act(dialogManager.combobox("style", "Style",
                CommonUtils.toArray("Bold, Italic, Plain")));
        this.size = act(dialogManager.combobox("size", "Size",
                CommonUtils.toArray("5, 8, 9, 10, 11, 12, 13, 14, 15, 16, 20, 23, 26, 30, 33, 38")));
        this.preview = new JLabel("nEWBS gET pWNED by km-r4d h4x0rz 肉鸡");
        this.preview.setFont(this.font);
        this.preview.setBackground(Color.white);
        this.preview.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.preview.setOpaque(true);
        JButton jButton = dialogManager.action("Choose");
        this.dialog.add(dialogManager.layout(), "North");
        this.dialog.add(this.preview, "Center");
        this.dialog.add(DialogUtils.center(jButton), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
        this.dialog.show();
    }
}
