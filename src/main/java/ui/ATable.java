package ui;

import common.CommonUtils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;

public class ATable extends JTable {

    public static final String indicator = " \u25aa";

    protected boolean alternateBackground = false;

    protected TableClickListener clickl = new TableClickListener();

    protected int[] selected = null;

    public static final Color BACK_NEUTRAL = new Color(255, 235, 156);

    public static final Color BACK_GOOD = new Color(198, 239, 206);

    public static final Color BACK_BAD = new Color(255, 199, 206);

    public static final Color BACK_IGNORE = new Color(165, 165, 165);

    public static final Color BACK_CANCEL = new Color(61, 87, 158);

    public static final Color FORE_NEUTRAL = new Color(181, 107, 6);

    public static final Color FORE_GOOD = new Color(47, 75, 47);

    public static final Color FORE_BAD = new Color(173, 32, 40);

    public static final Color FORE_IGNORE = Color.WHITE;

    public static final Color FORE_CANCEL = Color.WHITE;

    public void markSelections() {
        this.selected = getSelectedRows();
    }

    public void setPopupMenu(TablePopup paramTablePopup) {
        this.clickl.setPopup(paramTablePopup);
    }

    public void fixSelection() {
        if (this.selected.length == 0) {
            return;
        }
        getSelectionModel().setValueIsAdjusting(true);
        int i = getModel().getRowCount();
        for (int value : this.selected) {
            if (value < i) {
                getSelectionModel().addSelectionInterval(value, value);
            }
        }
        getSelectionModel().setValueIsAdjusting(false);
    }

    public void restoreSelections() {
        CommonUtils.runSafe(new Runnable() {

            @Override
            public void run() {
                ATable.this.fixSelection();
            }
        });
    }

