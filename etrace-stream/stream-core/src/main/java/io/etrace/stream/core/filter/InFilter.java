package io.etrace.stream.core.filter;

import com.google.common.collect.Sets;
import io.etrace.common.message.trace.MessageHeader;
import io.etrace.common.pipeline.Filter;
import io.etrace.common.pipeline.Filterable;

import java.util.Map;
import java.util.Set;

public class InFilter implements Filter {
    private Set<String> eventTypes;
    private String name;

    public InFilter(String name) {
        this.name = name;
    }

    @Override
    public void init(Map<String, Object> params) {
        try {
            eventTypes = Sets.newHashSet(params.get("key").toString().split(","));
        } catch (Exception ex) {
            String msg =
                "init EventTypeFilter error, events should be provided in params as string list, actual params: ["
                    + params + "]";
            throw new RuntimeException(msg);
        }
    }

    @Override
    public boolean match(Filterable filterable) {
        return eventTypes.contains(filterable.filterKey());
    }

    @Override
    public boolean matchByMessageHeader(MessageHeader messageHeader) {
        return match(messageHeader);
    }

    @Override
    public String name() {
        return this.name;
    }
}
