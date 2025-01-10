package common;

import java.io.Serializable;

public class Request implements Serializable {

    protected String call;

    protected Object[] args;

    protected long callback_ref;

    public Request(String string, Object[] arrobject, long l) {
        this.call = string;
        this.args = arrobject;
        this.callback_ref = l;
    }

    public Reply reply(Object object) {
        return new Reply(this.call, this.callback_ref, object);
    }

    public Request derive(String string, Object[] arrobject) {
        return new Request(string, arrobject, this.callback_ref);
    }

    public Request derive(String string) {
        return new Request(string, this.args, this.callback_ref);
    }

    public String getCall() {
        return this.call;
    }

    public boolean is(String string) {
        return this.call.equals(string);
    }

    public boolean is(String string, int n) {
        return (getCall().equals(string) && size() == n);
    }

    public Object[] getArgs() {
        return this.args;
    }

    public Object arg(int n) {
        return this.args[n];
    }

    public String argz(int n) {
        return (String) arg(n);
    }

    public int size() {
        return (this.args == null) ? 0 : this.args.length;
    }

    public String toString() {
        return "Request '" + getCall() + "' with " + size() + " args";
    }
}
