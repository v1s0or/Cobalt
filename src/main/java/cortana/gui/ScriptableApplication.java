package cortana.gui;

import javax.swing.JMenuBar;

import ui.KeyHandler;

public interface ScriptableApplication {
    void bindKey(String string, KeyHandler paramKeyHandler);

    JMenuBar getJMenuBar();

    boolean isHeadless();
}
