package console;

import aggressor.Prefs;
import common.CommonUtils;
import dialog.DialogUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;

public class Console extends AssociatedPanel implements FocusListener {
    protected JTextPane console;

    protected JTextField input;

    protected JTextPane prompt;

    protected StatusBar status;

    protected PrintStream log = null;

    protected Properties display;

    protected Font consoleFont;

    protected Colors colors;

    protected ClickListener clickl;

    protected String defaultPrompt = "aggressor > ";

    protected LinkedList<JComponent> components = new LinkedList();

    protected ListIterator history = new LinkedList().listIterator(0);

    protected boolean promptLock = false;

    protected Replacements[] colorme = null;

    protected JPanel bottom = null;

    public void addWordClickListener(ActionListener actionListener) {
        this.clickl.addListener(actionListener);
    }

    public void writeToLog(PrintStream paramPrintStream) {
        this.log = paramPrintStream;
    }

    public void setDefaultPrompt(String string) {
        this.defaultPrompt = string;
    }

    public void setPopupMenu(ConsolePopup paramConsolePopup) {
        this.clickl.setPopup(paramConsolePopup);
    }

    public JTextField getInput() {
        return this.input;
    }

    public void updateProperties(Properties properties) {
        this.display = properties;
        updateComponentLooks();
    }

    private void updateComponentLooks() {
        this.colors = new Colors(this.display);
        Color color1 = Prefs.getPreferences()
                .getColor("console.foreground.color", "#c0c0c0");
        Color color2 = Prefs.getPreferences()
                .getColor("console.background.color", "#000000");
        for (JComponent jComponent : this.components) {
            if (jComponent == this.status) {
                jComponent.setFont(this.consoleFont);
                continue;
            }
            jComponent.setForeground(color1);
            if (jComponent == this.console || jComponent == this.prompt) {
                jComponent.setOpaque(false);
            } else {
                jComponent.setBackground(color2);
            }
            jComponent.setFont(this.consoleFont);
            if (jComponent == this.console || jComponent == this.prompt) {
                jComponent.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
            } else {
                jComponent.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            }
            if (jComponent instanceof JTextComponent) {
                JTextComponent jTextComponent = (JTextComponent) jComponent;
                jTextComponent.setCaretColor(color1.brighter());
            }
        }
    }

    public String getPromptText() {
        return this.prompt.getText();
    }

    public void setPrompt(String string) {
        String str = "\ufffd\ufffd";
        if (string.equals(str) || string.equals("null")) {
            this.colors.set(this.prompt, fixText(this.defaultPrompt));
        } else {
            this.defaultPrompt = string;
            this.colors.set(this.prompt, fixText(string));
        }
    }

    public void updatePrompt(final String _prompt) {
        CommonUtils.runSafe(new Runnable() {
            @Override
            public void run() {
                if (!promptLock) {
                    setPrompt(_prompt);
                }
            }
        });
    }

    public void setStyle(String string) {
        String[] arrstring = string.trim().split("\n");
        this.colorme = new Replacements[arrstring.length];
        for (int i = 0; i < arrstring.length; i++) {
            String[] strs1 = arrstring[i].split("\\t+");
            if (strs1.length == 2) {
                strs1[1] = strs1[1].replace("\\c", "\003");
                strs1[1] = strs1[1].replace("\\o", "\017");
                strs1[1] = strs1[1].replace("\\u", "\037");
                this.colorme[i] = new Replacements(strs1[0], strs1[1]);
            } else {
                System.err.println(arrstring[i] + "<-- didn't split right:" + strs1.length);
            }
        }
    }

    protected String fixText(String string) {
        if (this.colorme == null) {
            return string;
        }
        StringBuffer stringBuffer = new StringBuffer();
        String[] arrstring = string.split("(?<=\\n)");
        for (int i = 0; i < arrstring.length; i++) {
            String str = arrstring[i];
            for (int j = 0; j < this.colorme.length; j++) {
                if (this.colorme[j] != null) {
                    str = (this.colorme[j]).original.matcher(str).replaceFirst((this.colorme[j]).replacer);
                }
            }
            stringBuffer.append(str);
        }
        return stringBuffer.toString();
    }

