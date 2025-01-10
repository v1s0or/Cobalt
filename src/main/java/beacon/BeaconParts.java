package beacon;

import common.CommonUtils;
import common.Packer;

import java.util.HashMap;
import java.util.Map;

public class BeaconParts {
    protected Map parts = new HashMap();

    public void start(String string, int n) {
        Part part = new Part();
        part.length = n;
        if (n <= 0)
            return;
        synchronized (this) {
            this.parts.put(string, part);
        }
    }

    public boolean isReady(String string) {
        Part part = null;
        synchronized (this) {
            part = (Part) this.parts.get(string);
        }
        return (part != null && part.buffer.size() >= part.length);
    }

    public boolean hasPart(String string) {
        Part part = null;
        synchronized (this) {
            part = (Part) this.parts.get(string);
        }
        return (part != null);
    }

    public void put(String string, byte[] arrby) {
        Part part = null;
        synchronized (this) {
            part = (Part) this.parts.get(string);
        }
        if (part == null) {
            CommonUtils.print_error("CALLBACK_CHUNK_SEND " + string + ": no pending transmission");
            return;
        }
        part.buffer.addString(arrby, arrby.length);
    }

    public byte[] data(String string) {
        Part part = null;
        synchronized (this) {
            part = (Part) this.parts.get(string);
            this.parts.remove(string);
        }
        return (part == null) ? new byte[0] : part.buffer.getBytes();
    }

    public static class Part {
        public int length;

        public Packer buffer = new Packer();
    }
}
