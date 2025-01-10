package data;

import aggressor.AggressorClient;

import java.util.Map;

public interface Aggregator {
    void extract(AggressorClient aggressorClient);

    void publish(Map map);
}
