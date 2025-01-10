package graph;

public class Route {
    private static final long RANGE_MAX = ipToLong("255.255.255.255");

    protected long begin;

    protected long end;

    protected String gateway;

    protected String network;

    protected String mask;

    public static long ipToLong(String string) {
        if (string == null)
            return 0L;
        String[] arrstring = string.split("\\.");
        long l = 0L;
        if (arrstring.length != 4)
            return 0L;
        try {
            l += Integer.parseInt(arrstring[3]);
            l += (Long.parseLong(arrstring[2]) << 8);
            l += (Long.parseLong(arrstring[1]) << 16);
            l += (Long.parseLong(arrstring[0]) << 24);
        } catch (Exception exception) {
            return l;
        }
        return l;
    }

    public Route(String string) {
        String[] arrstring = string.split("/");
        String str1 = "";
        String str2 = "";
        if (arrstring.length == 1) {
            str1 = string;
            String[] strs1 = string.split("\\.");
            if (strs1[0].equals("0")) {
                str2 = "1";
            } else if (strs1[1].equals("0")) {
                str2 = "8";
            } else if (strs1[2].equals("0")) {
                str2 = "16";
            } else if (strs1[3].equals("0")) {
                str2 = "24";
            } else {
                str2 = "32";
            }
        } else {
            str1 = arrstring[0];
            str2 = arrstring[1];
        }
        this.network = str1;
        this.mask = str2;
        this.gateway = "undefined";
        this.begin = ipToLong(str1);
        try {
            this.end = this.begin + (RANGE_MAX >> Integer.parseInt(str2));
        } catch (Exception exception) {
            System.err.println(str2 + " is malformed!");
        }
    }

    public Route(String string1, String string2, String string3) {
        this.begin = ipToLong(string1);
        this.end = this.begin + RANGE_MAX - ipToLong(string2);
        this.gateway = string3;
        this.network = string1;
        this.mask = string2;
    }

    public boolean equals(Object object) {
        if (object instanceof Route) {
            Route route = (Route) object;
            return (route.begin == this.begin && route.end == this.end && route.gateway.equals(this.gateway));
        }
        return false;
    }

    public int hashCode() {
        return (int) (this.begin + this.end + this.gateway.hashCode());
    }

    public String getGateway() {
        return this.gateway;
    }

    public boolean shouldRoute(String string) {
        long l = ipToLong(string);
        return (l >= this.begin && l <= this.end);
    }

    public String toString() {
        return this.network + "/" + this.mask + " via " + this.gateway;
    }
}
