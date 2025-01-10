package ui;

import common.CommonUtils;
import dialog.SolidIcon;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JPanel;

public class ColorPanel extends JPanel {

    protected LinkedList<JButton> buttons = new LinkedList();

    protected Map fcolors = new HashMap();

    protected Map bcolors = new HashMap();

    public ColorPanel() {
        setLayout(new FlowLayout(0));
        add(" ", "", Color.BLACK, Color.WHITE);
        add("G", "good", ATable.FORE_GOOD, ATable.BACK_GOOD);
        add("B", "bad", ATable.FORE_BAD, ATable.BACK_BAD);
        add("N", "neutral", ATable.FORE_NEUTRAL, ATable.BACK_NEUTRAL);
        add("I", "ignore", ATable.FORE_IGNORE, ATable.BACK_IGNORE);
        add("C", "cancel", ATable.FORE_CANCEL, ATable.BACK_CANCEL);
    }

    public static boolean isColorAction(String string) {
        Set set = CommonUtils.toSet("good, bad, neutral, ignore, cancel");
        set.add("");
        return set.contains(string);
    }

    public Color getForeColor(String string) {
        return (string == null || "".equals(string)) ? null : (Color) this.fcolors.get(string);
    }

    public Color getBackColor(String string) {
        return (string == null || "".equals(string)) ? null : (Color) this.bcolors.get(string);
    }

    public void addActionListener(ActionListener actionListener) {
        for (JButton jButton : this.buttons)
            jButton.addActionListener(actionListener);
    }

    public void add(String string1, String string2, Color paramColor1, Color paramColor2) {
        JButton jButton = new JButton("");
        jButton.setIcon(new SolidIcon(paramColor2, 16, 16));
        jButton.setForeground(paramColor1);
        jButton.setBackground(paramColor2);
        jButton.setOpaque(false);
        jButton.setActionCommand(string2);
        this.fcolors.put(string2, paramColor1);
        this.bcolors.put(string2, paramColor2);
        add(jButton);
        this.buttons.add(jButton);
    }
}