    protected void appendToConsole(String string) {
        string = fixText(string);
        if (string.length() == 0) {
            return;
        }
        if (string.endsWith("\n") || string.endsWith("\r")) {
            if (!this.promptLock) {
                this.colors.append(this.console, string);
                if (this.log != null) {
                    this.log.print(this.colors.strip(string));
                }
            } else {
                this.colors.append(this.console, this.prompt.getText());
            }
            if (!string.startsWith(this.prompt.getText())) {
                this.promptLock = false;
            }
        } else {
            int i = string.lastIndexOf("\n");
            if (i != -1) {
                this.colors.append(this.console, string.substring(0, i + 1));
                updatePrompt(string.substring(i + 1) + " ");
                if (this.log != null) {
                    this.log.print(this.colors.strip(string.substring(0, i + 1)));
                }
            } else {
                updatePrompt(string);
            }
            this.promptLock = true;
        }
        if (this.console.getDocument().getLength() >= 1) {
            this.console.setCaretPosition(this.console.getDocument().getLength() - 1);
        }
    }

    public void append(final String _text) {
        if (_text == null) {
            return;
        }
        CommonUtils.runSafe(new Runnable() {
            @Override
            public void run() {
                appendToConsole(_text);
            }
        });
    }

    public void clear() {
        CommonUtils.runSafe(new Runnable() {
            @Override
            public void run() {
                console.setText("");
            }
        });
    }

    public void noInput() {
        CommonUtils.runSafe(new Runnable() {
            @Override
            public void run() {
                remove(bottom);
                validate();
            }
        });
    }

    public Console() {
        this(new Properties(), false);
    }

    public Console(boolean bl) {
        this(new Properties(), bl);
    }

