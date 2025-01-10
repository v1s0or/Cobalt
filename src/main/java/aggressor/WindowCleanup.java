package aggressor;

import common.Callback;
import common.CommonUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class WindowCleanup implements ActionListener, WindowListener {

    protected String[] keys;

    protected Callback listener;

    protected GenericDataManager data;

    protected boolean open = true;

    public WindowCleanup(GenericDataManager paramGenericDataManager, String string, Callback paramCallback) {
        this.keys = CommonUtils.toArray(string);
        this.listener = paramCallback;
        this.data = paramGenericDataManager;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        for (byte b = 0; b < this.keys.length; b++)
            this.data.unsub(this.keys[b], this.listener);
        this.open = false;
    }

    public boolean isOpen() {
        return this.open;
    }

    public void windowClosed(WindowEvent paramWindowEvent) {
        for (byte b = 0; b < this.keys.length; b++)
            this.data.unsub(this.keys[b], this.listener);
        this.open = false;
    }

    public void windowActivated(WindowEvent paramWindowEvent) {
    }

    public void windowClosing(WindowEvent paramWindowEvent) {
    }

    public void windowDeactivated(WindowEvent paramWindowEvent) {
    }

    public void windowDeiconified(WindowEvent paramWindowEvent) {
    }

    public void windowIconified(WindowEvent paramWindowEvent) {
    }

    public void windowOpened(WindowEvent paramWindowEvent) {
    }
}
