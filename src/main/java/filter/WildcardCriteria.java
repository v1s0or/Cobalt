package filter;

import common.CommonUtils;

public class WildcardCriteria implements Criteria {
    protected String wildcard;

    public WildcardCriteria(String string) {
        this.wildcard = string.toLowerCase();
    }

    public boolean test(Object object) {
        return (object == null) ? false : CommonUtils.iswm(this.wildcard, object.toString().toLowerCase());
    }
}
