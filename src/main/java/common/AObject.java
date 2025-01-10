package common;

public class AObject {
    protected void finalize() {
        CommonUtils.print_stat("Finalized: " + getClass().getName());
    }
}
