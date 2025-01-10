package pe;

import common.CommonUtils;
import common.MudgeSanity;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PEClone {
  public void start(String string) {
    try {
      _start(string);
    } catch (Exception exception) {
      MudgeSanity.logException("Error cloning headers of " + string, exception, false);
    } 
  }
  
  public void out(String string) { System.out.println(string); }
  
  public void set(String string, long l) { set(string, l + ""); }
  
  public void set(String string, byte[] arrby) {
    if (arrby.length == 0)
      return; 
    set(string, CommonUtils.toAggressorScriptHexString(arrby));
  }
  
  public void set(String string1, String string2) {
    if (string2 != null)
      System.out.println("\tset " + string1 + " \"" + string2 + "\";");
  }
  
  public void _start(String string) throws IOException {
    File file = new File(string);
    PEParser pEParser = PEParser.load(new FileInputStream(file));
    out("# ./peclone " + (new File(string)).getName());
    out("stage {");
    set("checksum      ", pEParser.get("Checksum"));
    set("compile_time  ", CommonUtils.formatDateAny("dd MMM yyyy HH:mm:ss", pEParser.getDate("TimeDateStamp").getTime()));
    set("entry_point   ", pEParser.get("AddressOfEntryPoint"));
    if (pEParser.get("SizeOfImage") > 307200) {
      set("image_size_x86", pEParser.get("SizeOfImage"));
      set("image_size_x64", pEParser.get("SizeOfImage"));
    } 
    set("name          ", pEParser.getString("Export.Name"));
    set("rich_header   ", pEParser.getRichHeader());
    out("}");
  }
}
