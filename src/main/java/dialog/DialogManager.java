package dialog;

import aggressor.AggressorClient;
import aggressor.DataManager;
import aggressor.DataUtils;
import aggressor.dialogs.BeaconChooser;
import aggressor.dialogs.CredentialChooser;
import aggressor.dialogs.InterfaceDialog;
import aggressor.dialogs.MailServerDialog;
import aggressor.dialogs.ProxyServerDialog;
import aggressor.dialogs.ScListenerChooser;
import aggressor.dialogs.SiteChooser;
import common.BeaconEntry;
import common.Callback;
import common.CommonUtils;
import common.TeamQueue;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileSystemView;

import ui.APasswordField;
import ui.ATable;
import ui.ATextField;
import ui.GenericTableModel;
import ui.ListCopyPopup;

public class DialogManager {

    protected HashMap<String, String> options = new HashMap();

    protected LinkedList<DialogListener> listeners = new LinkedList();

    protected LinkedList<DialogListener> listeners2 = new LinkedList();

    protected LinkedList rows = new LinkedList();

    protected JFrame dialog = null;

    protected LinkedList group = null;

    protected Map groups = new HashMap();

    public void addDialogListener(DialogListener dialogListener) {
        this.listeners2.add(dialogListener);
    }

    public void addDialogListenerInternal(DialogListener dialogListener) {
        this.listeners.add(dialogListener);
    }

    public LinkedList getRows() {
        return new LinkedList(this.rows);
    }

    public void startGroup(String string) {
        this.group = new LinkedList();
        this.groups.put(string, this.group);
    }

    public void endGroup() {
        this.group = null;
    }

    public DialogManager(JFrame jFrame) {
        this.dialog = jFrame;
    }

    public void set(String string1, String string2) {
        this.options.put(string1, string2);
    }

    public void set(Map<String, String> map) {
        for (Map.Entry entry : map.entrySet()) {
            this.options.put(entry.getKey() + "", entry.getValue() + "");
        }
    }

    private static void setEnabledSafe(final JComponent c, final boolean state) {
        CommonUtils.runSafe(new Runnable() {
            @Override
            public void run() {
                c.setEnabled(state);
            }
        });
    }

