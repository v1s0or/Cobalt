package common;

import java.io.Serializable;

public class Reply implements Serializable {

    protected String call;

    protected Object reply;

    protected long callback_ref;

    public Reply(String string, long l, Object object) {
        this.call = string;
        this.reply = object;
        this.callback_ref = l;
    }

    public String getCall() {
        return this.call;
    }

    public Object getCallbackReference() {
        return new Long(this.callback_ref);
    }

    public Object getContent() {
        return this.reply;
    }

    public boolean hasCallback() {
        return (this.callback_ref != 0L);
    }

    public String toString() {
        return "Reply '" + getCall() + "': " + getContent();
    }
}
