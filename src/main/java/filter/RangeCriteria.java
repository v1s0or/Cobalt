package filter;

import common.CommonUtils;
import common.RangeList;

public class RangeCriteria implements Criteria {
    protected RangeList range;

    public RangeCriteria(String string) {
        this.range = new RangeList(string);
    }

    public boolean test(Object object) {
        return (object == null) ? false : this.range.hit(CommonUtils.toNumber(object.toString(), 0));
    }
}
