package server;

import common.CommonUtils;
import common.MudgeSanity;
import common.Request;
import common.StringStack;

import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import ssl.ArmitageTrustListener;
import ssl.SecureSocket;

public class WebsiteCloneTool implements Runnable, HostnameVerifier, ArmitageTrustListener {
    protected Request request;

    protected ManageUser client;

    public WebsiteCloneTool(Request request, ManageUser manageUser) {
        this.request = request;
        this.client = manageUser;
        (new Thread(this, "Clone: " + request.arg(0))).start();
    }

    public boolean trust(String string) {
        return true;
    }

    public boolean verify(String string, SSLSession paramSSLSession) {
        return true;
    }

    private String cloneAttempt(String string) throws Exception {
        String str2;
        URL uRL = new URL(string);
        HttpURLConnection httpURLConnection = (HttpURLConnection) uRL.openConnection();
        if (httpURLConnection instanceof HttpsURLConnection) {
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) httpURLConnection;
            httpsURLConnection.setHostnameVerifier(this);
            httpsURLConnection.setSSLSocketFactory(SecureSocket.getMyFactory(this));
        }
        httpURLConnection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
        httpURLConnection.setInstanceFollowRedirects(true);
        byte[] arrby = CommonUtils.readAll(httpURLConnection.getInputStream());
        if (httpURLConnection.getResponseCode() == 302 || httpURLConnection.getResponseCode() == 301)
            return cloneAttempt(httpURLConnection.getHeaderField("location"));
        String str1 = CommonUtils.bString(arrby);
        if (!uRL.getFile().endsWith("/")) {
            StringStack stringStack = new StringStack(uRL.getFile(), "/");
            stringStack.pop();
            str2 = CommonUtils.strrep(string, uRL.getFile(), stringStack.toString() + "/");
        } else {
            str2 = string;
        }
        if (str1.toLowerCase().indexOf("shortcut icon") < 0 && str1.toLowerCase().indexOf("rel=\"icon") < 0)
            str1 = str1.replaceFirst("(?i:<head.*?>)", "$0\n<link rel=\"shortcut icon\" type=\"image/x-icon\" href=\"/favicon.ico\">");
        if (str1.toLowerCase().indexOf("<base href=") < 0)
            str1 = str1.replaceFirst("(?i:<head.*?>)", "$0\n<base href=\"" + str2 + "\">");
        return str1;
    }

    public void run() {
        String str = this.request.arg(0) + "";
        try {
            String str1 = cloneAttempt(str);
            this.client.write(this.request.reply(str1));
        } catch (Exception exception) {
            MudgeSanity.logException("clone: " + str, exception, false);
            this.client.write(this.request.reply("error: " + exception.getMessage()));
        }
    }
}
