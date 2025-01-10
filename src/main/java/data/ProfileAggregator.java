package data;

import aggressor.AggressorClient;
import aggressor.DataUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ProfileAggregator implements Aggregator {
    protected List samples = new LinkedList();

    public void extract(AggressorClient aggressorClient) {
        Map map = DataUtils.getC2Info(aggressorClient.getData());
        this.samples.add(map);
    }

    public void publish(Map map) {
        map.put("c2samples", this.samples);
    }
}
