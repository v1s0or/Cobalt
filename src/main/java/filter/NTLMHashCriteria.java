package filter;

public class NTLMHashCriteria implements Criteria {
    public boolean test(Object object) {
        return (object == null) ? false : ((object.toString().length() == 32));
    }
}