    public JButton action_noclose(final String text) throws IOException {
        JButton jButton = new JButton(text);
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ((JComponent) actionEvent.getSource()).setEnabled(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (DialogListener dialogListener : listeners) {
                            try {
                                dialogListener.dialogAction(actionEvent, options);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        for (DialogListener dialogListener : listeners2) {
                            try {
                                dialogListener.dialogAction(actionEvent, options);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        DialogManager.setEnabledSafe((JComponent) actionEvent.getSource(), true);
                    }
                }, "dialog action: " + text).start();
            }
        });
        return jButton;
    }

    public DialogRow beacon_disabled(String string1, String string2, AggressorClient aggressorClient) {
        DialogRow dialogRow = beacon(string1, string2, aggressorClient);
        dialogRow.get(2).setEnabled(false);
        return dialogRow;
    }

    public DialogRow beacon(final String key, String string2, final AggressorClient client) {
        final DialogRow result = text(key + ".title", string2);
        ((JTextField) result.c[1]).setEditable(false);
        if (this.options.containsKey(key)) {
            BeaconEntry beaconEntry = DataUtils.getBeacon(client.getData(), this.options.get(key) + "");
            if (beaconEntry != null) {
                ((JTextField) result.c[1]).setText(beaconEntry.getUser() + beaconEntry.title(" via "));
            }
        }
        JButton jButton = new JButton("...");
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                new BeaconChooser(client, new SafeDialogCallback() {
                    @Override
                    public void dialogResult(String string) {
                        options.put(key, string);
                        BeaconEntry beaconEntry = DataUtils.getBeacon(client.getData(), string);
                        if (beaconEntry != null) {
                            ((JTextField) result.c[1]).setText(beaconEntry.getUser() + beaconEntry.title(" via "));
                        }
                    }
                }).show();
            }
        });
        result.c[2] = jButton;
        return result;
    }

    public DialogRow interfaces(String string1, String string2, TeamQueue teamQueue, DataManager dataManager) {
        List list = DataUtils.getInterfaceList(dataManager);
        DialogRow dialogRow = combobox(string1, string2, CommonUtils.toArray(list));
        JButton jButton = new JButton("Add");
        jButton.addActionListener(new InterfaceAdd((JComboBox) dialogRow.c[1], teamQueue, dataManager));
        dialogRow.last(jButton);
        return dialogRow;
    }

    public DialogRow exploits(String string1, String string2, AggressorClient aggressorClient) {
        List list = DataUtils.getBeaconExploits(aggressorClient.getData()).exploits();
        return combobox(string1, string2, CommonUtils.toArray(list));
    }

    public DialogRow krbtgt(String string1, String string2, final AggressorClient client) {
        DialogRow dialogRow = text(string1, string2);
        final JTextField textc = (JTextField) dialogRow.c[1];
        JButton jButton = new JButton("...");
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                CredentialChooser credentialChooser = new CredentialChooser(client, new SafeDialogCallback() {
                    @Override
                    public void dialogResult(String string) {
                        String[] arrstring = string.split(" ");
                        textc.setText(arrstring[1]);
                    }
                });
                credentialChooser.getFilter().checkLiteral("user", "krbtgt");
                credentialChooser.getFilter().checkNTLMHash("password", false);
                try {
                    credentialChooser.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        dialogRow.c[2] = jButton;
        return dialogRow;
    }

    public DialogRow label(String string) {
        DialogRow dialogRow = new DialogRow(new JPanel(), new JLabel(string), new JPanel());
        this.rows.add(dialogRow);
        if (this.group != null) {
            this.group.add(dialogRow);
        }
        return dialogRow;
    }

    public DialogRow combobox(final String key, String string2, Object[] arrobject) {
        JLabel jLabel = new JLabel(string2);
        final JComboBox<Object> combobox = new JComboBox<Object>(arrobject);
        combobox.setPreferredSize(new Dimension(240, 0));
        if (this.options.containsKey(key)) {
            combobox.setSelectedItem(this.options.get(key));
        }
        addDialogListenerInternal(new DialogListener() {
            @Override
            public void dialogAction(ActionEvent actionEvent, Map map) {
                map.put(key, combobox.getSelectedItem());
            }
        });
        DialogRow dialogRow = new DialogRow(new JLabel(string2), combobox, new JPanel());
        this.rows.add(dialogRow);
        if (this.group != null) {
            this.group.add(dialogRow);
        }
        return dialogRow;
    }

    public DialogRow attack(String string1, String string2) {
        DialogRow dialogRow = text(string1, string2);
        JTextField jTextField = (JTextField) dialogRow.c[1];
        JButton jButton = new JButton("...");
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
            }
        });
        dialogRow.c[2] = jButton;
        return dialogRow;
    }

    public DialogRow site(String string1, String string2, final TeamQueue conn, final DataManager data) {
        DialogRow dialogRow = text(string1, string2);
        final JTextField textc = (JTextField) dialogRow.c[1];
        JButton jButton = new JButton("...");
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                new SiteChooser(conn, data, new SafeDialogCallback() {
                // new SiteChooser(teamQueue, dataManager, new SafeDialogCallback() {
                    @Override
                    public void dialogResult(String string) {
                        textc.setText(string + "?id=%TOKEN%");
                    }
                }).show();
            }
        });
        dialogRow.c[2] = jButton;
        return dialogRow;
    }

    public DialogRow sc_listener_all(String string1, String string2, final AggressorClient client) {
        DialogRow dialogRow = text(string1, string2);
        final JTextField textc = (JTextField) dialogRow.c[1];
        textc.setEditable(false);
        JButton jButton = new JButton("...");
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ScListenerChooser.ListenersAll(client, new SafeDialogCallback() {
                    @Override
                    public void dialogResult(String string) {
                        textc.setText(string);
                    }
                }).show();
            }
        });
        dialogRow.c[2] = jButton;
        return dialogRow;
    }

    public DialogRow sc_listener_stagers(String string1, String string2, final AggressorClient client) {
        DialogRow dialogRow = text(string1, string2);
        final JTextField textc = (JTextField) dialogRow.c[1];
        textc.setEditable(false);
        JButton jButton = new JButton("...");
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ScListenerChooser.ListenersWithStagers(client, new SafeDialogCallback() {
                    @Override
                    public void dialogResult(String string) {
                        textc.setText(string);
                    }
                }).show();
            }
        });
        dialogRow.c[2] = jButton;
        return dialogRow;
    }

    public DialogRow proxyserver(String string1, String string2, final AggressorClient client) {
        DialogRow dialogRow = text(string1, string2);
        final JTextField textc = (JTextField) dialogRow.c[1];
        JButton jButton = new JButton("...");
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    new ProxyServerDialog(textc.getText(), new SafeDialogCallback() {
                        @Override
                        public void dialogResult(String string) {
                            client.getConnection().call("armitage.broadcast", CommonUtils.args("manproxy", string));
                            textc.setText(string);
                        }
                    }).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        dialogRow.c[2] = jButton;
        return dialogRow;
    }

    public DialogRow mailserver(String string1, String string2) {
        DialogRow dialogRow = text(string1, string2);
        final JTextField textc = (JTextField) dialogRow.c[1];
        JButton jButton = new JButton("...");
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    new MailServerDialog(textc.getText(), new SafeDialogCallback() {
                        @Override
                        public void dialogResult(String string) {
                            textc.setText(string);
                        }
                    }).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        dialogRow.c[2] = jButton;
        return dialogRow;
    }

    public DialogRow file(String string1, String string2) {
        DialogRow dialogRow = text(string1, string2);
        final JTextField textc = (JTextField) dialogRow.c[1];
        JButton jButton = new JButton(FileSystemView.getFileSystemView().getSystemIcon(new File(".")));
        jButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                SafeDialogs.openFile("Choose file", null, null, false, false, new SafeDialogCallback() {
                    @Override
                    public void dialogResult(String string) {
                        textc.setText(string);
                    }
                });
            }
        });
        dialogRow.c[2] = jButton;
        return dialogRow;
    }

    public DialogRow font(String string1, String string2) {
        DialogRow dialogRow = text(string1, string2);
        JButton jButton = new JButton("...");
        final JTextField textc = (JTextField) dialogRow.c[1];
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                FontDialog fontDialog = new FontDialog(Font.decode(textc.getText()));
                fontDialog.addFontChooseListener(new SafeDialogCallback() {
                    @Override
                    public void dialogResult(String string) {
                        textc.setText(string);
                    }
                });
                try {
                    fontDialog.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        dialogRow.c[2] = jButton;
        return dialogRow;
    }

    public DialogRow color(String string1, String string2) {
        DialogRow dialogRow = text(string1, string2);
        final JTextField textc = (JTextField) dialogRow.c[1];
        Color color = Color.black;
        if (textc.getText() != null && textc.getText().length() > 0) {
            color = Color.decode(textc.getText());
        }
        final SolidIcon icon = new SolidIcon(color, 16, 16);
        JButton jButton = new JButton(icon);
        final Color tempcc = color;
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                SafeDialogs.chooseColor("pick a color", tempcc, new SafeDialogCallback() {
                    @Override
                    public void dialogResult(String string) {
                        textc.setText(string);
                        icon.setColor(Color.decode(string));
                    }
                });
            }
        });
        dialogRow.c[2] = jButton;
        return dialogRow;
    }

    public DialogRow file_import(final String key, String string2, ATable aTable, final GenericTableModel model) {
        DialogRow dialogRow = file("_" + key, string2);
        final JTextField textc = (JTextField) dialogRow.c[1];
        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                check();
            }

            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                check();
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                check();
            }

            public void check() {
                model.clear(128);
                File file = new File(textc.getText().trim());
                if (file.exists() && file.canRead() && !file.isDirectory()) {
                    String[] arrstring = CommonUtils.bString(CommonUtils.strrep(CommonUtils.readFile(file.getAbsolutePath()), "\r", "")).split("\n");
                    for (int i = 0; i < arrstring.length; i++) {
                        if (arrstring[i].length() > 0) {
                            String[] arrstring2 = arrstring[i].split("\t");
                            if (arrstring2.length == 1) {
                                model.addEntry(CommonUtils.toMap("To", arrstring2[0], "To_Name", ""));
                            } else if (arrstring2.length >= 2) {
                                model.addEntry(CommonUtils.toMap("To", arrstring2[0], "To_Name", arrstring2[1]));
                            }
                        }
                    }
                }
                model.fireListeners();
            }
        };
        textc.getDocument().addDocumentListener(documentListener);
        addDialogListenerInternal(new DialogListener() {
            @Override
            public void dialogAction(ActionEvent actionEvent, Map map) {
                map.put(key, model.export());
            }
        });
        documentListener.insertUpdate(null);
        return dialogRow;
    }

    public JButton action(String string) throws IOException {
        JButton jButton = action_noclose(string);
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!DialogUtils.isShift(actionEvent)) {
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            }
        });
        return jButton;
    }

    public JButton help(String string) {
        JButton jButton = new JButton("Help");
        jButton.addActionListener(DialogUtils.gotoURL(string));
        return jButton;
    }

    public DialogRow text(String string1, String string2) {
        return text(string1, string2, 20);
    }

    public DialogRow text_disabled(String string1, String string2) {
        DialogRow dialogRow = text(string1, string2, 20);
        dialogRow.get(1).setEnabled(false);
        return dialogRow;
    }

    public DialogRow text(final String key, String string2, int n) {
        final ATextField t = new ATextField(n);
        if (this.options.containsKey(key)) {
            t.setText(this.options.get(key) + "");
        }
        addDialogListenerInternal(new DialogListener() {
            @Override
            public void dialogAction(ActionEvent actionEvent, Map map) {
                if ("".equals(t.getText())) {
                    map.put(key, "");
                } else {
                    map.put(key, t.getText());
                }
            }
        });
        DialogRow dialogRow = new DialogRow(new JLabel(string2), t, new JPanel());
        this.rows.add(dialogRow);
        if (this.group != null) {
            this.group.add(dialogRow);
        }
        return dialogRow;
    }

    public DialogRow list_file(String string1, String string2) {
        return list(string1, string2, "file", 64);
    }

    public DialogRow list_text(String string1, String string2) {
        return list(string1, string2, "text", 160);
    }

    public DialogRow list_csv(final String key, String string2, final String desc, final String defv, int n) {
        final JList<String> list = new JList<String>();
        JScrollPane jScrollPane = new JScrollPane(list, 20, 30);
        jScrollPane.setPreferredSize(new Dimension(240, n));
        if (this.options.containsKey(key)) {
            String str = this.options.get(key) + "";
            if (!"".equals(str)) {
                list.setListData(CommonUtils.toArray(str));
            }
        }
        addDialogListenerInternal(new DialogListener() {
            @Override
            public void dialogAction(ActionEvent actionEvent, Map map) {
                if (list.getModel().getSize() == 0) {
                    map.put(key, "");
                } else {
                    LinkedList linkedList = new LinkedList();
                    for (int i = 0; i < list.getModel().getSize(); i++) {
                        linkedList.add(list.getModel().getElementAt(i));
                    }
                    map.put(key, CommonUtils.join(linkedList, ", "));
                }
            }
        });
        JButton jButton1 = new JButton(DialogUtils.getIcon("resources/cc/black/png/delete_icon&16.png"));
        jButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                list.setListData(new String[0]);
                options.put(key, "");
            }
        });
        JButton jButton2 = new JButton(DialogUtils.getIcon("resources/cc/black/png/sq_minus_icon&16.png"));
        jButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                LinkedList linkedList = new LinkedList();
                for (int i = 0; i < list.getModel().getSize(); i++) {
                    boolean bool = false;
                    for (int j = 0; j < list.getSelectedIndices().length; j++) {
                        if (i == list.getSelectedIndices()[j]) {
                            bool = true;
                        }
                    }
                    if (!bool) {
                        linkedList.add(list.getModel().getElementAt(i));
                    }
                }
                list.setListData(CommonUtils.toArray(linkedList));
                options.put(key, CommonUtils.join(linkedList, ", "));
            }
        });
        JButton jButton3 = new JButton(DialogUtils.getIcon("resources/cc/black/png/sq_plus_icon&16.png"));
        jButton3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                SafeDialogs.ask(desc, defv, new SafeDialogCallback() {
                    @Override
                    public void dialogResult(String string) {
                        LinkedList linkedList = new LinkedList();
                        for (int i = 0; i < list.getModel().getSize(); i++) {
                            linkedList.add(list.getModel().getElementAt(i));
                        }
                        String[] arrstring = CommonUtils.toArray(string);
                        for (int i = 0; i < arrstring.length; i++) {
                            linkedList.add(arrstring[i]);
                        }
                        list.setListData(CommonUtils.toArray(linkedList));
                        options.put(key, CommonUtils.join(linkedList, ", "));
                    }
                });
            }
        });
        new ListCopyPopup(list);
        DialogRow dialogRow = new DialogRow(new JLabel(string2), jScrollPane, DialogUtils.stack(jButton3, jButton2, jButton1));
        this.rows.add(dialogRow);
        if (this.group != null) {
            this.group.add(dialogRow);
        }
        return dialogRow;
    }

    public DialogRow list(final String key, String string2, String string3, int n) {
        final JList<String> list = new JList();
        JScrollPane jScrollPane = new JScrollPane(list, 20, 30);
        jScrollPane.setPreferredSize(new Dimension(240, n));
        if (this.options.containsKey(key)) {
            String str = this.options.get(key) + "";
            if (!"".equals(str)) {
                list.setListData(str.split("!!"));
            }
        }
        addDialogListenerInternal(new DialogListener() {
            @Override
            public void dialogAction(ActionEvent actionEvent, Map map) {
                if (list.getModel().getSize() == 0) {
                    map.put(key, "");
                } else {
                    LinkedList linkedList = new LinkedList();
                    for (int i = 0; i < list.getModel().getSize(); i++) {
                        linkedList.add(list.getModel().getElementAt(i));
                    }
                    map.put(key, CommonUtils.join(linkedList, "!!"));
                }
            }
        });
        JButton jButton = new JButton(DialogUtils.getIcon("resources/cc/black/png/sq_minus_icon&16.png"));
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                LinkedList linkedList = new LinkedList();
                for (int i = 0; i < list.getModel().getSize(); i++) {
                    boolean bool = false;
                    for (int j = 0; j < list.getSelectedIndices().length; j++) {
                        if (i == list.getSelectedIndices()[j]) {
                            bool = true;
                        }
                    }
                    if (!bool) {
                        linkedList.add(list.getModel().getElementAt(i));
                    }
                }
                list.setListData(CommonUtils.toArray(linkedList));
                options.put(key, CommonUtils.join(linkedList, "!!"));
            }
        });
        JComponent jComponent = null;
        if ("file".equals(string3)) {
            JButton jButton1 = new JButton(FileSystemView.getFileSystemView().getSystemIcon(new File(".")));
            jButton1.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    SafeDialogs.openFile("Choose a file", null, null, false, false, new SafeDialogCallback() {
                        @Override
                        public void dialogResult(String string) {
                            LinkedList linkedList = new LinkedList();
                            for (int i = 0; i < list.getModel().getSize(); i++) {
                                linkedList.add(list.getModel().getElementAt(i));
                            }
                            linkedList.add(string);
                            list.setListData(CommonUtils.toArray(linkedList));
                            options.put(key, CommonUtils.join(linkedList, "!!"));
                        }
                    });
                }
            });
            jComponent = DialogUtils.stack(jButton1, jButton);
        } else {
            jComponent = DialogUtils.stack(jButton);
        }
        DialogRow dialogRow = new DialogRow(new JLabel(string2), jScrollPane, jComponent);
        this.rows.add(dialogRow);
        if (this.group != null)
            this.group.add(dialogRow);
        return dialogRow;
    }

    public DialogRow text_big(String string1, String string2) {
        return text_big(string1, string2, 20);
    }

    public DialogRow text_big(final String key, String string2, int n) {
        final JTextArea t = new JTextArea();
        t.setRows(3);
        t.setColumns(n);
        t.setLineWrap(true);
        t.setWrapStyleWord(true);
        if (this.options.containsKey(key)) {
            t.setText(this.options.get(key) + "");
        }
        addDialogListenerInternal(new DialogListener() {
            @Override
            public void dialogAction(ActionEvent actionEvent, Map map) {
                if ("".equals(t.getText())) {
                    map.put(key, "");
                } else {
                    map.put(key, t.getText());
                }
            }
        });
        DialogRow dialogRow = new DialogRow(new JLabel(string2), new JScrollPane(t), new JPanel());
        this.rows.add(dialogRow);
        if (this.group != null) {
            this.group.add(dialogRow);
        }
        return dialogRow;
    }

    public DialogRow password(final String key, String string2, int n) {
        final APasswordField t = new APasswordField(n);
        if (this.options.containsKey(key)) {
            t.setText(this.options.get(key) + "");
        }
        addDialogListenerInternal(new DialogListener() {
            @Override
            public void dialogAction(ActionEvent actionEvent, Map map) {
                if ("".equals(t.getText())) {
                    map.remove(key);
                } else {
                    map.put(key, t.getText());
                }
            }
        });
        DialogRow dialogRow = new DialogRow(new JLabel(string2), t, new JPanel());
        this.rows.add(dialogRow);
        if (this.group != null) {
            this.group.add(dialogRow);
        }
        return dialogRow;
    }

    public JComponent layout() {
        return layout(this.rows);
    }

    public JComponent layout(String string) {
        LinkedList linkedList = (LinkedList) this.groups.get(string);
        return layout(linkedList);
    }

    public JComponent row() {
        if (this.rows.size() != 1) {
            throw new RuntimeException("Can only layout a row with one component!");
        }
        DialogRow dialogRow = (DialogRow) this.rows.get(0);
        JPanel jPanel = new JPanel();
        jPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jPanel.setLayout(new BorderLayout(5, 5));
        jPanel.add(dialogRow.get(0), "West");
        jPanel.add(dialogRow.get(1), "Center");
        jPanel.add(dialogRow.get(2), "East");
        return jPanel;
    }

    public JComponent layout(LinkedList<DialogRow> linkedList) {
        JPanel jPanel = new JPanel();
        GroupLayout groupLayout = new GroupLayout(jPanel);
        groupLayout.setAutoCreateGaps(true);
        groupLayout.setAutoCreateContainerGaps(true);
        jPanel.setLayout(groupLayout);
        GroupLayout.SequentialGroup sequentialGroup1 = groupLayout.createSequentialGroup();
        for (int i = 0; i < 3; i++) {
            GroupLayout.ParallelGroup parallelGroup = groupLayout.createParallelGroup();
            for (DialogRow dialogRow : linkedList) {
                parallelGroup.addComponent(dialogRow.get(i));
            }
            sequentialGroup1.addGroup(parallelGroup);
        }
        groupLayout.setHorizontalGroup(sequentialGroup1);
        GroupLayout.SequentialGroup sequentialGroup2 = groupLayout.createSequentialGroup();
        for (DialogRow dialogRow : linkedList) {
            GroupLayout.ParallelGroup parallelGroup = groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE);
            for (int i = 0; i < 3; i++) {
                parallelGroup.addComponent(dialogRow.get(i));
            }
            sequentialGroup2.addGroup(parallelGroup);
        }
        groupLayout.setVerticalGroup(sequentialGroup2);
        return jPanel;
    }

    public DialogRow checkbox_add(String string1, String string2, String string3) {
        return checkbox_add(string1, string2, string3, true);
    }

    public DialogRow checkbox_add(String string1, String string2, String string3, boolean bl) {
        JLabel jLabel = new JLabel(string2);
        JCheckBox jCheckBox = checkbox(string1, string3);
        if (!bl) {
            jLabel.setEnabled(false);
            jCheckBox.setEnabled(false);
        }
        DialogRow dialogRow = new DialogRow(jLabel, jCheckBox, new JPanel());
        this.rows.add(dialogRow);
        if (this.group != null) {
            this.group.add(dialogRow);
        }
        return dialogRow;
    }

    public JCheckBox checkbox(final String key, String string2) {
        final JCheckBox checkbox = new JCheckBox(string2);
        if ("true".equals(this.options.get(key))) {
            checkbox.setSelected(true);
        } else {
            checkbox.setSelected(false);
        }
        addDialogListenerInternal(new DialogListener() {
            @Override
            public void dialogAction(ActionEvent actionEvent, Map map) {
                if (checkbox.isSelected()) {
                    map.put(key, "true");
                } else {
                    map.put(key, "false");
                }
            }
        });
        return checkbox;
    }

    private static class InterfaceAdd implements ActionListener, Callback {
        protected JComboBox mybox;

        protected TeamQueue conn;

        protected DataManager data;

        public InterfaceAdd(JComboBox jComboBox, TeamQueue teamQueue, DataManager dataManager) {
            this.mybox = jComboBox;
            this.conn = teamQueue;
            this.data = dataManager;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            InterfaceDialog interfaceDialog = new InterfaceDialog(this.conn, this.data);
            interfaceDialog.notify(this);
            try {
                interfaceDialog.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void result(String string, final Object value) {
            CommonUtils.runSafe(new Runnable() {
                @Override
                public void run() {
                    mybox.addItem(value + "");
                    mybox.setSelectedItem(value + "");
                }
            });
        }
    }

    public static final class DialogRow {
        public JComponent[] c = new JComponent[3];

        public DialogRow(JComponent jComponent, JComponent jComponent2, JComponent jComponent3) {
            this.c[0] = jComponent;
            this.c[1] = jComponent2;
            this.c[2] = jComponent3;
        }

        public JComponent get(int n) {
            return this.c[n];
        }

        public void last(JComponent jComponent) {
            this.c[2] = jComponent;
        }
    }
}
