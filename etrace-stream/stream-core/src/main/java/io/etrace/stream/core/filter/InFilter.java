package io.etrace.stream.core.filter;

import com.google.common.collect.Sets;
import io.etrace.common.pipeline.AbstractFilter;
import io.etrace.stream.core.model.Event;

import java.util.Map;
import java.util.Set;

public class InFilter extends AbstractFilter {
    private Set<String> eventTypes;
    private String name;

    public InFilter(String name) {
        this.name = name;
    }

    @Override
    public boolean isMatch(Object obj) {
        if (obj instanceof Event) {
            Event event = (Event)obj;
            return eventTypes.contains(event.getEventType());
        }
        return false;
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
    public String name() {
        return this.name;
    }
}
