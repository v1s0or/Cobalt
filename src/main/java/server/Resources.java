package server;

import common.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import logger.Archiver;
import logger.Logger;

public class Resources {
    protected Map<String, ManageUser> clients = new HashMap();

    protected Map<String, Object> transcripts = new HashMap();

    protected ServerBus bus;

    protected Map shared = new HashMap();

    protected Map<String, Object> replayme = new HashMap();

    protected Logger logger = new Logger(this);

    protected Archiver archiver = null;

    public void reset() {
        synchronized (this) {
            Set set = CommonUtils.toSet("listeners, sites, users, metadata, localip, cmdlets");
            Iterator iterator = Keys.getDataModelIterator();
            while (iterator.hasNext()) {
                String str = (String) iterator.next();
                if (!set.contains(str)) {
                    call(str + ".reset");
                }
            }
            call("beacons.reset");
            if (this.archiver != null) {
                this.archiver.reset();
            }
            this.transcripts = new HashMap();
            broadcast("data_reset", new TranscriptReset(), false);
        }
    }

    public void archive(Informant informant) {
        this.archiver.act(informant);
    }

    public boolean isLimit(Collection collection, String string) {
        return (collection.size() >= CommonUtils.limit(string));
    }

    public Resources(Map map) {
        this.bus = new ServerBus(map);
        this.archiver = new Archiver(this);
    }

    public Object get(String string) {
        synchronized (this.shared) {
            if (!this.shared.containsKey(string)) {
                CommonUtils.print_error("Shared resource: '" + string
                        + "' does not exist [this is probably bad]");
                Thread.dumpStack();
            }
            return this.shared.get(string);
        }
    }

    public void put(String string, Object object) {
        synchronized (this.shared) {
            this.shared.put(string, object);
        }
    }

    public void backlog(String string, Object object) {
        synchronized (this) {
            LinkedList<Object> linkedList = (LinkedList<Object>) this.transcripts.get(string);
            if (linkedList == null) {
                linkedList = new LinkedList();
                this.transcripts.put(string, linkedList);
            }
            while (isLimit(linkedList, string)) {
                linkedList.removeFirst();
            }
            linkedList.add(object);
        }
    }

    public void playback(String string) {
        synchronized (this) {
            PlaybackStatus playbackStatus = new PlaybackStatus("syncing with server",
                    this.transcripts.size() + this.replayme.size());
            send(string, "playback.status", playbackStatus.copy());
            for (Map.Entry entry : this.transcripts.entrySet()) {
                String str = entry.getKey() + "";
                LinkedList linkedList = (LinkedList) entry.getValue();
                playbackStatus.message("syncing " + str);
                send(string, "playback.status", playbackStatus.copy());
                playbackStatus.more(linkedList.size());
                Iterator iterator = linkedList.iterator();
                while (iterator.hasNext()) {
                    send(string, str, iterator.next());
                    playbackStatus.sent();
                    send(string, "playback.status", playbackStatus.copy());
                }
                playbackStatus.sent();
            }
            send(string, "playback.status", playbackStatus.copy());
            for (Map.Entry entry : this.replayme.entrySet()) {
                String str = entry.getKey() + "";
                Object object = entry.getValue();
                playbackStatus.message("syncing " + str);
                send(string, "playback.status", playbackStatus.copy());
                send(string, str, object);
                playbackStatus.sent();
            }
            send(string, "playback.status", playbackStatus.copy());
        }
    }

    public List<ManageUser> getClients() {
        LinkedList<ManageUser> linkedList = new LinkedList();
        synchronized (this) {
            for (ManageUser manageUser : this.clients.values()) {
                linkedList.add(manageUser);
            }
        }
        return linkedList;
    }

    public Set getUsers() {
        synchronized (this) {
            return new HashSet(this.clients.keySet());
        }
    }

    public void send(String string1, String string2, Object object) {
        synchronized (this) {
            ManageUser manageUser = (ManageUser) this.clients.get(string1);
            send(manageUser, string2, object);
        }
    }

    public void send(ManageUser manageUser, String string, Object object) {
        Reply reply = new Reply(string, 0L, object);
        manageUser.write(reply);
    }

    public void sendAndProcess(ManageUser manageUser, String string, Object object) {
        process(object);
        send(manageUser, string, object);
    }

    public void process(Object object) {
        if (object instanceof Loggable) {
            this.logger.act(object);
        }
        if (object instanceof Informant) {
            this.archiver.act(object);
        }
    }

    public void broadcast(String string, Object object) {
        broadcast(string, object, false);
    }

    public void broadcast(String string, Object object, boolean bl) {
        broadcast(string, object, null, bl);
    }

    public void broadcast(String string, Object object, ChangeLog changeLogant, boolean bl) {
        synchronized (this) {
            if (object instanceof Transcript) {
                backlog(string, object);
            } else if (bl) {
                this.replayme.put(string, object);
            }
            process(object);
            Reply reply = new Reply(string, 0L, changeLogant != null ? changeLogant : object);
            for (ManageUser manageUser : getClients()) {
                manageUser.write(reply);
            }
        }
    }

    public boolean isRegistered(String string) {
        synchronized (this) {
            return this.clients.containsKey(string);
        }
    }

    public void register(String string, ManageUser manageUser) {
        synchronized (this) {
            this.clients.put(string, manageUser);
            playback(string);
        }
        broadcast("users", getUsers());
    }

    public void deregister(String string, ManageUser manageUser) {
        synchronized (this) {
            this.clients.remove(string);
        }
        broadcast("users", getUsers());
    }

    public void call(String string, Object[] arrobject) {
        this.bus.addRequest(null, new Request(string, arrobject, 0L));
    }

    public void call(String string) {
        this.bus.addRequest(null, new Request(string, new Object[0], 0L));
    }

    public void call(ManageUser manageUser, Request request) {
        this.bus.addRequest(manageUser, request);
    }
}
