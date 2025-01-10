package dialog;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import aggressor.windows.BeaconConsole;
import aggressor.windows.SecureShellConsole;
import common.AssertUtils;
import common.BeaconEntry;
import common.CommonUtils;
import common.MudgeSanity;
import common.StringStack;
import common.TabScreenshot;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.RescaleOp;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultRowSorter;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import table.FilterAndScroll;
import ui.ATable;
import ui.ATextField;
import ui.GenericTableModel;
import ui.Sorters;

public class DialogUtils {

    public static Map icache = new HashMap();

    public static JFrame dialog(String string, int n1, int n2) {
        JFrame jFrame = new JFrame(string);
        jFrame.setSize(n1, n2);
        jFrame.setLayout(new BorderLayout());
        jFrame.setLocationRelativeTo(null);
        jFrame.setIconImage(getImage("resources/armitage-icon.gif"));
        jFrame.setDefaultCloseOperation(2);
        return jFrame;
    }

    public static void close(final JFrame frame) {
        CommonUtils.runSafe(new Runnable() {
            @Override
            public void run() {
                frame.setVisible(false);
                frame.dispose();
            }
        });
    }

    public static void showError(final String message) {
        CommonUtils.runSafe(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(null, message, null, 0);
            }
        });
    }

    public static void showInfo(final String message) {
        CommonUtils.runSafe(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(null, message, null, 1);
            }
        });
    }

    public static void showInput(final JFrame dialog, final String message, final String text) {
        CommonUtils.runSafe(new Runnable() {
            public void run() {
                JOptionPane.showInputDialog(dialog, message, text);
            }
        });
    }

    public static GenericTableModel setupModel(String string, String[] arrstring, List list) {
        GenericTableModel genericTableModel = new GenericTableModel(arrstring, string, 8);
        for (Object o : list) {
            genericTableModel._addEntry((Map) o);
        }
        return genericTableModel;
    }

    public static Map toMap(String string) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        StringStack stringStack = new StringStack(string, ",");
        while (!stringStack.isEmpty()) {
            String str = stringStack.pop();
            String[] arrstring = str.split(": ");
            if (arrstring.length != 2) {
                throw new RuntimeException("toMap: '" + string + "' failed at: " + str);
            }
            hashMap.put(arrstring[0].trim(), arrstring[1].trim());
        }
        return hashMap;
    }

    public static void addToClipboard(String string) {
        addToClipboardQuiet(string);
        showInfo("Copied text to clipboard");
    }

    public static void addToClipboardQuiet(String string) {
        StringSelection stringSelection = new StringSelection(string);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemSelection();
        if (clipboard != null)
            clipboard.setContents(stringSelection, null);
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        if (clipboard != null)
            clipboard.setContents(stringSelection, null);
    }

    public static void setTableColumnWidths(JTable jTable, Map<String, String> map) {
        for (Map.Entry entry : map.entrySet()) {
            String str = entry.getKey() + "";
            int i = Integer.parseInt(entry.getValue() + "");
            jTable.getColumn(str).setPreferredWidth(i);
        }
    }

    public static ATable setupTable(TableModel tableModel, String[] arrstring, boolean bl) {
        if (bl) {
            return setupTable(tableModel, arrstring, 2);
        }
        return setupTable(tableModel, arrstring, 0);
    }

    public static ATable setupTable(TableModel tableModel, String[] arrstring, int n) {
        ATable aTable = new ATable(tableModel);
        aTable.getSelectionModel().setSelectionMode(n);
        TableRowSorter tableRowSorter = new TableRowSorter(tableModel);
        for (int i = 0; i < arrstring.length; i++) {
            String str = arrstring[i];
            Comparator comparator = Sorters.getProperSorter(str);
            if (comparator != null) {
                tableRowSorter.setComparator(i, comparator);
            }
        }
        aTable.setRowSorter(tableRowSorter);
        return aTable;
    }

    public static void sortby(JTable jTable, int n) {
        try {
            LinkedList<RowSorter.SortKey> linkedList = new LinkedList<RowSorter.SortKey>();
            linkedList.add(new RowSorter.SortKey(n, SortOrder.ASCENDING));
            jTable.getRowSorter().setSortKeys(linkedList);
            ((DefaultRowSorter) jTable.getRowSorter()).sort();
        } catch (Exception exception) {
            MudgeSanity.logException("sortby: " + n, exception, false);
        }
    }

    public static void sortby(JTable jTable, int n1, int n2) {
        try {
            LinkedList linkedList = new LinkedList();
            linkedList.add(new RowSorter.SortKey(n1, SortOrder.ASCENDING));
            linkedList.add(new RowSorter.SortKey(n2, SortOrder.ASCENDING));
            jTable.getRowSorter().setSortKeys(linkedList);
            ((DefaultRowSorter) jTable.getRowSorter()).sort();
        } catch (Exception exception) {
            MudgeSanity.logException("sortby: " + n1 + ", " + n2, exception, false);
        }
    }

    public static void startedWebService(String string1, String string2) {
        final JFrame dialog = dialog("Success", 240, 120);
        dialog.setLayout(new BorderLayout());
        JLabel jLabel = new JLabel("<html>Started service: " + string1 + "<br />Copy and paste this URL to access it</html>");
        ATextField aTextField = new ATextField(string2, 20);
        JButton jButton = new JButton("Ok");
        jButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        });
        dialog.add(wrapComponent(jLabel, 5), "North");
        dialog.add(wrapComponent(aTextField, 5), "Center");
        dialog.add(center(jButton), "South");
        dialog.pack();
        dialog.show();
        dialog.setVisible(true);
    }

    public static void presentURL(String string) {
        final JFrame dialog = dialog("Open URL", 240, 120);
        dialog.setLayout(new BorderLayout());
        JLabel jLabel = new JLabel("I couldn't open your browser. Try browsing to:");
        ATextField aTextField = new ATextField(string, 20);
        JButton jButton = new JButton("Ok");
        jButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        });
        dialog.add(wrapComponent(jLabel, 5), "North");
        dialog.add(wrapComponent(aTextField, 5), "Center");
        dialog.add(center(jButton), "South");
        dialog.pack();
        dialog.show();
        dialog.setVisible(true);
    }

    public static void presentText(String string1, String string2, String string3) {
        final JFrame dialog = dialog(string1, 240, 120);
        dialog.setLayout(new BorderLayout());
        JLabel jLabel = new JLabel("<html>" + string2 + "</html>");
        ATextField aTextField = new ATextField(string3, 20);
        JButton jButton = new JButton("Ok");
        jButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        });
        dialog.add(wrapComponent(jLabel, 5), "North");
        dialog.add(wrapComponent(aTextField, 5), "Center");
        dialog.add(center(jButton), "South");
        dialog.pack();
        dialog.show();
        dialog.setVisible(true);
    }

    public static JComponent wrapComponent(JComponent jComponent, int n) {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());
        jPanel.add(jComponent, "Center");
        jPanel.setBorder(BorderFactory.createEmptyBorder(n, n, n, n));
        return jPanel;
    }

    public static JComponent pad(JComponent jComponent, int n1, int n2, int n3, int n4) {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());
        jPanel.add(jComponent, "Center");
        jPanel.setBorder(BorderFactory.createEmptyBorder(n1, n2, n3, n4));
        return jPanel;
    }

    private static LinkedList asList(Object object) {
        LinkedList linkedList = new LinkedList();
        linkedList.add(object);
        return linkedList;
    }

    private static LinkedList asList(Object object1, Object object2) {
        LinkedList linkedList = new LinkedList();
        linkedList.add(object1);
        linkedList.add(object2);
        return linkedList;
    }

    private static LinkedList asList(Object object1, Object object2, Object object3) {
        LinkedList linkedList = new LinkedList();
        linkedList.add(object1);
        linkedList.add(object2);
        linkedList.add(object3);
        return linkedList;
    }

    private static LinkedList asList(Object object1, Object object2, Object object3, Object object4) {
        LinkedList linkedList = new LinkedList();
        linkedList.add(object1);
        linkedList.add(object2);
        linkedList.add(object3);
        linkedList.add(object4);
        return linkedList;
    }

    private static LinkedList asList(Object object1, Object object2, Object object3, Object object4, Object object5) {
        LinkedList linkedList = new LinkedList();
        linkedList.add(object1);
        linkedList.add(object2);
        linkedList.add(object3);
        linkedList.add(object4);
        linkedList.add(object5);
        return linkedList;
    }

    private static LinkedList asList(Object object1, Object object2, Object object3, Object object4, Object object5, Object object6) {
        LinkedList linkedList = new LinkedList();
        linkedList.add(object1);
        linkedList.add(object2);
        linkedList.add(object3);
        linkedList.add(object4);
        linkedList.add(object5);
        linkedList.add(object6);
        return linkedList;
    }

    private static LinkedList asList(Object object1, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        LinkedList linkedList = new LinkedList();
        linkedList.add(object1);
        linkedList.add(object2);
        linkedList.add(object3);
        linkedList.add(object4);
        linkedList.add(object5);
        linkedList.add(object6);
        linkedList.add(object7);
        return linkedList;
    }

    private static LinkedList asList(Object object1, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        LinkedList linkedList = new LinkedList();
        linkedList.add(object1);
        linkedList.add(object2);
        linkedList.add(object3);
        linkedList.add(object4);
        linkedList.add(object5);
        linkedList.add(object6);
        linkedList.add(object7);
        linkedList.add(object8);
        return linkedList;
    }

    private static LinkedList asList(Object object1, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        LinkedList linkedList = new LinkedList();
        linkedList.add(object1);
        linkedList.add(object2);
        linkedList.add(object3);
        linkedList.add(object4);
        linkedList.add(object5);
        linkedList.add(object6);
        linkedList.add(object7);
        linkedList.add(object8);
        linkedList.add(object9);
        return linkedList;
    }

    public static JComponent stack(JComponent jComponent) {
        return stack(asList(jComponent));
    }

    public static JComponent stack(JComponent jComponent1, JComponent jComponent2) {
        return stack(asList(jComponent1, jComponent2));
    }

    public static JComponent stack(JComponent jComponent1, JComponent jComponent2, JComponent jComponent3) {
        return stack(asList(jComponent1, jComponent2, jComponent3));
    }

    public static JComponent stack(List<JComponent> list) {
        Box box = Box.createVerticalBox();
        for (JComponent jComponent : list) {
            jComponent.setAlignmentX(0.0F);
            box.add(jComponent);
        }
        return box;
    }

    public static JComponent stackTwo(JComponent jComponent1, JComponent jComponent2) {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());
        jPanel.add(jComponent1, "Center");
        jPanel.add(jComponent2, "South");
        return jPanel;
    }

    public static JComponent stackThree(JComponent jComponent1, JComponent jComponent2, JComponent jComponent3) {
        return stackTwo(jComponent1, stackTwo(jComponent2, jComponent3));
    }

    public static JComponent center(JComponent jComponent) {
        return center(asList(jComponent));
    }

    public static JComponent center(JComponent jComponent1, JComponent jComponent2) {
        return center(asList(jComponent1, jComponent2));
    }

    public static JComponent center(JComponent jComponent1, JComponent jComponent2, JComponent jComponent3) {
        return center(asList(jComponent1, jComponent2, jComponent3));
    }

    public static JComponent center(JComponent jComponent1, JComponent jComponent2, JComponent jComponent3, JComponent jComponent4) {
        return center(asList(jComponent1, jComponent2, jComponent3, jComponent4));
    }

    public static JComponent center(JComponent jComponent1, JComponent jComponent2, JComponent jComponent3, JComponent jComponent4, JComponent jComponent5) {
        return center(asList(jComponent1, jComponent2, jComponent3, jComponent4, jComponent5));
    }

    public static JComponent center(JComponent jComponent1, JComponent jComponent2, JComponent jComponent3, JComponent jComponent4, JComponent jComponent5, JComponent jComponent6) {
        return center(asList(jComponent1, jComponent2, jComponent3, jComponent4, jComponent5, jComponent6));
    }

    public static JComponent center(JComponent jComponent1, JComponent jComponent2, JComponent jComponent3, JComponent jComponent4, JComponent jComponent5, JComponent jComponent6, JComponent jComponent7) {
        return center(asList(jComponent1, jComponent2, jComponent3, jComponent4, jComponent5, jComponent6, jComponent7));
    }

    public static JComponent center(JComponent jComponent1, JComponent jComponent2, JComponent jComponent3, JComponent jComponent4, JComponent jComponent5, JComponent jComponent6, JComponent jComponent7, JComponent jComponent8) {
        return center(asList(jComponent1, jComponent2, jComponent3, jComponent4, jComponent5, jComponent6, jComponent7, jComponent8));
    }

    public static JComponent center(JComponent jComponent1, JComponent jComponent2, JComponent jComponent3, JComponent jComponent4, JComponent jComponent5, JComponent jComponent6, JComponent jComponent7, JComponent jComponent8, JComponent jComponent9) {
        return center(asList(jComponent1, jComponent2, jComponent3, jComponent4, jComponent5, jComponent6, jComponent7, jComponent8, jComponent9));
    }

    public static JComponent center(List<JComponent> list) {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new FlowLayout(1));
        for (JComponent jComponent : list) {
            jPanel.add(jComponent);
        }
        return jPanel;
    }

    public static JComponent description(String string) {
        JEditorPane jEditorPane = new JEditorPane();
        jEditorPane.setContentType("text/html");
        jEditorPane.setText(string.trim());
        jEditorPane.setEditable(false);
        jEditorPane.setOpaque(true);
        jEditorPane.setCaretPosition(0);
        jEditorPane.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        JScrollPane jScrollPane = new JScrollPane(jEditorPane);
        jScrollPane.setPreferredSize(new Dimension(0, 48));
        jScrollPane.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        return jScrollPane;
    }

    public static ActionListener gotoURL(final String url) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (Desktop.isDesktopSupported()) {
                    (new Thread(new Runnable() {
                        public void run() {
                            try {
                                Desktop.getDesktop().browse((new URL(url)).toURI());
                            } catch (Exception exception) {
                                MudgeSanity.logException("goto: " + url + " *grumble* *grumble*", exception, true);
                                DialogUtils.presentURL(url);
                            }
                        }
                    }, "show URL")).start();
                } else {
                    CommonUtils.print_error("No desktop support to show: " + url);
                }
            }
        };
    }

    public static boolean isShift(ActionEvent actionEvent) {
        return (actionEvent.getModifiers() & 1) == 1;
    }

    public static void setupTimeRenderer(JTable jTable, String string) {
        jTable.getColumn(string).setCellRenderer(ATable.getTimeTableRenderer());
    }

    public static void setupDateRenderer(JTable jTable, String string) {
        jTable.getColumn(string).setCellRenderer(ATable.getDateTableRenderer());
    }

    public static void setupSizeRenderer(JTable jTable, String string) {
        jTable.getColumn(string).setCellRenderer(ATable.getSizeTableRenderer());
    }

    public static void setupImageRenderer(JTable jTable, GenericTableModel genericTableModel, String string1, String string2) {
        jTable.getColumn(string1).setCellRenderer(ATable.getImageTableRenderer(genericTableModel, string2));
    }

    public static void setupBoldOnKeyRenderer(JTable jTable, GenericTableModel genericTableModel, String string1, String string2) {
        jTable.getColumn(string1).setCellRenderer(ATable.getBoldOnKeyRenderer(genericTableModel, string2));
    }

    public static void setupListenerStatusRenderer(JTable jTable, GenericTableModel genericTableModel, String string) {
        jTable.getColumn(string).setCellRenderer(ATable.getListenerStatusRenderer(genericTableModel));
    }

    public static boolean bool(Map map, String string) {
        String str = map.get(string) + "";
        return str.equals("true");
    }

    public static String string(Map map, String string) {
        Object object = map.get(string);
        return (object == null) ? "" : object.toString();
    }

    public static boolean isNumber(Map map, String string) {
        try {
            number(map, string);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    public static int number(Map map, String string) {
        return Integer.parseInt(string(map, string));
    }

    public static void workAroundEditorBug(JEditorPane paramJEditorPane) {
        paramJEditorPane.getDocument().putProperty("IgnoreCharsetDirective", Boolean.TRUE);
    }

    public static JComponent top(JComponent jComponent) {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());
        jPanel.add(jComponent, "North");
        jPanel.add(Box.createVerticalGlue(), "Center");
        return jPanel;
    }

    public static String encodeColor(Color color) {
        String string = Integer.toHexString(color.getRGB() & 0xFFFFFF);
        while (string.length() < 6) {
            string = "0" + string;
        }
        return "#" + string;
    }

    public static JButton Button(String string, ActionListener actionListener) {
        JButton jButton = new JButton(string);
        jButton.addActionListener(actionListener);
        return jButton;
    }

    public static Icon getIcon(String string) {
        try {
            return new ImageIcon(ImageIO.read(CommonUtils.resource(string)));
        } catch (IOException iOException) {
            MudgeSanity.logException("getIcon: " + string, iOException, false);
            return null;
        }
    }

    public static Image getImage(String string) {
        try {
            return ImageIO.read(CommonUtils.resource(string));
        } catch (IOException iOException) {
            MudgeSanity.logException("getImage: " + string, iOException, false);
            return null;
        }
    }

    public static Image getImage(String[] arrstring, boolean bl) {
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration graphicsConfiguration = graphicsEnvironment.getDefaultScreenDevice().getDefaultConfiguration();
        BufferedImage bufferedImage = graphicsConfiguration.createCompatibleImage(1000, 776, 2);
        Graphics2D graphics2D = bufferedImage.createGraphics();
        for (int i = 0; i < arrstring.length; i++) {
            try {
                BufferedImage bufferedImage1 = ImageIO.read(CommonUtils.resource(arrstring[i]));
                graphics2D.drawImage(bufferedImage1, 0, 0, 1000, 776, null);
            } catch (Exception exception) {
                MudgeSanity.logException("getImage: " + arrstring[i], exception, false);
            }
        }
        if (bl) {
            Graphics2D graphics2D1 = graphics2D;
            graphics2D1.setColor(Color.BLACK);
            AlphaComposite alphaComposite = AlphaComposite.getInstance(3, 0.4F);
            graphics2D1.setComposite(alphaComposite);
            graphics2D1.fillRect(0, 0, 1000, 776);
        }
        graphics2D.dispose();
        return bufferedImage;
    }

    public static byte[] toImage(RenderedImage renderedImage, String string) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(524288);
            ImageIO.write(renderedImage, string, byteArrayOutputStream);
            byteArrayOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception exception) {
            MudgeSanity.logException("toImage: " + string, exception, false);
            return new byte[0];
        }
    }

    public static Image getImageSmall(String[] arrstring, boolean bl) {
        BufferedImage bufferedImage = new BufferedImage(1000, 776, 2);
        Graphics2D graphics2D = bufferedImage.createGraphics();
        for (int i = 0; i < arrstring.length; i++) {
            try {
                BufferedImage bufferedImage1 = ImageIO.read(CommonUtils.resource(arrstring[i]));
                graphics2D.drawImage(bufferedImage1, 0, 0, 1000, 776, null);
            } catch (Exception exception) {
                MudgeSanity.logException("getImageSmall: " + arrstring[i], exception, false);
            }
        }
        if (bl) {
            float[] arrf = {1.0F, 1.0F, 1.0F, 0.5F};
            float[] arrf2 = new float[4];
            RescaleOp rescaleOp = new RescaleOp(arrf, arrf2, null);
            bufferedImage = rescaleOp.filter(bufferedImage, null);
        }
        graphics2D.dispose();
        return bufferedImage;
    }

    public static BufferedImage resize(Image image, int n1, int n2) {
        BufferedImage bufferedImage = new BufferedImage(n1, n2, 2);
        Graphics2D graphics2D = bufferedImage.createGraphics();
        Image image2 = image.getScaledInstance(n1, n2, 4);
        graphics2D.drawImage(image2, 0, 0, n1, n2, null);
        graphics2D.dispose();
        return bufferedImage;
    }

    public static void addToTable(final ATable table, final GenericTableModel model, final Map row) {
        CommonUtils.runSafe(new Runnable() {
            public void run() {
                table.markSelections();
                model.addEntry(row);
                model.fireListeners();
                table.restoreSelections();
            }
        });
    }

    public static void setText(final ATextField text, final String data) {
        CommonUtils.runSafe(new Runnable() {
            @Override
            public void run() {
                text.setText(data);
            }
        });
    }

    public static void setTable(final ATable table, final GenericTableModel model, Collection collection) {
        if (!AssertUtils.TestNotNull(table, "table")) {
            return;
        }
        final LinkedList saferows = new LinkedList(collection);
        CommonUtils.runSafe(new Runnable() {
            @Override
            public void run() {
                table.markSelections();
                model.clear(saferows.size());
                for (Object saferow : saferows) {
                    model.addEntry((Map) saferow);
                }
                model.fireListeners();
                table.restoreSelections();
            }
        });
    }

    public static String[] TargetVisualizationArray(String string, double d, boolean bl) {
        String[] arrstring = new String[2];
        arrstring[0] = "resources/unknown.png";
        if (bl) {
            arrstring[1] = "resources/hacked.png";
        } else {
            arrstring[1] = "resources/computer.png";
        }
        if (string.equals("windows")) {
            if (d <= 5.0D) {
                arrstring[0] = "resources/windows2000.png";
            } else if (d > 5.0D && d < 6.0D) {
                arrstring[0] = "resources/windowsxp.png";
            } else if (d == 6.0D || d == 6.1D) {
                arrstring[0] = "resources/windows7.png";
            } else if (d >= 6.2D) {
                arrstring[0] = "resources/windows8.png";
            }
        } else {
            if (string.equals("firewall")) {
                return CommonUtils.toArray("resources/firewall.png");
            }
            if (string.equals("printer")) {
                return CommonUtils.toArray("resources/printer.png");
            }
            if (string.equals("android")) {
                arrstring[0] = "resources/android.png";
            } else if (string.equals("vmware")) {
                arrstring[0] = "resources/vmware.png";
            } else if (string.equals("solaris")) {
                arrstring[0] = "resources/solaris.png";
            } else if (string.equals("freebsd") || string.equals("openbsd") || string.equals("netbsd")) {
                arrstring[0] = "resources/bsd.png";
            } else if (string.equals("linux")) {
                arrstring[0] = "resources/linux.png";
            } else if (string.equals("cisco ios")) {
                arrstring[0] = "resources/cisco.png";
            } else if (string.equals("macos x")) {
                arrstring[0] = "resources/macosx.png";
            } else if (string.equals("apple ios")) {
                arrstring[0] = "resources/ios.png";
            }
        }
        return arrstring;
    }

    public static ImageIcon TargetVisualizationSmall(String string, double d, boolean bl1, boolean bl2) {
        String str = "small:" + string.toLowerCase() + "." + d + "." + bl1 + "." + bl2;
        synchronized (icache) {
            if (icache.containsKey(str)) {
                return (ImageIcon) icache.get(str);
            }
            Image image = getImageSmall(TargetVisualizationArray(string.toLowerCase(), d, bl1), bl2);
            ImageIcon imageIcon = new ImageIcon(image.getScaledInstance((int) Math.floor(image.getWidth(null) / 44.0D), (int) Math.floor(image.getHeight(null) / 44.0D), 4));
            icache.put(str, imageIcon);
            return imageIcon;
        }
    }

    public static Image TargetVisualizationMedium(String string, double d, boolean bl1, boolean bl2) {
        String str = "medium:" + string.toLowerCase() + "." + d + "." + bl1 + "." + bl2;
        synchronized (icache) {
            if (icache.containsKey(str)) {
                return (Image) icache.get(str);
            }
            Image image = getImageSmall(TargetVisualizationArray(string.toLowerCase(), d, bl1), bl2);
            BufferedImage bufferedImage = resize(image, 125, 97);
            icache.put(str, bufferedImage);
            return bufferedImage;
        }
    }

    public static Image TargetVisualization(String string, double d, boolean bl1, boolean bl2) {
        String str = string.toLowerCase() + "." + d + "." + bl1 + "." + bl2;
        synchronized (icache) {
            if (icache.containsKey(str)) {
                return (Image) icache.get(str);
            }
            Image image = getImage(TargetVisualizationArray(string.toLowerCase(), d, bl1), bl2);
            icache.put(str, image);
            return image;
        }
    }

    public static void openOrActivate(final AggressorClient client, final String bid) {
        CommonUtils.runSafe(new Runnable() {
            public void run() {
                BeaconEntry beaconEntry = DataUtils.getBeacon(client.getData(), bid);
                if (!client.getTabManager().activateConsole(bid)) {
                    if (beaconEntry.isBeacon()) {
                        BeaconConsole beaconConsole = new BeaconConsole(bid, client);
                        client.getTabManager().addTab(beaconEntry.title(), beaconConsole.getConsole(), beaconConsole.cleanup(), "Beacon console");
                    } else if (beaconEntry.isSSH()) {
                        SecureShellConsole secureShellConsole = new SecureShellConsole(bid, client);
                        client.getTabManager().addTab(beaconEntry.title(), secureShellConsole.getConsole(), secureShellConsole.cleanup(), "SSH console");
                    }
                }
            }
        });
    }

    public static void setupScreenshotShortcut(final AggressorClient client, final ATable table, final String title) {
        table.addActionForKey("ctrl pressed P", new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                BufferedImage bufferedImage = table.getScreenshot();
                byte[] arrby = DialogUtils.toImage(bufferedImage, "png");
                client.getConnection().call("aggressor.screenshot", CommonUtils.args(new TabScreenshot(title, arrby)));
                DialogUtils.showInfo("Pushed screenshot to team server");
            }
        });
    }

    public static byte[] screenshot(Component component) {
        BufferedImage bufferedImage = new BufferedImage(component.getWidth(), component.getHeight(), 6);
        Graphics graphics = bufferedImage.getGraphics();
        component.paint(graphics);
        graphics.dispose();
        return toImage(bufferedImage, "png");
    }

    public static void removeBorderFromButton(JButton paramJButton) {
        paramJButton.setOpaque(false);
        paramJButton.setContentAreaFilled(false);
        paramJButton.setBorder(new EmptyBorder(2, 2, 2, 2));
    }

    public static JPanel FilterAndScroll(ATable aTable) {
        return new FilterAndScroll(aTable);
    }

    public static void showSessionPopup(AggressorClient aggressorClient, MouseEvent mouseEvent, Object[] arrobject) {
        if (arrobject.length == 0) {
            return;
        }
        String str1 = arrobject[0].toString();
        String str2 = "";
        if ("beacon".equals(CommonUtils.session(str1))) {
            str2 = "beacon";
        } else {
            str2 = "ssh";
        }
        Stack stack = new Stack();
        stack.push(CommonUtils.toSleepArray(arrobject));
        aggressorClient.getScriptEngine().getMenuBuilder().installMenu(mouseEvent, str2, stack);
    }
}
