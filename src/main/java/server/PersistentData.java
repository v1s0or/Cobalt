package server;

import common.CommonUtils;
import common.MudgeSanity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class PersistentData implements Runnable {

    protected String model;

    protected Object value = null;

    protected Object lock = null;

    public PersistentData(String string, Object object) {
        this.model = string;
        this.lock = object;
        new Thread(this, "save thread for: " + string).start();
    }

    public void save(Object object) {
        synchronized (this.lock) {
            this.value = object;
        }
    }

    private void _save() {
        try {
            new File("data").mkdirs();
            File file = CommonUtils.SafeFile("data", this.model + ".bin");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                    new FileOutputStream(file, false));
            objectOutputStream.writeObject(this.value);
            objectOutputStream.close();
        } catch (Exception exception) {
            MudgeSanity.logException("save " + this.model, exception, false);
        }
    }

    public void run() {
        while (true) {
            synchronized (this.lock) {
                if (this.value != null) {
                    _save();
                    this.value = null;
                }
            }
            CommonUtils.sleep(10000L);
        }
    }

    public Object getValue(Object object) {
        try {
            File file = CommonUtils.SafeFile("data", this.model + ".bin");
            if (file.exists()) {
                ObjectInputStream objectInputStream = new ObjectInputStream(
                        new FileInputStream(file));
                Object read = objectInputStream.readObject();
                objectInputStream.close();
                return read;
            }
        } catch (Exception exception) {
            MudgeSanity.logException("load " + this.model, exception, false);
            CommonUtils.print_error("the " + this.model + " model will start empty [everything is OK]");
        }
        return object;
    }
}
