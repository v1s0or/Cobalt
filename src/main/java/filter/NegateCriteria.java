package filter;

public class NegateCriteria implements Criteria {
    protected Criteria parent;

    public NegateCriteria(Criteria paramCriteria) {
        this.parent = paramCriteria;
    }

    public boolean test(Object object) {
        return !this.parent.test(object);
    }
}
