package aggressor;

import common.Callback;

import java.util.List;
import java.util.Map;

public interface GenericDataManager {
    void unsub(String string, Callback callback);

    void subscribe(String string, Callback callback);

    WindowCleanup unsubOnClose(String string, Callback callback);

    Object get(String string, Object object);

    Map getMapSafe(String string);

    List getListSafe(String string);
}
