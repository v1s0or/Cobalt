package console;

import aggressor.Prefs;
import common.CommonUtils;
import dialog.DialogUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Properties;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;

import ui.CopyPopup;

public class Display extends JPanel {
    protected JTextPane console;

    protected Properties display;

    protected Font consoleFont;

    protected Colors colors;

    protected LinkedList<JComponent> components = new LinkedList();

    private void updateComponentLooks() {
        this.colors = new Colors(this.display);
        Color color1 = Prefs.getPreferences().getColor("console.foreground.color", "#ffffff");
        Color color2 = Prefs.getPreferences().getColor("console.background.color", "#000000");
        for (JComponent jComponent : this.components) {
            if (jComponent == this.console) {
                jComponent.setOpaque(false);
            } else {
                jComponent.setBackground(color2);
            }
            jComponent.setForeground(color1);
            jComponent.setFont(this.consoleFont);
            if (jComponent == this.console) {
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

    public void append(final String text) {
        CommonUtils.runSafe(new Runnable() {
            @Override
            public void run() {
                _append(text);
            }
        });
    }

    public void _append(String string) {
        Rectangle rectangle = this.console.getVisibleRect();
        this.colors.append(this.console, string);
        this.console.scrollRectToVisible(rectangle);
    }

    public void setText(final String _text) {
        CommonUtils.runSafe(new Runnable() {
            @Override
            public void run() {
                console.setText(_text);
            }
        });
    }

    public void setTextDirect(String string) {
        this.console.setText(string);
    }

    public Display() {
        this(new Properties());
    }

    public Display(Properties properties) {
        this.display = properties;
        this.consoleFont = Prefs.getPreferences().getFont("console.font.font", "Monospaced BOLD 14");
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(2, 2, 2, 2));
        this.console = new JTextPane();
        this.console.setEditable(false);
        this.console.setCaret(new DefaultCaret() {
            @Override
            public void setSelectionVisible(boolean isVisible) {
                super.setSelectionVisible(true);
            }
        });
        JScrollPane jScrollPane = new JScrollPane(this.console, 22, 30);
        add(jScrollPane, "Center");
        this.components.add(this.console);
        this.components.add(jScrollPane);
        this.components.add(this);
        updateComponentLooks();
        new CopyPopup(this.console);
        addActionForKeySetting("console.clear_screen.shortcut", "ctrl K", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                console.setText("");
            }
        });
        addActionForKeySetting("console.select_all.shortcut", "ctrl A", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                console.requestFocus();
                console.selectAll();
            }
        });
        setupFindShortcutFeature();
        setupPageShortcutFeature();
        setupFontShortcutFeature();
        this.console.setBackground(new Color(0, 0, 0, 0));
        Color color = Prefs.getPreferences().getColor("console.background.color", "#000000");
        jScrollPane.getViewport().setBackground(color);
        this.console.setOpaque(false);
    }

    private void setupFindShortcutFeature() {
        Properties properties = this.display;
        final Display myConsole = this;
        addActionForKeySetting("console.find.shortcut", "ctrl pressed F",
                new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Color color = Prefs.getPreferences().getColor("console.highlight.color", "#0000cc");
                final SearchPanel search = new SearchPanel(console, color);
                final JPanel north = new JPanel();
                JButton jButton = new JButton("X ");
                DialogUtils.removeBorderFromButton(jButton);
                jButton.addActionListener(new ActionListener() {
                    @Override
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
                consoleFont = Prefs.getPreferences().getFont("console.font.font", "Monospaced BOLD 14");
                updateComponentLooks();
            }
        });
    }

    private void setupPageShortcutFeature() {
        addActionForKeySetting("console.page_up.shortcut", "pressed PAGE_UP",
                new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                Rectangle rectangle1 = new Rectangle(console.getVisibleRect());
                Rectangle rectangle2 = new Rectangle(
                        0, (int) (rectangle1.getY() - rectangle1.getHeight() / 2.0D), 1, 1);
                if (rectangle2.getY() <= 0.0D) {
                    rectangle1.setLocation(0, 0);
                }
                console.scrollRectToVisible(rectangle2);
            }
        });
        addActionForKeySetting("console.page_down.shortcut", "pressed PAGE_DOWN",
                new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                Rectangle rectangle1 = new Rectangle(console.getVisibleRect());
                Rectangle rectangle2 = new Rectangle(
                        0, (int) (rectangle1.getY()
                        + rectangle1.getHeight() + rectangle1.getHeight() / 2.0D), 1, 1);
                if (rectangle2.getY() >= console.getHeight()) {
                    rectangle1.setLocation(0, console.getHeight());
                }
                console.scrollRectToVisible(rectangle2);
            }
        });
    }

    private void changeFontSize(float f) {
        this.consoleFont = this.consoleFont.deriveFont(this.consoleFont.getSize2D() + f);
        updateComponentLooks();
    }

    public void addActionForKeyStroke(KeyStroke keyStroke, Action action) {
        this.console.getKeymap().addActionForKeyStroke(keyStroke, action);
    }

    public void addActionForKey(String string, Action action) {
        addActionForKeyStroke(KeyStroke.getKeyStroke(string), action);
    }

    public void addActionForKeySetting(String string1, String string2, Action action) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(display.getProperty(string1, string2));
        if (keyStroke != null) {
            addActionForKeyStroke(keyStroke, action);
        }
    }

    public void clear() {
        CommonUtils.Guard();
        this.console.setDocument(new DefaultStyledDocument());
    }

    public void swap(StyledDocument styledDocument) {
        CommonUtils.Guard();
        this.console.setDocument(styledDocument);
    }

    public JTextPane getConsole() {
        return this.console;
    }
}
