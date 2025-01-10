package data;

import aggressor.AggressorClient;
import common.Keys;
import data.Aggregator;
import data.ArchiveAggregator;
import data.ModelAggregator;
import data.ProfileAggregator;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DataAggregate {

    protected AggressorClient client;

    protected List<Aggregator> aggregators = new LinkedList();

    protected DataAggregate(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public void register(Aggregator aggregator) {
        this.aggregators.add(aggregator);
    }

    public Map aggregate() {
        Map<String, AggressorClient> map = this.client.getWindow().getClients();
        for (AggressorClient aggressorClient : map.values()) {
            for (Aggregator aggregator : this.aggregators) {
                aggregator.extract(aggressorClient);
            }
        }
        HashMap hashMap = new HashMap();
        for (Aggregator aggregator : this.aggregators) {
            aggregator.publish(hashMap);
        }
        return hashMap;
    }

    public static Map AllModels(AggressorClient aggressorClient) {
        DataAggregate dataAggregate = new DataAggregate(aggressorClient);
        Iterator iterator = Keys.getDataModelIterator();
        while (iterator.hasNext()) {
            dataAggregate.register(new ModelAggregator((String) iterator.next()));
        }
        dataAggregate.register(new ArchiveAggregator());
        dataAggregate.register(new ProfileAggregator());
        return dataAggregate.aggregate();
    }
}