    public Console(Properties properties, boolean bl) {
        this.display = properties;
        this.consoleFont = Prefs.getPreferences()
                .getFont("console.font.font", "Monospaced BOLD 14");
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(2, 2, 2, 2));
        this.console = new JTextPane();
        this.console.setEditable(false);
        this.console.addFocusListener(this);
        this.console.setCaret(new DefaultCaret() {
            @Override
            public void setSelectionVisible(boolean isVisible) {
                super.setSelectionVisible(true);
            }
        });
        JScrollPane jScrollPane = new JScrollPane(this.console, 22, 30);
        add(jScrollPane, "Center");
        this.prompt = new JTextPane();
        this.prompt.setEditable(false);
        // this.input.setKeymap((this.input = new JTextField()).addKeymap(null, this.input.getKeymap()));
        this.input.setKeymap(JTextField.addKeymap(null, this.input.getKeymap()));
        this.input.addMouseListener(new MouseAdapter() {
            public void checkEvent(MouseEvent mouseEvent) {
                if (mouseEvent.isPopupTrigger())
                    getPopupMenu((JTextComponent) mouseEvent.getSource()).show((JComponent) mouseEvent.getSource(), mouseEvent.getX(), mouseEvent.getY());
            }

            public void mouseClicked(MouseEvent mouseEvent) {
                checkEvent(mouseEvent);
            }

            public void mousePressed(MouseEvent mouseEvent) {
                checkEvent(mouseEvent);
            }

            public void mouseReleased(MouseEvent mouseEvent) {
                checkEvent(mouseEvent);
            }
        });
        this.input.setFocusTraversalKeys(0, new HashSet());
        this.input.setFocusTraversalKeys(1, new HashSet());
        this.input.setFocusTraversalKeys(2, new HashSet());
        this.bottom = new JPanel();
        this.bottom.setLayout(new BorderLayout());
        this.status = new StatusBar(properties);
        if (bl)
            this.bottom.add(this.status, "North");
        this.bottom.add(this.input, "Center");
        this.bottom.add(this.prompt, "West");
        add(this.bottom, "South");
        this.components.add(this.input);
        this.components.add(this.console);
        this.components.add(jScrollPane);
        this.components.add(this.prompt);
        this.components.add(this.bottom);
        this.components.add(this.status);
        this.components.add(this);
        updateComponentLooks();
        addActionForKeySetting("console.clear_screen.shortcut", "ctrl K",
                new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                console.setText("");
            }
        });
        addActionForKeySetting("console.select_all.shortcut", "ctrl A",
                new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                console.requestFocus();
                console.selectAll();
            }
        });
        addActionForKeySetting("console.clear_buffer.shortcut", "ESCAPE",
                new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                input.setText("");
            }
        });
        setupFindShortcutFeature();
        setupPageShortcutFeature();
        setupFontShortcutFeature();
        setupHistoryFeature();
        this.clickl = new ClickListener(this);
        this.console.addMouseListener(this.clickl);
        Color color = Prefs.getPreferences().getColor("console.background.color", "#000000");
        this.console.setBackground(new Color(0, 0, 0, 0));
        this.prompt.setBackground(new Color(0, 0, 0, 0));
        jScrollPane.getViewport().setBackground(color);
        this.console.setOpaque(false);
    }

    public StatusBar getStatusBar() {
        return this.status;
    }

    public JPopupMenu getPopupMenu(final JTextComponent _component) {
        JPopupMenu jPopupMenu = new JPopupMenu();
        JMenuItem jMenuItem1 = new JMenuItem("Cut", 67);
        JMenuItem jMenuItem2 = new JMenuItem("Copy", 111);
        JMenuItem jMenuItem3 = new JMenuItem("Paste", 80);
        JMenuItem jMenuItem4 = new JMenuItem("Clear", 108);
        if (_component.isEditable()) {
            jPopupMenu.add(jMenuItem1);
        }
        jPopupMenu.add(jMenuItem2);
        jPopupMenu.add(jMenuItem3);
        jPopupMenu.add(jMenuItem4);
        jMenuItem1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                _component.cut();
            }
        });
        jMenuItem2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                _component.copy();
            }
        });
        jMenuItem1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                _component.cut();
            }
        });
        jMenuItem3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                input.paste();
            }
        });
        jMenuItem4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                _component.setText("");
            }
        });
        return jPopupMenu;
    }

    private void setupFindShortcutFeature() {
        Properties properties = this.display;
        final Console myConsole = this;
        addActionForKeySetting("console.find.shortcut", "ctrl pressed F",
                new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                Color color = Prefs.getPreferences().getColor("console.highlight.color", "#0000cc");
                final SearchPanel search = new SearchPanel(console, color);
                final JPanel north = new JPanel();
                JButton jButton = new JButton("X ");
                DialogUtils.removeBorderFromButton(jButton);
                jButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        myConsole.remove(north);
                        myConsole.validate();
                        search.clear();
                    }
                });
                north.setLayout(new BorderLayout());
                north.add(search, "Center");
                north.add(jButton, "East");
                myConsole.add(north, "North");
                myConsole.validate();
                search.requestFocusInWindow();
                search.requestFocus();
            }
        });
    }

    private void setupFontShortcutFeature() {
        addActionForKeySetting("console.font_size_plus.shortcut", "ctrl EQUALS",
                new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                changeFontSize(1.0F);
            }
        });
        addActionForKeySetting("console.font_size_minus.shortcut", "ctrl MINUS",
                new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                changeFontSize(-1.0F);
            }
        });
        addActionForKeySetting("console.font_size_reset.shortcut", "ctrl pressed 0",
                new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                consoleFont = Prefs.getPreferences().getFont("console.font.font",
                        "Monospaced BOLD 14");
                updateComponentLooks();
            }
        });
    }

    private void setupPageShortcutFeature() {
        addActionForKeySetting("console.page_up.shortcut", "pressed PAGE_UP", new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                Rectangle rectangle1 = new Rectangle(console.getVisibleRect());
                Rectangle rectangle2 = new Rectangle(
                        0, (int) (rectangle1.getY() - rectangle1.getHeight() / 2.0D), 1, 1);
                if (rectangle2.getY() <= 0.0D)
                    rectangle1.setLocation(0, 0);
                console.scrollRectToVisible(rectangle2);
            }
        });
        addActionForKeySetting("console.page_down.shortcut", "pressed PAGE_DOWN", new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                Rectangle rectangle1 = new Rectangle(console.getVisibleRect());
                Rectangle rectangle2 = new Rectangle(
                        0, (int) (rectangle1.getY() + rectangle1.getHeight()
                        + rectangle1.getHeight() / 2.0D), 1, 1);
                if (rectangle2.getY() >= console.getHeight())
                    rectangle1.setLocation(0, console.getHeight());
                console.scrollRectToVisible(rectangle2);
            }
        });
    }

    private void setupHistoryFeature() {
        this.input.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (!"".equals(actionEvent.getActionCommand()))
                    history.add(actionEvent.getActionCommand());
            }
        });
        addActionForKeySetting("console.history_previous.shortcut", "UP",
                new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (history.hasPrevious()) {
                    input.setText((String) history.previous());
                } else {
                    input.setText("");
                }
            }
        });
        addActionForKeySetting("console.history_next.shortcut", "DOWN",
                new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (history.hasNext()) {
                    input.setText((String) history.next());
                } else {
                    input.setText("");
                }
            }
        });
    }

    private void changeFontSize(float f) {
        this.consoleFont = this.consoleFont.deriveFont(this.consoleFont.getSize2D() + f);
        updateComponentLooks();
    }

    public void addActionForKeyStroke(KeyStroke keyStroke, Action action) {
        this.input.getKeymap().addActionForKeyStroke(keyStroke, action);
    }

    public void addActionForKey(String string, Action action) {
        addActionForKeyStroke(KeyStroke.getKeyStroke(string), action);
    }

    public void addActionForKeySetting(String string1, String string2, Action action) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(
                this.display.getProperty(string1, string2));
        if (keyStroke != null)
            addActionForKeyStroke(keyStroke, action);
    }

    public void focusGained(FocusEvent focusEvent) {
        if (!focusEvent.isTemporary() && focusEvent.getComponent() == this.console && (System.getProperty("os.name") + "").indexOf("Windows") == -1 && (System.getProperty("os.name") + "").indexOf("Mac") == -1)
            this.input.requestFocusInWindow();
    }

    public boolean requestFocusInWindow() {
        return this.input.requestFocusInWindow();
    }

    public void focusLost(FocusEvent focusEvent) {
    }

    private static class Replacements {
        public Pattern original;

        public String replacer;

        public Replacements(String string1, String string2) {
            this.original = Pattern.compile(string1);
            this.replacer = string2;
        }
    }

    public class ClickListener extends MouseAdapter {
        protected LinkedList listeners = new LinkedList();

        protected ConsolePopup popup = null;

        protected Console parent = null;

        public ClickListener(Console console) {
            this.parent = console;
        }

        public void setPopup(ConsolePopup consolePopup) {
            this.popup = consolePopup;
        }

        public void addListener(ActionListener actionListener) {
            this.listeners.add(actionListener);
        }

        public void mousePressed(MouseEvent mouseEvent) {
            checkPopup(mouseEvent);
        }

        public void mouseReleased(MouseEvent mouseEvent) {
            checkPopup(mouseEvent);
        }

        public void checkPopup(MouseEvent mouseEvent) {
            if (mouseEvent.isPopupTrigger())
                if (this.popup != null && console.getSelectedText() == null) {
                    String str = resolveWord(mouseEvent.getPoint());
                    this.popup.showPopup(str, mouseEvent);
                } else {
                    getPopupMenu((JTextComponent) mouseEvent.getSource())
                            .show((JComponent) mouseEvent.getSource(), mouseEvent.getX(),
                                    mouseEvent.getY());
                }
        }

        public void mouseClicked(MouseEvent mouseEvent) {
            if (!mouseEvent.isPopupTrigger()) {
                String str = resolveWord(mouseEvent.getPoint());
                Iterator iterator = this.listeners.iterator();
                ActionEvent actionEvent = new ActionEvent(this.parent, 0, str);
                if (!"".equals(str))
                    while (iterator.hasNext()) {
                        ActionListener actionListener = (ActionListener) iterator.next();
                        actionListener.actionPerformed(new ActionEvent(this.parent, 0, str));
                    }
            } else {
                checkPopup(mouseEvent);
            }
        }

        public String resolveWord(Point param1Point) {
            int i = console.viewToModel(param1Point);
            String str = console.getText().replace("\n", " ").replaceAll("\\s", " ");
            int j = str.lastIndexOf(" ", i);
            int k = str.indexOf(" ", i);
            if (j == -1) {
                j = 0;
            }
            if (k == -1) {
                k = str.length();
            }
            if (k >= j) {
                return str.substring(j, k).trim();
            }
            return null;
        }
    }
}
