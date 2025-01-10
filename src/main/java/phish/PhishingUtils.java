package phish;

import common.CommonUtils;
import common.RegexParser;

import java.util.Map;

public class PhishingUtils {

    public static String updateMessage(String string1, Map<String, String> map, String string2, String string3) {
        for (Map.Entry entry : map.entrySet()) {
            string1 = CommonUtils.strrep(string1, "%" + entry.getKey() + "%", entry.getValue() + "");
        }
        string1 = CommonUtils.strrep(string1, "%TOKEN%", string3);
        if (!"".equals(string2) && string2.length() > 0) {
            string2 = CommonUtils.strrep(string2, "%TOKEN%", string3);
            string1 = CommonUtils.strrep(string1, "%URL%", string2);
            String str1 = "$1\"" + string2 + "\"";
            String str2 = "(?is:(href=)[\"'].*?[\"'])";
            string1 = string1.replaceAll(str2, str1);
        }
        return string1;
    }

    public static MailServer parseServerString(String string) {
        MailServer mailServer = new MailServer();
        RegexParser regexParser = new RegexParser(string);
        if (regexParser.matches("(.*?):(.*?)@(.*)")) {
            mailServer.username = regexParser.group(1);
            mailServer.password = regexParser.group(2);
            regexParser.whittle(3);
        }
        if (regexParser.matches("(.*?),(\\d+)")) {
            mailServer.delay = Integer.parseInt(regexParser.group(2));
            regexParser.whittle(1);
        } else {
            mailServer.delay = 0;
        }
        if (regexParser.endsWith("-ssl")) {
            mailServer.ssl = true;
        } else {
            mailServer.ssl = false;
        }
        if (regexParser.endsWith("-starttls")) {
            mailServer.starttls = true;
        } else {
            mailServer.starttls = false;
        }
        if (regexParser.matches("(.*?):(.*)")) {
            mailServer.lhost = regexParser.group(1);
            mailServer.lport = Integer.parseInt(regexParser.group(2));
        } else {
            mailServer.lhost = regexParser.getText();
            mailServer.lport = 25;
        }
        return mailServer;
    }
}
