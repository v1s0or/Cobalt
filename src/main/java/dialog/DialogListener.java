package dialog;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Map;

public interface DialogListener {
    void dialogAction(ActionEvent actionEvent, Map map) throws IOException;
}
