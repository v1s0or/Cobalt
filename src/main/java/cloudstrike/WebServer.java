package cloudstrike;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebServer extends NanoHTTPD {
    protected Map hooks = new HashMap();
    protected Map hooksSecondary = new HashMap();
    protected Map always = new HashMap();
    protected Map hosts = new HashMap();

    protected List weblisteners = new LinkedList();


    public void associate(String uri, String host) {
        this.hosts.put(uri, host);
    }


    public void addWebListener(WebListener l) {
        this.weblisteners.add(l);
    }


    public List sites() {
        List temp = new LinkedList();
        Iterator i = this.hooks.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            String uri = entry.getKey() + "";
            String desc = entry.getValue() + "";
            String type = ((WebService) entry.getValue()).getType();
            String host = (String) this.hosts.get(uri);

            Map result = new HashMap();
            result.put("URI", uri);
            result.put("Description", desc);
            result.put("Type", type);
            result.put("Host", host);


            if (isSSL()) {
                result.put("Proto", "https://");
            } else {
                result.put("Proto", "http://");
            }
            temp.add(result);
        }
        return temp;
    }


    public boolean isRegistered(String uri) {
        return this.hooks.containsKey(uri);
    }


    public void register(String uri, WebService service) {
        if (this.hooks.containsKey(uri)) {
            throw new RuntimeException("URI " + uri + " hosts: " + get(uri).getType());
        }
        this.always.remove(uri);
        this.hooks.put(uri, service);
    }

    public void registerSecondary(String uri, WebService service) {
        this.always.remove(uri);
        this.hooksSecondary.put(uri, service);
    }


    public void setSpecialPostURI(String uri) {
        this.always.put(uri, Boolean.TRUE);
    }


    public boolean alwaysRaw(String uri) {
        if (this.always.containsKey(uri)) {
            return true;
        }

        Iterator i = this.always.keySet().iterator();
        while (i.hasNext()) {
            String hook = i.next() + "";


            if (uri.startsWith(hook)) {
                return true;
            }
        }

        return false;
    }


    public WebService get(String uri) {
        return (WebService) this.hooks.get(uri);
    }


    public boolean deregister(String uri) {
        this.hooks.remove(uri);
        this.always.remove(uri);
        this.hosts.remove(uri);
        if (this.hooks.size() == 0) {
            stop();
            return true;
        }
        return false;
    }

    protected void fireWebListener(String uri, String method, Properties header, Properties param, String desc, boolean primary, String response, long size) {
        Iterator i = this.weblisteners.iterator();
        while (i.hasNext()) {
            ((WebListener) i.next()).receivedClient(uri, method, header, param, desc, getPort(), primary, response, size);
        }
    }

    protected Response processResponse(String uri, String method, Properties header, Properties param, boolean primary, WebService service, Response r) {
        String desc;
        if (service == null) {
            desc = null;
        } else {

            desc = service.getType() + " " + service.toString();
        }

        String resp = r.status;
        long size = r.size;

        if (service == null || !service.suppressEvent(uri))
            fireWebListener(uri, method, header, param, desc, primary, resp, size);
        return r;
    }


    public static long checksum8(String text) {
        if (text.length() < 4) {
            return 0L;
        }
        text = text.replace("/", "");
        long sum = 0L;
        for (int x = 0; x < text.length(); x++) {
            sum += text.charAt(x);
        }

        return sum % 256L;
    }


    public static boolean isStager(String uri) {
        return (checksum8(uri) == 92L);
    }


    public static boolean isStagerX64(String uri) {
        return (checksum8(uri) == 93L && uri.matches("/[A-Za-z0-9]{4}"));
    }


    public static boolean isStagerStrict(String uri) {
        return (isStager(uri) && uri.length() == 5);
    }


    public static boolean isStagerX64Strict(String uri) {
        return (isStagerX64(uri) && uri.length() == 5);
    }


    public Response handleRanges(String method, Properties header, Response original) {
        if (header.containsKey("Range") && "GET".equals(method) && original.size > 0L && original.data != null && "200 OK".equals(original.status)) {
            Pattern p = Pattern.compile("bytes=(\\d+)-(\\d+)");
            Matcher m = p.matcher((String) header.get("Range"));
            if (m.matches()) {
                int start = Integer.parseInt(m.group(1));
                int end = Integer.parseInt(m.group(2)) + 1;


                try {
                    if (start < end && end <= original.size) {
                        byte[] rdata = new byte[end - start];
                        original.data.skip(start);
                        int r = original.data.read(rdata, 0, rdata.length);


                        if (r != rdata.length) {
                            throw new RuntimeException("Read " + r + " bytes instead of expected " + rdata.length);
                        }

                        original.addHeader("Content-Range", "bytes " + start + "-" + m.group(2) + "/" + original.size);
                        original.addHeader("Content-Length", rdata.length + "");


                        original.size = rdata.length;
                        original.data = new ByteArrayInputStream(rdata);
                        original.status = "206 Partial Content";

                        return original;
                    }

                } catch (Exception ioex) {
                    logException("Range handling failed: " + header.get("Range") + "; " + start + ", " + end + "; size = " + original.size, ioex, false);
                }


                Response bust = new Response("416 Range Not Satisfiable", "text/plain", "Range Not Satisfiable");
                bust.addHeader("Content-Range", "bytes */" + original.size);
                return bust;
            }
        }

        return original;
    }


    public Response serve(String uri, String method, Properties header, Properties param) {
        return handleRanges(method, header, _serve(uri, method, header, param));
    }


    public Response _serve(String uri, String method, Properties header, Properties param) {
        String useragent = (header.getProperty("User-Agent") + "").toLowerCase();
        if (useragent.startsWith("lynx") || useragent.startsWith("curl") || useragent.startsWith("wget")) {
            return processResponse(uri, method, header, param, false, null, new Response("404 Not Found", "text/plain", ""));
        }
        if (method.equals("OPTIONS")) {
            Response r = processResponse(uri, method, header, param, false, null, new Response("200 OK", "text/html", ""));
            r.addHeader("Allow", "OPTIONS,GET,HEAD,POST");
            return r;
        }
        if (this.hooks.containsKey(uri)) {
            WebService service = (WebService) this.hooks.get(uri);
            return processResponse(uri, method, header, param, true, service, service.serve(uri, method, header, param));
        }
        if (this.hooksSecondary.containsKey(uri)) {
            WebService service = (WebService) this.hooksSecondary.get(uri);
            return processResponse(uri, method, header, param, false, service, service.serve(uri, method, header, param));
        }
        if (this.hooks.containsKey(uri + "/")) {
            WebService service = (WebService) this.hooks.get(uri + "/");
            return processResponse(uri + "/", method, header, param, true, service, service.serve(uri, method, header, param));
        }
        if (uri.startsWith("http://")) {
            WebService service = (WebService) this.hooks.get("proxy");
            if (service != null) {
                return processResponse(uri, method, header, param, true, service, service.serve(uri, method, header, param));
            }

            return processResponse(uri, method, header, param, false, null, new Response("404 Not Found", "text/plain", ""));
        }


        if (isStagerX64Strict(uri) && this.hooks.containsKey("stager64")) {
            WebService service = (WebService) this.hooks.get("stager64");
            return processResponse(uri + "/", method, header, param, true, service, service.serve(uri, method, header, param));
        }
        if (isStagerStrict(uri) && this.hooks.containsKey("stager")) {
            WebService service = (WebService) this.hooks.get("stager");
            return processResponse(uri + "/", method, header, param, true, service, service.serve(uri, method, header, param));
        }


        Iterator i = this.hooksSecondary.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry e = (Map.Entry) i.next();
            WebService svc = (WebService) e.getValue();
            String hook = e.getKey() + "";


            if (uri.startsWith(hook) && svc.isFuzzy()) {
                return processResponse(uri, method, header, param, false, svc, svc.serve(uri.substring(hook.length()), method, header, param));
            }
        }


        if (isStagerX64(uri) && this.hooks.containsKey("stager64")) {
            WebService service = (WebService) this.hooks.get("stager64");
            return processResponse(uri + "/", method, header, param, true, service, service.serve(uri, method, header, param));
        }
        if (isStagerX64(uri)) {
            print_warn("URI Matches staging (x64) URL, but there is no stager bound...: " + uri);
            return processResponse(uri, method, header, param, false, null, new Response("404 Not Found", "text/plain", ""));
        }
        if (isStager(uri) && this.hooks.containsKey("stager")) {
            WebService service = (WebService) this.hooks.get("stager");
            return processResponse(uri + "/", method, header, param, true, service, service.serve(uri, method, header, param));
        }
        if (isStager(uri)) {
            print_warn("URI Matches staging (x86) URL, but there is no stager bound...: " + uri);
            return processResponse(uri, method, header, param, false, null, new Response("404 Not Found", "text/plain", ""));
        }

        return processResponse(uri, method, header, param, false, null, new Response("404 Not Found", "text/plain", ""));
    }


    public WebServer(int port) throws IOException {
        super(port);
    }


    public WebServer(int port, boolean ssl, InputStream keystore, String password) throws IOException {
        super(port, ssl, keystore, password);
    }


    public static void main(String[] args) {
        try {
            new WebServer(80);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static interface WebListener {
        void receivedClient(String string1, String string2, Properties properties, Properties properties2, String string3, int n, boolean bl, String string4, long pl1);
    }
}