    public static TableCellRenderer getDefaultTableRenderer(JTable jTable, final TableModel model) {
        final HashSet<String> specialitems = new HashSet();
        specialitems.add("Wordlist");
        specialitems.add("PAYLOAD");
        specialitems.add("RHOST");
        specialitems.add("RHOSTS");
        specialitems.add("Template");
        specialitems.add("DICTIONARY");
        specialitems.add("NAMELIST");
        specialitems.add("SigningKey");
        specialitems.add("SigningCert");
        specialitems.add("WORDLIST");
        specialitems.add("SESSION");
        specialitems.add("REXE");
        specialitems.add("EXE::Custom");
        specialitems.add("EXE::Template");
        specialitems.add("USERNAME");
        specialitems.add("PASSWORD");
        specialitems.add("SMBUser");
        specialitems.add("SMBPass");
        specialitems.add("INTERFACE");
        specialitems.add("URL");
        specialitems.add("PATH");
        specialitems.add("SCRIPT");
        specialitems.add("KEY_PATH");
        return new TableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable jTable, Object object,
                                                           boolean bl, boolean bl2,
                                                           int n1, int n2) {
                TableCellRenderer tableCellRenderer = jTable.getDefaultRenderer(String.class);
                String str = (object != null ? object : "") + "";
                if (specialitems.contains(str) || str.indexOf("FILE") != -1) {
                    str = str + indicator;
                }
                JComponent jComponent = (JComponent) tableCellRenderer
                        .getTableCellRendererComponent(jTable, str, bl, false, n1, n2);
                jComponent.setToolTipText(
                        ((GenericTableModel) model).getValueAtColumn(jTable, n1, "Tooltip")
                                + "");
                return jComponent;
            }
        };
    }

    public static TableCellRenderer getFileTypeTableRenderer(final GenericTableModel model) {
        return new TableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable jTable, Object object,
                                                           boolean bl, boolean bl2,
                                                           int n1, int n2) {
                TableCellRenderer tableCellRenderer = jTable.getDefaultRenderer(String.class);
                JComponent jComponent = (JComponent) tableCellRenderer
                        .getTableCellRendererComponent(jTable, "", bl, false, n1, n2);
                jComponent.setEnabled(true);
                if ("dir".equals(object)) {
                    ((JLabel) jComponent).setIcon(UIManager.getIcon("FileView.directoryIcon"));
                    if (model.getValueAt(jTable, n1, "cache") == Boolean.FALSE) {
                        jComponent.setEnabled(false);
                    }
                } else if ("drive".equals(object)) {
                    ((JLabel) jComponent).setIcon(UIManager.getIcon("FileView.hardDriveIcon"));
                } else {
                    ((JLabel) jComponent).setIcon(UIManager.getIcon("FileView.fileIcon"));
                }
                return jComponent;
            }
        };
    }

    public static TableCellRenderer getListenerStatusRenderer(final GenericTableModel model) {
        return new TableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable jTable, Object object,
                                                           boolean bl, boolean bl2,
                                                           int n1, int n2) {
                TableCellRenderer tableCellRenderer = jTable.getDefaultRenderer(String.class);
                JLabel jLabel = (JLabel) tableCellRenderer.getTableCellRendererComponent(jTable, object, bl, false, n1, n2);
                Object value = model.getValueAt(jTable, n1, "status");
                if (value != null && !"".equals(value) && !"success".equals(value)) {
                    jLabel.setText("<html><body><font color=\"#8b0000\"><strong>ERROR!</strong></font> " + jLabel.getText() + " <font color=\"#8b0000\">" + object + "</font></body></html>");
                }
                return jLabel;
            }
        };
    }

    public static TableCellRenderer getBoldOnKeyRenderer(final GenericTableModel model, final String key) {
        return new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable jTable, Object object,
                                                           boolean bl, boolean bl2,
                                                           int n1, int n2) {
                TableCellRenderer tableCellRenderer = jTable.getDefaultRenderer(String.class);
                JLabel jLabel = (JLabel) tableCellRenderer
                        .getTableCellRendererComponent(jTable, object, bl, false, n1, n2);
                if (model.getValueAt(jTable, n1, key) == Boolean.TRUE) {
                    jLabel.setFont(jLabel.getFont().deriveFont(1));
                } else {
                    jLabel.setFont(jLabel.getFont().deriveFont(0));
                }
                return jLabel;
            }
        };
    }

    public static TableCellRenderer getSimpleTableRenderer() {
        return new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable jTable, Object object,
                                                           boolean bl, boolean bl2,
                                                           int n1, int n2) {
                TableCellRenderer tableCellRenderer = jTable.getDefaultRenderer(String.class);
                JComponent jComponent = (JComponent) tableCellRenderer
                        .getTableCellRendererComponent(jTable, object, bl, false, n1, n2);
                ((JLabel) jComponent).setIcon(null);
                return jComponent;
            }
        };
    }

    public static TableCellRenderer getSizeTableRenderer() {
        return new TableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable jTable, Object object,
                                                           boolean bl, boolean bl2,
                                                           int n1, int n2) {
                TableCellRenderer tableCellRenderer = jTable.getDefaultRenderer(String.class);
                JComponent jComponent = (JComponent) tableCellRenderer
                        .getTableCellRendererComponent(jTable, "", bl, false, n1, n2);
                try {
                    long l = Long.parseLong(object + "");
                    String str = "b";
                    if (l > 1024L) {
                        l /= 1024L;
                        str = "kb";
                    }
                    if (l > 1024L) {
                        l /= 1024L;
                        str = "mb";
                    }
                    if (l > 1024L) {
                        l /= 1024L;
                        str = "gb";
                    }
                    ((JLabel) jComponent).setText(l + str);
                } catch (Exception exception) {
                }
                return jComponent;
            }
        };
    }

    public static TableCellRenderer getTimeTableRenderer() {
        return new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable jTable, Object object,
                                                           boolean bl, boolean bl2,
                                                           int n1, int n2) {
                TableCellRenderer tableCellRenderer = jTable.getDefaultRenderer(String.class);
                JComponent jComponent = (JComponent) tableCellRenderer
                        .getTableCellRendererComponent(jTable, "", bl, false, n1, n2);
                try {
                    long l = Long.parseLong(object + "");
                    String str = "ms";
                    if (l > 1000L) {
                        l /= 1000L;
                        str = "s";
                    } else {
                        ((JLabel) jComponent).setText(l + str);
                        return jComponent;
                    }
                    if (l > 60L) {
                        l /= 60L;
                        str = "m";
                    }
                    if (l > 60L) {
                        l /= 60L;
                        str = "h";
                    }
                    ((JLabel) jComponent).setText(l + str);
                } catch (Exception exception) {
                }
                return jComponent;
            }
        };
    }

    public static TableCellRenderer getImageTableRenderer(final GenericTableModel model,
                                                          final String icol) {
        return new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable jTable, Object object,
                                                           boolean bl, boolean bl2, int n1, int n2) {
                JLabel jLabel = (JLabel) jTable.getDefaultRenderer(Object.class)
                        .getTableCellRendererComponent(jTable, object, bl, false, n1, n2);
                ImageIcon imageIcon = (ImageIcon) model.getValueAt(jTable, n1, icol);
                if (imageIcon != null) {
                    jLabel.setIcon(imageIcon);
                    jLabel.setText("");
                } else {
                    jLabel.setIcon(null);
                    jLabel.setText("");
                }
                return jLabel;
            }
        };
    }

    public static TableCellRenderer getDateTableRenderer() {
        return new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable jTable, Object object,
                                                           boolean bl, boolean bl2,
                                                           int n1, int n2) {
                TableCellRenderer tableCellRenderer = jTable.getDefaultRenderer(String.class);
                JComponent jComponent = (JComponent) tableCellRenderer.getTableCellRendererComponent(jTable, "", bl, false, n1, n2);
                try {
                    long l = Long.parseLong(object + "");
                    ((JLabel) jComponent).setText(CommonUtils.formatDate(l));
                } catch (Exception exception) {
                }
                return jComponent;
            }
        };
    }

    public void adjust() {
        setOpaque(true);
        addMouseListener(this.clickl);
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 0));
        setRowHeight(getRowHeight() + 2);
        final TableCellEditor defaulte = getDefaultEditor(Object.class);
        setDefaultEditor(Object.class, new TableCellEditor() {
            @Override
            public Component getTableCellEditorComponent(JTable jTable, Object object,
                                                         boolean bl,
                                                         int n1, int n2) {
                Component component = defaulte
                        .getTableCellEditorComponent(jTable, object, bl, n1, n2);
                if (component instanceof JTextComponent)
                    new CutCopyPastePopup((JTextComponent) component);
                return component;
            }

            public void addCellEditorListener(CellEditorListener cellEditorListener) {
                defaulte.addCellEditorListener(cellEditorListener);
            }

            public void cancelCellEditing() {
                defaulte.cancelCellEditing();
            }

            public Object getCellEditorValue() {
                return defaulte.getCellEditorValue();
            }

            public boolean isCellEditable(EventObject eventObject) {
                return defaulte.isCellEditable(eventObject);
            }

            public void removeCellEditorListener(CellEditorListener cellEditorListener) {
                defaulte.removeCellEditorListener(cellEditorListener);
            }

            public boolean shouldSelectCell(EventObject eventObject) {
                return defaulte.shouldSelectCell(eventObject);
            }

            public boolean stopCellEditing() {
                return defaulte.stopCellEditing();
            }
        });
        final TableCellRenderer defaultr = getDefaultRenderer(Object.class);
        setDefaultRenderer(Object.class, new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable jTable, Object object,
                                                           boolean bl, boolean bl2,
                                                           int n1, int n2) {
                if (object == null) {
                    object = "";
                }
                Component component = defaultr
                        .getTableCellRendererComponent(jTable, object, bl, false, n1, n2);
                Object model = ((GenericTableModel) jTable.getModel())
                        .getValueAtColumn(jTable, n1, "_accent");
                if (!bl) {
                    if ("neutral".equals(model)) {
                        component.setForeground(ATable.FORE_NEUTRAL);
                        component.setBackground(ATable.BACK_NEUTRAL);
                    } else if ("bad".equals(model)) {
                        component.setForeground(ATable.FORE_BAD);
                        component.setBackground(ATable.BACK_BAD);
                    } else if ("good".equals(model)) {
                        component.setForeground(ATable.FORE_GOOD);
                        component.setBackground(ATable.BACK_GOOD);
                    } else if ("ignore".equals(model)) {
                        component.setForeground(ATable.FORE_IGNORE);
                        component.setBackground(ATable.BACK_IGNORE);
                    } else if ("cancel".equals(model)) {
                        component.setForeground(ATable.FORE_CANCEL);
                        component.setBackground(ATable.BACK_CANCEL);
                    } else {
                        component.setForeground(Color.BLACK);
                        component.setBackground(alternateBackground ? new Color(0xF7F7F9) : Color.WHITE);
                    }
                } else if ("neutral".equals(model)) {
                    component.setForeground(ATable.BACK_NEUTRAL);
                } else if ("bad".equals(model)) {
                    component.setForeground(ATable.BACK_BAD);
                } else if ("good".equals(model)) {
                    component.setForeground(ATable.BACK_GOOD);
                } else if ("ignore".equals(model)) {
                    component.setForeground(ATable.BACK_IGNORE);
                } else if ("cancel".equals(model)) {
                    component.setForeground(ATable.BACK_CANCEL);
                }
                return component;
            }
        });
    }

    public ATable() {
        adjust();
    }

    public ATable(TableModel tableModel) {
        super(tableModel);
        adjust();
    }

    public Component prepareRenderer(TableCellRenderer tableCellRenderer, int n1, int n2) {
        this.alternateBackground = (n1 % 2 == 0);
        return super.prepareRenderer(tableCellRenderer, n1, n2);
    }

    public Color getComponentBackground() {
        return this.alternateBackground ? new Color(0xF7F7F9) : Color.WHITE;
    }

    public void addActionForKeyStroke(KeyStroke keyStroke, Action action) {
        getActionMap().put(keyStroke.toString(), action);
        getInputMap().put(keyStroke, keyStroke.toString());
    }

    public void addActionForKey(String string, Action action) {
        addActionForKeyStroke(KeyStroke.getKeyStroke(string), action);
    }

    public BufferedImage getScreenshot() {
        BufferedImage bufferedImage = new BufferedImage(getWidth(), getHeight(), 6);
        Graphics graphics = bufferedImage.getGraphics();
        paint(graphics);
        graphics.dispose();
        return bufferedImage;
    }
}
