package filter;

import common.AddressList;

public class NetworkCriteria implements Criteria {
    protected AddressList hosts;

    public NetworkCriteria(String string) {
        this.hosts = new AddressList(string);
    }

    public boolean test(Object object) {
        return (object == null) ? false : this.hosts.hit(object.toString());
    }
}
