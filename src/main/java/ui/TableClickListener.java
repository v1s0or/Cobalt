package ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TableClickListener extends MouseAdapter {

    protected TablePopup popup = null;

    public void setPopup(TablePopup paramTablePopup) {
        this.popup = paramTablePopup;
    }

    public void mousePressed(MouseEvent mouseEvent) {
        checkPopup(mouseEvent);
    }

    public void mouseReleased(MouseEvent mouseEvent) {
        checkPopup(mouseEvent);
    }

    public void checkPopup(MouseEvent mouseEvent) {
        if (mouseEvent.isPopupTrigger() && this.popup != null)
            this.popup.showPopup(mouseEvent);
    }

    public void mouseClicked(MouseEvent mouseEvent) {
        checkPopup(mouseEvent);
    }
}
