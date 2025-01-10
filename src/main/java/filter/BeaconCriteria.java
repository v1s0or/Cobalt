package filter;

import common.CommonUtils;

public class BeaconCriteria implements Criteria {
    public boolean test(Object object) {
        if (object == null)
            return false;
        String str = object.toString();
        int i = CommonUtils.toNumber(str, 0);
        return "beacon".equals(CommonUtils.session(i));
    }
}
