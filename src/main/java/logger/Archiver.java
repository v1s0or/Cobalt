package logger;

import common.ArchiveMap;
import common.CommonUtils;
import common.Informant;

import java.util.LinkedList;

import server.PersistentData;
import server.Resources;

public class Archiver extends ProcessBackend {
    protected Resources resources;

    protected PersistentData store;

    protected LinkedList<ArchiveMap> model = null;

    public Archiver(Resources resources) {
        this.resources = resources;
        load();
    }

    public void load() {
        this.store = new PersistentData("archives", this);
        this.model = (LinkedList) this.store.getValue(new LinkedList());
        for (ArchiveMap archiveMap : this.model) {
            this.resources.broadcast("archives", archiveMap);
        }
        start("archiver");
    }

    public void reset() {
        synchronized (this) {
            this.tasks = new LinkedList();
            this.model = new LinkedList();
            this.store.save(this.model);
        }
    }

    public void process(Object object) {
        Informant informant = (Informant) object;
        if (informant.hasInformation()) {
            ArchiveMap archiveMap = new ArchiveMap(informant.archive());
            this.resources.broadcast("archives", archiveMap);
            synchronized (this) {
                this.model.add(archiveMap);
                while (this.model.size() > CommonUtils.limit("archives")) {
                    this.model.removeFirst();
                }
                this.store.save(this.model);
            }
        }
    }
}
