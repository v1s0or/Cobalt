package stagers;

import common.ListenerConfig;
import common.MudgeSanity;
import common.ScListener;

public abstract class GenericStager implements Cloneable {
    protected ScListener listener = null;

    public GenericStager(ScListener scListener) {
    }

    public GenericStager create(ScListener scListener) {
        try {
            GenericStager genericStager = (GenericStager) clone();
            genericStager.listener = scListener;
            return genericStager;
        } catch (CloneNotSupportedException cloneNotSupportedException) {
            MudgeSanity.logException("can't clone", cloneNotSupportedException, false);
            return null;
        }
    }

    public ListenerConfig getConfig() {
        return this.listener.getConfig();
    }

    public ScListener getListener() {
        return this.listener;
    }

    public abstract String arch();

    public abstract String payload();

    public abstract byte[] generate();
}
