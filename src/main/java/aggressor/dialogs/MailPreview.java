package aggressor.dialogs;

import common.AObject;
import common.CommonUtils;
import common.MudgeSanity;
import console.Display;
import dialog.DialogUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import mail.Eater;
import phish.PhishingUtils;
import ui.ATextField;

public class MailPreview extends AObject implements ActionListener {

    protected JFrame dialog = null;

    protected String templatef = null;

    protected String attachf = null;

    protected LinkedList contacts = null;

    protected String urlv = null;

    protected String cRaw = null;

    protected String cHtml = null;

    protected String cText = null;

    public boolean processOptions() {
        try {
            _processOptions();
            return true;
        } catch (Exception exception) {
            DialogUtils.showError("Trouble processing " + this.templatef + ":\n" + exception.getMessage());
            MudgeSanity.logException("process phishing preview", exception, false);
            return false;
        }
    }

    public void _processOptions() throws IOException {
        Eater eater1 = new Eater(this.templatef);
        if (!"".equals(this.attachf) && this.attachf.length() > 0 && new File(this.attachf).exists()) {
            eater1.attachFile(this.attachf);
        }
        Map map = (Map) CommonUtils.pick(this.contacts);
        String str1 = (String) map.get("To");
        String str2 = (String) map.get("To_Name") + "";
        byte[] arrby = eater1.getMessage(null, str2.length() > 0 ? str2 + " <" + str1 + ">" : str1);
        String str3 = PhishingUtils.updateMessage(CommonUtils.bString(arrby), map, this.urlv, "1234567890ab");
        Eater eater2 = new Eater(new ByteArrayInputStream(CommonUtils.toBytes(str3)));
        this.cHtml = eater2.getMessageEntity("text/html");
        this.cText = eater2.getMessageEntity("text/plain");
        this.cRaw = str3;
        eater1.done();
        eater2.done();
    }

    public MailPreview(Map map) {
        this.templatef = DialogUtils.string(map, "template");
        this.attachf = DialogUtils.string(map, "attachment");
        this.contacts = (LinkedList) map.get("targets");
        this.urlv = DialogUtils.string(map, "url");
    }

    public void actionPerformed(ActionEvent actionEvent) {
        this.dialog.setVisible(false);
        this.dialog.dispose();
    }

    public JComponent buildRaw() {
        Display display = new Display(new Properties());
        display.setFont(Font.decode("Monospaced BOLD 14"));
        display.setForeground(Color.decode("#ffffff"));
        display.setBackground(Color.decode("#000000"));
        display.setTextDirect(this.cRaw);
        return display;
    }

    public byte[] buildHTMLScreenshot() {
        JEditorPane jEditorPane = new JEditorPane();
        jEditorPane.setContentType("text/html");
        DialogUtils.workAroundEditorBug(jEditorPane);
        jEditorPane.setEditable(false);
        jEditorPane.setOpaque(true);
        jEditorPane.setCaretPosition(0);
        jEditorPane.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jEditorPane.setText(this.cHtml);
        jEditorPane.setSize(new Dimension(640, 480));
        return DialogUtils.screenshot(jEditorPane);
    }

    public JComponent buildHTML() {
        final ATextField aTextField = new ATextField();
        final JEditorPane jEditorPane = new JEditorPane();
        jEditorPane.setContentType("text/html");
        DialogUtils.workAroundEditorBug(jEditorPane);
        jEditorPane.setEditable(false);
        jEditorPane.setOpaque(true);
        jEditorPane.setCaretPosition(0);
        jEditorPane.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jEditorPane.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {
                String str = hyperlinkEvent.getEventType() + "";
                if (str.equals("ENTERED")) {
                    aTextField.setText(hyperlinkEvent.getURL() + "");
                    aTextField.setCaretPosition(0);
                } else if (str.equals("EXITED")) {
                    aTextField.setText("");
                } else if (str.equals("ACTIVATED")) {
                    DialogUtils.showInput(MailPreview.this.dialog, "You clicked", hyperlinkEvent.getURL() + "");
                }
            }
        });
        new Thread(new Runnable() {
            public void run() {
                jEditorPane.setText(MailPreview.this.cHtml);
            }
        }, "buildHTML").start();
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());
        jPanel.add(new JScrollPane(jEditorPane), "Center");
        jPanel.add(aTextField, "South");
        return jPanel;
    }

    public JComponent buildText() {
        JEditorPane jEditorPane = new JEditorPane();
        jEditorPane.setContentType("text/plain");
        jEditorPane.setText(this.cText);
        jEditorPane.setEditable(false);
        jEditorPane.setOpaque(true);
        jEditorPane.setCaretPosition(0);
        jEditorPane.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        return jEditorPane;
    }

    public void show() throws IOException {
        if (!processOptions()) {
            return;
        }
        this.dialog = DialogUtils.dialog("Preview", 640, 480);
        this.dialog.setLayout(new BorderLayout());
        JTabbedPane jTabbedPane = new JTabbedPane();
        jTabbedPane.addTab("Raw", buildRaw());
        jTabbedPane.addTab("HTML", buildHTML());
        jTabbedPane.addTab("Text", new JScrollPane(buildText()));
        JButton jButton = new JButton("Close");
        jButton.addActionListener(this);
        this.dialog.add(jTabbedPane, "Center");
        this.dialog.add(DialogUtils.center(jButton), "South");
        this.dialog.setVisible(true);
        this.dialog.show();
    }
}
