package common;

import java.util.Map;

public interface AdjustData extends Callback {
    Map format(String string, Object object);
}
