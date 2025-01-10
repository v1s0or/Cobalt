package phish;

import common.AObject;
import common.CommonUtils;
import encoders.Base64;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import phish.MailServer;
import phish.PhishingUtils;
import phish.SmtpNotify;
import ssl.ArmitageTrustListener;
import ssl.SecureSocket;

public class SmtpClient extends AObject implements ArmitageTrustListener {
    protected Socket socket = null;

    protected InputStream in = null;

    protected OutputStream out = null;

    protected SmtpNotify notify = null;

    public SmtpClient(SmtpNotify smtpNotify) {
        notify = smtpNotify;
    }

    public boolean trust(String string) {
        return true;
    }

    public void update(String string) {
        if (notify != null) {
            notify.update(string);
        }
    }

    public String readLoop(InputStream inputStream) throws IOException {
        String str = CommonUtils.bString(SecureSocket.readbytes(inputStream));
        checkSmtpError(str);
        return str;
    }

    public void checkSmtpError(String string) {
        if (!string.startsWith("2") && !string.startsWith("3")) {
            throw new RuntimeException(string);
        }
    }

    public void writeb(OutputStream outputStream, String string) throws IOException {
        for (int b = 0; b < string.length(); b++) {
            outputStream.write((byte) string.charAt(b));
        }
        outputStream.flush();
    }

    public String send_email(String string1, String string2, String string3, String string4) throws Exception {
        MailServer mailServer = PhishingUtils.parseServerString(string1);
        String readLoop = "";
        String str = string2.split("@")[1];
        if (mailServer.delay > 0) {
            for (int i = CommonUtils.rand(mailServer.delay) + 1; i > 0; i--) {
                update("[Delay " + i + "s]");
                CommonUtils.sleep(1000L);
            }
        }
        update("[Connecting to " + mailServer.lhost + ":" + mailServer.lport + "]");
        if (mailServer.ssl) {
            SecureSocket secureSocket = new SecureSocket(mailServer.lhost, mailServer.lport, this);
            socket = secureSocket.getSocket();
        } else {
            socket = new Socket(mailServer.lhost, mailServer.lport);
        }
        in = socket.getInputStream();
        out = new BufferedOutputStream(socket.getOutputStream(), 65536);
        socket.setSoTimeout(0);
        update("[Connected to " + mailServer.lhost + ":" + mailServer.lport + "]");
        readLoop = readLoop(in);
        writeb(out, "EHLO " + str + "\r\n");
        update("[EHLO " + str + "]");
        readLoop = readLoop(in);
        if (mailServer.starttls) {
            writeb(out, "STARTTLS\r\n");
            update("[STARTTLS]");
            readLoop = readLoop(in);
            SecureSocket secureSocket = new SecureSocket(socket);
            socket = secureSocket.getSocket();
            in = socket.getInputStream();
            out = new BufferedOutputStream(socket.getOutputStream(), 65536);
            socket.setSoTimeout(0);
            writeb(out, "EHLO " + str + "\r\n");
            update("EHLO " + str);
            readLoop = readLoop(in);
        }
        if (mailServer.username != null && mailServer.password != null) {
            if (!mailServer.starttls && CommonUtils.isin("STARTTLS", readLoop)
                    && !CommonUtils.isin("AUTH", readLoop)) {
                writeb(out, "STARTTLS\r\n");
                update("[STARTTLS]");
                readLoop = readLoop(in);
                SecureSocket secureSocket = new SecureSocket(socket);
                socket = secureSocket.getSocket();
                in = socket.getInputStream();
                out = new BufferedOutputStream(socket.getOutputStream(), 65536);
                socket.setSoTimeout(0);
                writeb(out, "EHLO " + str + "\r\n");
                update("EHLO " + str);
                readLoop = readLoop(in);
            }
            writeb(out, "AUTH LOGIN\r\n");
            update("[AUTH LOGIN]");
            readLoop = readLoop(in);
            writeb(out, Base64.encode(mailServer.username) + "\r\n");
            readLoop = readLoop(in);
            writeb(out, Base64.encode(mailServer.password) + "\r\n");
            readLoop = readLoop(in);
            update("[I am authenticated...]");
        }
        writeb(out, "MAIL FROM: <" + string2 + ">\r\n");
        update("[MAIL FROM: <" + string2 + ">]");
        readLoop = readLoop(in);
        writeb(out, "RCPT TO: <" + string3 + ">\r\n");
        update("[RCPT TO: <" + string3 + ">]");
        readLoop = readLoop(in);
        writeb(out, "DATA\r\n");
        update("[DATA]");
        readLoop = readLoop(in);
        writeb(out, string4);
        writeb(out, "\r\n.\r\n");
        update("[Message Transmitted]");
        readLoop = readLoop(in);
        return readLoop;
    }

    public void cleanup() {
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null) {
                socket.close();
            }
            in = null;
            out = null;
            socket = null;
        } catch (IOException iOException) {
        }
    }
}
