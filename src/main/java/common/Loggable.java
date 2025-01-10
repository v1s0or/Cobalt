package common;

import java.io.DataOutputStream;
import java.io.IOException;

public interface Loggable {
    String getBeaconId();

    void formatEvent(DataOutputStream dataOutputStream) throws IOException;

    String getLogFile();

    String getLogFolder();
}
