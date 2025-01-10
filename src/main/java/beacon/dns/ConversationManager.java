package beacon.dns;

import c2profile.Profile;
import common.CommonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConversationManager {

    protected Map<String, Entry> conversations = new HashMap();

    protected int maxtxt;

    protected long idlemask;

    public ConversationManager(Profile profile) {
        this.maxtxt = profile.getInt(".dns_max_txt");
        this.idlemask = CommonUtils.ipToLong(profile.getString(".dns_idle"));
    }

    public RecvConversation getRecvConversation(String string1, String string2) {
        return (RecvConversation) getConversation(string1, string2, RecvConversation.class);
    }

    public SendConversation getSendConversationA(String string1, String string2) {
        return (SendConversation) getConversation(string1, string2, SendConversationA.class);
    }

    public SendConversation getSendConversationAAAA(String string1, String string2) {
        return (SendConversation) getConversation(string1, string2, SendConversationAAAA.class);
    }

    public SendConversation getSendConversationTXT(String string1, String string2) {
        return (SendConversation) getConversation(string1, string2, SendConversationTXT.class);
    }

    public Map getConversations(String string) {
        if (!this.conversations.containsKey(string)) {
            this.conversations.put(string, new Entry());
        }
        Entry entry = (Entry) this.conversations.get(string);
        entry.last = System.currentTimeMillis();
        return entry.convos;
    }

    public Object getConversation(String string1, String string2, Class class_) {
        Map map = getConversations(string1);
        if (!map.containsKey(string2)) {
            if (class_ == RecvConversation.class) {
                RecvConversation recvConversation = new RecvConversation(string1, string2);
                map.put(string2, recvConversation);
                return recvConversation;
            }
            if (class_ == SendConversationA.class) {
                SendConversationA sendConversationA = new SendConversationA(string1,
                        string2, this.idlemask);
                map.put(string2, sendConversationA);
                return sendConversationA;
            }
            if (class_ == SendConversationAAAA.class) {
                SendConversationAAAA sendConversationAAAA = new SendConversationAAAA(string1,
                        string2, this.idlemask);
                map.put(string2, sendConversationAAAA);
                return sendConversationAAAA;
            }
            if (class_ == SendConversationTXT.class) {
                SendConversationTXT sendConversationTXT = new SendConversationTXT(string1,
                        string2, this.idlemask, this.maxtxt);
                map.put(string2, sendConversationTXT);
                return sendConversationTXT;
            }
            return null;
        }
        return map.get(string2);
    }

    public void removeConversation(String string1, String string2) {
        if (!this.conversations.containsKey(string1)) {
            return;
        }
        Map map = getConversations(string1);
        map.remove(string2);
        if (map.size() == 0) {
            this.conversations.remove(string1);
        }
    }

    public void purge(String string) {
        if (!this.conversations.containsKey(string)) {
            return;
        }
        Entry entry = (Entry) this.conversations.get(string);
        if (System.currentTimeMillis() - entry.last > 15000L || entry.hits > 256L) {
            this.conversations.remove(string);
            CommonUtils.print_error("Purged " + entry.convos.size()
                    + " stalled conversation(s) for " + string);
        } else {
            CommonUtils.print_warn("Protected " + entry.convos.size()
                    + " open conversation(s) for " + string + " (strike " + entry.hits
                    + " of 256)");
            entry.hits++;
        }
    }

    private static class Entry {

        public Map convos = new HashMap();

        public long last = System.currentTimeMillis();

        public long hits = 0L;

        private Entry() {
        }
    }
}
