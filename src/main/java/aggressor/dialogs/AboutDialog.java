package aggressor.dialogs;

import aggressor.Aggressor;
import common.AObject;
import common.CommonUtils;
import dialog.DialogUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

public class AboutDialog extends AObject {
    public void show() {
        JFrame jFrame = DialogUtils.dialog("About", 320, 200);
        jFrame.setLayout(new BorderLayout());
        JLabel jLabel = new JLabel(DialogUtils.getIcon("resources/armitage-logo.gif"));
        jLabel.setBackground(Color.black);
        jLabel.setForeground(Color.gray);
        jLabel.setOpaque(true);
        JTextArea jTextArea = new JTextArea();
        jTextArea.setBackground(Color.black);
        jTextArea.setForeground(Color.gray);
        jTextArea.setEditable(false);
        jTextArea.setFocusable(false);
        jTextArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jTextArea.setOpaque(false);
        jTextArea.setLineWrap(true);
        jTextArea.setWrapStyleWord(true);
        String str1 = CommonUtils.bString(CommonUtils.readResource("resources/about.html"));
        jLabel.setText(str1);
        jTextArea.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
        ((DefaultCaret) jTextArea.getCaret()).setUpdatePolicy(1);
        JScrollPane jScrollPane = new JScrollPane(jTextArea, 22, 31);
        jScrollPane.setPreferredSize(new Dimension(jScrollPane.getWidth(), 100));
        String str2 = CommonUtils.bString(CommonUtils.readResource("resources/credits.txt"));
        jTextArea.setText(str2);
        jScrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jFrame.add(jLabel, "Center");
        jFrame.add(jScrollPane, "South");
        jFrame.pack();
        jFrame.setLocationRelativeTo(Aggressor.getFrame());
        jFrame.setVisible(true);
    }
}
