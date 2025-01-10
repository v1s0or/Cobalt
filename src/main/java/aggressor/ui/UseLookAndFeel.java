package aggressor.ui;

import javax.swing.UIManager;

public abstract class UseLookAndFeel {

    public static void set(String string, boolean bl) {
        if (bl) {
            UIManager.put(string, Boolean.TRUE);
        } else {
            UIManager.put(string, Boolean.FALSE);
        }
    }

    public abstract void setup();
}
