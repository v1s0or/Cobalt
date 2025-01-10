package common;

import c2profile.Profile;
import cloudstrike.Response;
import cloudstrike.ResponseFilter;

import java.util.List;
import java.util.Map;

public class WebTransforms implements ResponseFilter {

    protected List<String> order;

    protected Map<String, Object> headers;

    protected Profile c2profile;

    public WebTransforms(Profile profile) {
        this.c2profile = profile;
        if (profile.hasString(".http-config.headers")) {
            this.order = CommonUtils.toList(profile.getString(".http-config.headers"));
        } else {
            this.order = null;
        }
        this.headers = profile.getHeadersAsMap(".http-config");
        if (this.headers.size() == 0) {
            this.headers = null;
        }
    }

    public void filterResponse(Response response) {
        try {
            if (this.headers != null) {
                for (Map.Entry entry : this.headers.entrySet()) {
                    if (!response.header.containsKey(entry.getKey())) {
                        response.addHeader(entry.getKey().toString(),
                                entry.getValue().toString());
                    }
                }
            }
            if (this.order != null) {
                response.orderHeaders(this.order);
            }
        } catch (Throwable throwable) {
            MudgeSanity.logException("filterResponse failed", throwable, false);
        }
    }
}
