package cloudstrike;

import java.util.List;
import java.util.Properties;

public interface WebService {
    Response serve(String string1, String string2, Properties properties1, Properties properties2);

    String getType();

    List cleanupJobs();

    boolean suppressEvent(String string);

    boolean isFuzzy();

    void setup(WebServer webServer, String string);
}
