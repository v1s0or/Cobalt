package server;

import aggressor.Aggressor;
import beacon.BeaconSetup;
import common.CommonUtils;
import common.LoggedEvent;
import common.MudgeSanity;
import common.Reply;
import common.Request;
import common.TabScreenshot;
import common.TeamSocket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ManageUser implements Runnable {
    protected TeamSocket client;

    protected boolean authenticated = false;

    protected String nickname = "";

    protected Resources resources;

    protected BroadcastWriter writer = null;

    protected Map calls = null;

    protected Thread mine = null;

    public ManageUser(TeamSocket paramTeamSocket, Resources resources, Map map) {
        this.client = paramTeamSocket;
        this.resources = resources;
        this.calls = map;
    }

    public boolean isConnected() {
        return this.client.isConnected();
    }

    public String getNick() {
        return this.nickname;
    }

    public void write(Reply paramReply) {
        this.writer.addReply(paramReply);
    }

    public void writeNow(Reply paramReply) {
        if (Thread.currentThread() != this.mine) {
            CommonUtils.print_error("writeNow " + paramReply + " should be called in: " + this.mine + " not: " + Thread.currentThread());
            write(paramReply);
        } else {
            this.client.writeObject(paramReply);
        }
    }

    public void process(Request request) throws Exception {
        if (!this.authenticated && "aggressor.authenticate".equals(request.getCall()) && request.size() == 3) {
            String str1 = request.arg(0) + "";
            String str2 = request.arg(1) + "";
            String str3 = request.arg(2) + "";
            if (!Aggressor.VERSION.equals(str3)) {
                this.client.writeObject(request.reply("Your client software does not match this server\nClient: " + str3 + "\nServer: " + Aggressor.VERSION));
            } else if (ServerUtils.getServerPassword(this.resources, str1).equals(str2)) {
                if (this.resources.isRegistered(str1)) {
                    this.client.writeObject(request.reply("User is already connected."));
                } else {
                    this.client.writeObject(request.reply("SUCCESS"));
                    this.authenticated = true;
                    this.nickname = str1;
                    Thread.currentThread().setName("Manage: " + this.nickname);
                    this.writer = new BroadcastWriter();
                    (new Thread(this.writer, "Writer for: " + this.nickname)).start();
                }
            } else {
                this.client.writeObject(request.reply("Logon failure"));
            }
        } else if (!this.authenticated) {
            this.client.close();
        } else if ("aggressor.metadata".equals(request.getCall()) && request.size() == 1) {
            HashMap hashMap = new HashMap();
            hashMap.put("nick", this.nickname);
            ServerUtils.getProfile(this.resources).getPreview().summarize(hashMap);
            long l = System.currentTimeMillis() - Long.parseLong(request.arg(0) + "");
            hashMap.put("clockskew", Long.valueOf(l));
            hashMap.put("signer", ServerUtils.getProfile(this.resources).getCodeSigner());
            hashMap.put("validssl", ServerUtils.getProfile(this.resources).hasValidSSL() ? "true" : "false");
            hashMap.put("amsi_disable", ServerUtils.getProfile(this.resources).option(".post-ex.amsi_disable") ? "true" : "false");
            hashMap.put("postex_obfuscate", ServerUtils.getProfile(this.resources).option(".post-ex.obfuscate") ? "true" : "false");
            hashMap.put("postex_smartinject", ServerUtils.getProfile(this.resources).option(".post-ex.smartinject") ? "true" : "false");
            hashMap.put("c2profile", ServerUtils.getProfile(this.resources));
            hashMap.put("pubkey", BeaconSetup.beacon_asymmetric().exportPublicKey());
            this.client.writeObject(request.reply(hashMap));
        } else if ("aggressor.ready".equals(request.getCall())) {
            this.resources.register(this.nickname, this);
            this.resources.broadcast("eventlog", LoggedEvent.Join(this.nickname));
        } else if ("aggressor.ping".equals(request.getCall()) && request.size() == 1) {
            this.client.writeObject(request.reply(request.arg(0)));
        } else if ("aggressor.users".equals(request.getCall())) {
            this.client.writeObject(request.reply(this.resources.getUsers()));
        } else if ("aggressor.event".equals(request.getCall()) && request.size() == 1) {
            LoggedEvent loggedEvent = (LoggedEvent) request.arg(0);
            loggedEvent.touch();
            if (loggedEvent.type == 1) {
                if (this.resources.isRegistered(loggedEvent.to)) {
                    if (loggedEvent.from.equals(loggedEvent.to)) {
                        this.resources.send(loggedEvent.from, "eventlog", loggedEvent);
                    } else {
                        this.resources.send(loggedEvent.from, "eventlog", loggedEvent);
                        this.resources.send(loggedEvent.to, "eventlog", loggedEvent);
                    }
                } else {
                    this.resources.send(loggedEvent.from, "eventlog", LoggedEvent.NoUser(loggedEvent));
                }
            } else {
                this.resources.broadcast("eventlog", loggedEvent);
            }
        } else if ("armitage.upload".equals(request.getCall()) && request.size() == 1) {
            File file = CommonUtils.SafeFile("uploads", request.arg(0) + "");
            file.mkdirs();
            file.delete();
            this.client.writeObject(request.reply(file.getAbsolutePath()));
        } else if ("aggressor.resource".equals(request.getCall()) && request.size() == 1) {
            String str = (String) request.arg(0);
            if ("winvnc.x86.dll".equals(str)) {
                this.client.writeObject(request.reply(CommonUtils.readFile("third-party/winvnc.x86.dll")));
            } else if ("winvnc.x64.dll".equals(str)) {
                this.client.writeObject(request.reply(CommonUtils.readFile("third-party/winvnc.x64.dll")));
            }
        } else if ("aggressor.sysinfo".equals(request.getCall())) {
            this.client.writeObject(request.reply(MudgeSanity.systemInformation()));
        } else if (request.is("aggressor.screenshot", 1)) {
            TabScreenshot tabScreenshot = (TabScreenshot) request.arg(0);
            tabScreenshot.touch(this.nickname);
            this.resources.process(tabScreenshot);
        } else if ("armitage.append".equals(request.getCall()) && request.size() == 2) {
            File file = CommonUtils.SafeFile("uploads", request.arg(0) + "");
            byte[] arrby = (byte[]) request.arg(1);
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file, true);
                fileOutputStream.write(arrby);
                fileOutputStream.close();
                this.client.writeObject(request.reply(file.getAbsolutePath()));
            } catch (IOException iOException) {
                this.client.writeObject(request.reply("ERROR: " + iOException.getMessage()));
                MudgeSanity.logException(request.getCall() + " " + file, iOException, true);
            }
        } else if ("armitage.broadcast".equals(request.getCall()) && request.size() == 2) {
            String str = (String) request.arg(0);
            Object object = request.arg(1);
            this.resources.broadcast(str, object, true);
        } else if ("aggressor.reset_data".equals(request.getCall()) && request.size() == 0) {
            CommonUtils.print_warn(getNick() + " reset the data model.");
            this.resources.reset();
        } else if (this.calls.containsKey(request.getCall())) {
            ServerHook serverHook = (ServerHook) this.calls.get(request.getCall());
            serverHook.call(request, this);
        } else {
            this.client.writeObject(new Reply("server_error", 0L, request + ": unknown call [or bad arguments]"));
        }
    }

    public void run() {
        try {
            this.mine = Thread.currentThread();
            while (this.client.isConnected()) {
                Request request = (Request) this.client.readObject();
                if (request != null)
                    process(request);
            }
        } catch (Exception exception) {
            MudgeSanity.logException("manage user", exception, false);
            this.client.close();
        }
        if (this.authenticated) {
            this.resources.deregister(this.nickname, this);
            this.resources.broadcast("eventlog", LoggedEvent.Quit(this.nickname));
        }
    }

    private class BroadcastWriter implements Runnable {
        protected LinkedList replies = new LinkedList();

        protected Reply grabReply() {
            synchronized (this) {
                return (Reply) this.replies.pollFirst();
            }
        }

        protected void addReply(Reply param1Reply) {
            synchronized (this) {
                if (this.replies.size() > 100000)
                    this.replies.removeFirst();
                this.replies.add(param1Reply);
            }
        }

        public void run() {
            try {
                while (ManageUser.this.client.isConnected()) {
                    Reply reply = grabReply();
                    if (reply != null) {
                        ManageUser.this.client.writeObject(reply);
                        Thread.yield();
                        continue;
                    }
                    Thread.sleep(25L);
                }
            } catch (Exception exception) {
                MudgeSanity.logException("bwriter", exception, false);
            }
        }
    }
}
