package data;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import common.CommonUtils;
import data.Aggregator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ArchiveAggregator implements Aggregator {

    protected List archives = new LinkedList();

    public void extract(AggressorClient aggressorClient) {
        LinkedList<Map> linkedList = aggressorClient.getData().getTranscriptSafe("archives");
        for (Map map : linkedList) {
            HashMap hashMap = new HashMap(map);
            if (hashMap.containsKey("when")) {
                long l = CommonUtils.toLongNumber(hashMap.get("when") + "", 0L);
                hashMap.put("when", DataUtils.AdjustForSkew(aggressorClient.getData(), l));
            }
            this.archives.add(hashMap);
        }
    }

    public void publish(Map map) {
        map.put("archives", this.archives);
    }
}
