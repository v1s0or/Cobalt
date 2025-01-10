package ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DoubleClickWatch extends MouseAdapter {

    protected DoubleClickListener l;

    public DoubleClickWatch(DoubleClickListener paramDoubleClickListener) {
        this.l = paramDoubleClickListener;
    }

    public void mouseClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() >= 2) {
            this.l.doubleClicked(mouseEvent);
            mouseEvent.consume();
        }
    }
}
