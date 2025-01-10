package aggressor.headless;

import aggressor.AggressorClient;
import aggressor.MultiFrame;
import common.CommonUtils;
import common.MudgeSanity;
import common.TeamQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class HeadlessClient extends AggressorClient implements Runnable {
  protected String scriptf;
  
  @Override
  public void disconnected() {
    CommonUtils.print_error("Disconnected from team server.");
    System.exit(0);
  }
  
  public void result(String string, Object object) {
    if ("server_error".equals(string))
      CommonUtils.print_error("Server error: " + object);
  }
  
  public void loadScripts() {
    if (this.scriptf == null) {
      try {
        this.engine.loadScript("scripts/console.cna", CommonUtils.resource("scripts/console.cna"));
      } catch (Exception exception) {
        MudgeSanity.logException("Loading scripts/console.cna", exception, false);
      } 
      (new Thread(this, "Aggressor Script Console")).start();
    } else {
      try {
        this.engine.loadScript(this.scriptf);
      } catch (Exception exception) {
        MudgeSanity.logException("Loading " + this.scriptf, exception, true);
        System.exit(0);
      } 
    } 
  }
  
  public void run() {
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    while (true) {
      try {
        System.out.print("\033[4maggressor\033[0m> ");
        String str = bufferedReader.readLine();
        if (str != null && !"".equals(str))
          this.engine.getConsoleInterface().processCommand(str); 
      } catch (IOException iOException) {}
    } 
  }
  
  public HeadlessClient(MultiFrame paramMultiFrame, TeamQueue teamQueue, Map map, String string) {
    this.scriptf = string;
    setup(paramMultiFrame, teamQueue, map, new HashMap());
  }
  
  public boolean isHeadless() { return true; }
}
