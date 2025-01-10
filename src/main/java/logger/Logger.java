package logger;

import common.BeaconEntry;
import common.CommonUtils;
import common.Loggable;
import common.MudgeSanity;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import server.Resources;
import server.ServerUtils;

public class Logger extends ProcessBackend {
  protected Resources r;
  
  private static final SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyMMdd");
  
  public Logger(Resources resources) {
    this.r = resources;
    start("logger");
  }
  
  protected File base(String string) {
    Date date = new Date(System.currentTimeMillis());
    File file = new File("logs");
    file = CommonUtils.SafeFile(file, fileDateFormat.format(date));
    if (string != null)
      file = CommonUtils.SafeFile(file, string);
    if (!file.exists())
      file.mkdirs(); 
    return file;
  }
  
  protected File beacon(String string1, String string2) {
    File file2;
    File file1 = base(null);
    BeaconEntry beaconEntry = ServerUtils.getBeacon(this.r, string1);
    if (beaconEntry == null || "".equals(beaconEntry.getInternal())) {
      file2 = CommonUtils.SafeFile(file1, "unknown");
    } else {
      file2 = CommonUtils.SafeFile(file1, beaconEntry.getInternal());
    } 
    if (string2 != null)
      file2 = CommonUtils.SafeFile(file2, string2);
    if (!file2.exists())
      file2.mkdirs(); 
    return file2;
  }
  
  public void process(Object object) {
    Loggable loggable = (Loggable)object;
    String str = loggable.getBeaconId();
    File file = null;
    if (str != null) {
      file = CommonUtils.SafeFile(beacon(str, loggable.getLogFolder()), loggable.getLogFile());
    } else {
      file = CommonUtils.SafeFile(base(loggable.getLogFolder()), loggable.getLogFile());
    } 
    try {
      FileOutputStream fileOutputStream = new FileOutputStream(file, true);
      DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);
      loggable.formatEvent(dataOutputStream);
      dataOutputStream.flush();
      dataOutputStream.close();
    } catch (IOException iOException) {
      MudgeSanity.logException("Writing to: " + file, iOException, false);
    } 
  }
  
  static  {
    fileDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }
}
