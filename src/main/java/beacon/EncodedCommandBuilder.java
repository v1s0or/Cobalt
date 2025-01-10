package beacon;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import common.BeaconEntry;
import common.CommonUtils;
import common.MudgeSanity;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class EncodedCommandBuilder extends CommandBuilder {
    protected AggressorClient client;

    public EncodedCommandBuilder(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public byte[] process(String string1, String string2) {
        String str = getCharset(string1);
        try {
            Charset charset = Charset.forName(str);
            if (charset == null)
                return CommonUtils.toBytes(string2);
            ByteBuffer byteBuffer = charset.encode(string2);
            byte[] arrby = new byte[byteBuffer.remaining()];
            byteBuffer.get(arrby, 0, arrby.length);
            return arrby;
        } catch (Exception exception) {
            MudgeSanity.logException("could not convert text for id " + string1 + " with " + str, exception, false);
            return CommonUtils.toBytes(string2);
        }
    }

    public String getCharset(String string) {
        BeaconEntry beaconEntry = DataUtils.getBeacon(this.client.getData(), string);
        return (beaconEntry != null) ? beaconEntry.getCharset() : null;
    }

    public void addEncodedString(String string1, String string2) {
        addString(process(string1, string2));
    }

    public void addLengthAndEncodedString(String string1, String string2) {
        addLengthAndString(process(string1, string2));
    }
}
