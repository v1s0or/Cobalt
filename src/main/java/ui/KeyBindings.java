package ui;

import common.CommonUtils;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyBindings implements KeyEventDispatcher {

    protected Map<String, KeyHandler> bindings = new HashMap();

    public void bind(String string, KeyHandler paramKeyHandler) {
        synchronized (this) {
            this.bindings.put(string, paramKeyHandler);
        }
    }

    public boolean dispatchKeyEvent(KeyEvent paramKeyEvent) {
        StringBuffer stringBuffer = new StringBuffer();
        if (paramKeyEvent.getModifiers() != 0)
            stringBuffer.append(getKeyModifiers(paramKeyEvent));
        stringBuffer.append(getKeyText(paramKeyEvent));
        synchronized (this) {
            if (this.bindings.containsKey(stringBuffer.toString())) {
                paramKeyEvent.consume();
                if (paramKeyEvent.getID() != 401)
                    return false;
                CommonUtils.runSafe(new ExecuteBinding(stringBuffer.toString(), (KeyHandler) this.bindings.get(stringBuffer.toString())));
                return true;
            }
        }
        return false;
    }

    private static String getKeyModifiers(KeyEvent paramKeyEvent) {
        StringBuffer stringBuffer = new StringBuffer();
        if (paramKeyEvent.isShiftDown())
            stringBuffer.append("Shift+");
        if (paramKeyEvent.isControlDown())
            stringBuffer.append("Ctrl+");
        if (paramKeyEvent.isAltDown())
            stringBuffer.append("Alt+");
        if (paramKeyEvent.isMetaDown())
            stringBuffer.append("Meta+");
        return stringBuffer.toString();
    }

    private static String getKeyText(KeyEvent paramKeyEvent) {
        switch (paramKeyEvent.getKeyCode()) {
            case 30:
                return "Accept";
            case 192:
                return "Back_Quote";
            case 8:
                return "Backspace";
            case 20:
                return "Caps_Lock";
            case 12:
                return "Clear";
            case 28:
                return "Convert";
            case 127:
                return "Delete";
            case 40:
                return "Down";
            case 35:
                return "End";
            case 10:
                return "Enter";
            case 27:
                return "Escape";
            case 112:
                return "F1";
            case 113:
                return "F2";
            case 114:
                return "F3";
            case 115:
                return "F4";
            case 116:
                return "F5";
            case 117:
                return "F6";
            case 118:
                return "F7";
            case 119:
                return "F8";
            case 120:
                return "F9";
            case 121:
                return "F10";
            case 122:
                return "F11";
            case 123:
                return "F12";
            case 24:
                return "Final";
            case 156:
                return "Help";
            case 36:
                return "Home";
            case 155:
                return "Insert";
            case 37:
                return "Left";
            case 144:
                return "Num_Lock";
            case 106:
                return "NumPad_*";
            case 521:
                return "NumPad_+";
            case 44:
                return "NumPad_,";
            case 109:
                return "NumPad_-";
            case 46:
                return "Period";
            case 47:
                return "NumPad_/";
            case 34:
                return "Page_Down";
            case 33:
                return "Page_Up";
            case 19:
                return "Pause";
            case 154:
                return "Print_Screen";
            case 222:
                return "Quote";
            case 39:
                return "Right";
            case 145:
                return "Scroll_Lock";
            case 32:
                return "Space";
            case 9:
                return "Tab";
            case 38:
                return "Up";
        }
        return KeyEvent.getKeyText(paramKeyEvent.getKeyCode());
    }

    private static class ExecuteBinding implements Runnable {
        protected String binding;

        protected KeyHandler handler;

        public ExecuteBinding(String string, KeyHandler param1KeyHandler) {
            this.binding = string;
            this.handler = param1KeyHandler;
        }

        public void run() {
            this.handler.key_pressed(this.binding);
        }
    }
}
