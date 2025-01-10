package filter;

public class LiteralCriteria implements Criteria {
    protected String value;

    public LiteralCriteria(String string) {
        this.value = string;
    }

    public boolean test(Object object) {
        return (object == null) ? ((this.value.length() == 0)) : this.value.equals(object.toString());
    }
}
