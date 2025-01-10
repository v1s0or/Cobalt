package data;

import aggressor.AggressorClient;
import common.ChangeLog;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ModelAggregator implements Aggregator {
    protected ChangeLog changes;

    protected String name;

    protected Map model = new HashMap();

    public ModelAggregator(String string) {
        this.name = string;
        this.changes = new ChangeLog(string);
    }

    public void extract(AggressorClient aggressorClient) {
        Map map = aggressorClient.getData().getDataModel(this.name);
        merge(map);
    }

    public void merge(Map<Object, Object> map) {
        for (Map.Entry entry : map.entrySet()) {
            String str = (String) entry.getKey();
            Map val = (Map) entry.getValue();
            this.changes.update(str, new HashMap(val));
        }
        this.changes.applyForce(this.model);
        this.changes = new ChangeLog(this.name);
    }

    public void publish(Map map) {
        map.put(this.name, new LinkedList(this.model.values()));
    }

}
