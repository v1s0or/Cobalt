package aggressor;

import common.CommonUtils;
import cortana.Cortana;
import dialog.DialogUtils;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

public class MultiFrame extends JFrame implements KeyEventDispatcher {

    protected JToolBar toolbar;

    protected JPanel content;

    protected CardLayout cards;

    protected LinkedList<ClientInstance> buttons;

    protected AggressorClient active;

    public Collection<Cortana> getOtherScriptEngines(AggressorClient aggressorClient) {
        Collection<Cortana> collection = getScriptEngines();
        collection.remove(aggressorClient.getScriptEngine());
        return collection;
    }

    public Collection<Cortana> getScriptEngines() {
        synchronized (this.buttons) {
            LinkedList<Cortana> linkedList = new LinkedList();
            for (ClientInstance clientInstance : this.buttons) {
                linkedList.add(clientInstance.app.getScriptEngine());
            }
            return linkedList;
        }
    }

    public Map<String, AggressorClient> getClients() {
        synchronized (this.buttons) {
            HashMap<String, AggressorClient> hashMap = new HashMap();
            for (ClientInstance clientInstance : this.buttons) {
                hashMap.put(clientInstance.button.getText(), clientInstance.app);
            }
            return hashMap;
        }
    }

    public void setTitle(AggressorClient aggressorClient, String string) {
        if (this.active == aggressorClient)
            setTitle(string);
    }

    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (this.active != null) {
            return this.active.getBindings().dispatchKeyEvent(keyEvent);
        }
        return false;
    }

    public void closeConnect() {
        synchronized (this.buttons) {
            if (this.buttons.size() == 0)
                System.exit(0);
        }
    }

    public void quit() {
        CommonUtils.Guard();
        synchronized (this.buttons) {
            ClientInstance clientInstance = null;
            this.content.remove(this.active);
            Iterator iterator = this.buttons.iterator();
            while (iterator.hasNext()) {
                clientInstance = (ClientInstance) iterator.next();
                if (clientInstance.app != this.active) continue;
                this.toolbar.remove(clientInstance.button);
                iterator.remove();
                this.toolbar.validate();
                this.toolbar.repaint();
                break;
            }
            if (this.buttons.size() == 0) {
                System.exit(0);
            } else if (this.buttons.size() == 1) {
                getContentPane().remove(this.toolbar);
                validate();
            }
            clientInstance = iterator.hasNext() ? (ClientInstance) iterator.next() : (ClientInstance) this.buttons.getFirst();
            this.set(clientInstance.button);
        }
        System.gc();
    }

    public MultiFrame() {
        super("");
        getContentPane().setLayout(new BorderLayout());
        this.toolbar = new JToolBar();
        this.content = new JPanel();
        this.cards = new CardLayout();
        this.content.setLayout(this.cards);
        getContentPane().add(this.content, "Center");
        this.buttons = new LinkedList();
        setDefaultCloseOperation(3);
        setSize(800, 600);
        setExtendedState(6);
        setIconImage(DialogUtils.getImage("resources/armitage-icon.gif"));
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
    }

    protected void set(JToggleButton jToggleButton) {
        CommonUtils.Guard();
        synchronized (this.buttons) {
            for (ClientInstance clientInstance : this.buttons) {
                if (clientInstance.button.getText().equals(jToggleButton.getText())) {
                    clientInstance.button.setSelected(true);
                    this.active = clientInstance.app;
                    setTitle(this.active.getTitle());
                    continue;
                }
                clientInstance.button.setSelected(false);
            }
            this.cards.show(this.content, jToggleButton.getText());
            this.active.touch();
        }
    }

    public boolean checkCollision(String string) {
        synchronized (this.buttons) {
            for (ClientInstance clientInstance : this.buttons) {
                if (string.equals(clientInstance.button.getText()))
                    return true;
            }
            return false;
        }
    }

    public void addButton(String string, final AggressorClient component) {
        CommonUtils.Guard();
        if (checkCollision(string)) {
            addButton(string + " (2)", component);
            return;
        }
        synchronized (this.buttons) {
            final ClientInstance clientInstance = new ClientInstance();
            clientInstance.button = new JToggleButton(string);
            clientInstance.button.setToolTipText(string);
            clientInstance.app = component;
            clientInstance.button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    MultiFrame.this.set((JToggleButton) actionEvent.getSource());
                }
            });
            clientInstance.button.addMouseListener(new MouseAdapter() {
                public void check(MouseEvent mouseEvent) {
                    if (mouseEvent.isPopupTrigger()) {
                        final JToggleButton source = clientInstance.button;
                        JPopupMenu jPopupMenu = new JPopupMenu();
                        JMenuItem jMenuItem1 = new JMenuItem("Rename");
                        jMenuItem1.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent actionEvent) {
                                String str = JOptionPane.showInputDialog("Rename to?", source.getText());
                                if (str != null) {
                                    MultiFrame.this.content.remove(component);
                                    MultiFrame.this.content.add(component, str);
                                    source.setText(str);
                                    MultiFrame.this.set(source);
                                }
                            }
                        });
                        jPopupMenu.add(jMenuItem1);
                        JMenuItem jMenuItem2 = new JMenuItem("Disconnect");
                        jMenuItem2.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent actionEvent) {
                                clientInstance.app.kill();
                            }
                        });
                        jPopupMenu.add(jMenuItem2);
                        jPopupMenu.show((JComponent) mouseEvent.getSource(), mouseEvent.getX(), mouseEvent.getY());
                        mouseEvent.consume();
                    }
                }

                public void mouseClicked(MouseEvent mouseEvent) {
                    check(mouseEvent);
                }

                public void mousePressed(MouseEvent mouseEvent) {
                    check(mouseEvent);
                }

                public void mouseReleased(MouseEvent mouseEvent) {
                    check(mouseEvent);
                }
            });
            this.toolbar.add(clientInstance.button);
            this.content.add(component, string);
            this.buttons.add(clientInstance);
            set(clientInstance.button);
            if (this.buttons.size() == 1) {
                show();
            } else if (this.buttons.size() == 2) {
                getContentPane().add(this.toolbar, "South");
            }
            validate();
        }
    }

    private static class ClientInstance {
        public AggressorClient app;

        public JToggleButton button;

        public boolean serviced = false;

        private ClientInstance() {
        }
    }
}
