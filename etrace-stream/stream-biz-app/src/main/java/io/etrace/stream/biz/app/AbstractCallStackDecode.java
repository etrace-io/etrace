package io.etrace.stream.biz.app;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.etrace.common.message.trace.codec.JSONCodecV1;
import io.etrace.stream.biz.app.event.AbstractTransaction;
import io.etrace.stream.biz.app.event.EventType;
import io.etrace.stream.biz.app.event.SQL;
import io.etrace.stream.core.model.Event;
import io.etrace.stream.core.model.Header;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static io.etrace.stream.biz.app.EventConstant.DEFAULT_RMQ_CONSUMER_QUEUE;
import static io.etrace.stream.biz.app.EventConstant.DEFAULT_SOA_SERVICE_METHOD;

public abstract class AbstractCallStackDecode implements CallStackDecode {
    JsonFactory jsonFactory;

    AbstractCallStackDecode() {
        jsonFactory = new JsonFactory();
        /*
         * for disable the intern lock in com.fasterxml.jackson.util.InternCache.intern()
         *
         * <a href="https://github.com/FasterXML/jackson-core/issues/33">issue link</a>
         */
        jsonFactory.disable(JsonFactory.Feature.INTERN_FIELD_NAMES);
    }

    public abstract Object parseEvent(String type, String name, long timestamp, String status, Map<String, String> tags,
                                      String data);

    public abstract Object parseTransaction(String type, String name, long timestamp, String status,
                                            Map<String, String> tags, long duration, String soaServiceMethod,
                                            String rmqConsumerQueue);

    public abstract Object parseHeartbeat(String type, String name, long timestamp, String status,
                                          Map<String, String> tags, String data);

    /**
     * Do some logic after parse message
     *
     * @param event  current event
     * @param parent parent of current event
     * @param root   root of current event
     * @param appId  event没有appId信息
     */
    public abstract void postParseMessage(Event event, Event parent, Event root, String appId);

    /**
     * generate new event after parse message
     *
     * @param event  current event
     * @param parent parent of current event
     * @param root   root of current event
     * @param appId  event没有appId信息
     * @return Object new event or event collection if needed
     */
    public abstract Object parseNewEvent(Event event, Event parent, Event root, String appId);

    @Override
    public List<Event> decode(byte[] data) throws IOException {
        JsonParser parser = null;
        try {
            ByteArrayInputStream input = new ByteArrayInputStream(data);
            parser = jsonFactory.createParser(input);

            JsonToken token = parser.nextToken();
            if (token == JsonToken.VALUE_NULL) {
                return null;
            }
            if (token != JsonToken.START_ARRAY) {
                throw new IllegalArgumentException("Bad json data");
            }
            token = parser.nextToken();//move to first field
            if (token == JsonToken.END_ARRAY) {
                return null;
            }
            List<Event> events = newArrayList();
            List<Event> newEvents = newArrayList();
            AtomicLong dependenciesCost = new AtomicLong();
            Header header = new Header();
            if (parser.getText().equals(JSONCodecV1.CALLSTACK_PREFIX_V1)) {
                byte index = 0;
                while (token != JsonToken.END_ARRAY && token != null) {
                    if (token != JsonToken.VALUE_NULL) {
                        switch (index) {
                            case 0:
                                break;
                            case 1:
                                String appId = parser.getText();
                                header.setAppId(appId);
                                break;
                            case 2:
                                header.setHostIp(parser.getText());
                                break;
                            case 3:
                                header.setHostName(CallStackHelper.transferHostName(parser.getText()));
                                break;
                            case 4:
                                header.setRequestId(parser.getText());
                                break;
                            case 5:
                                header.setId(CallStackHelper.transferId(parser.getText()));
                                break;
                            case 6:
                                if (token != JsonToken.START_ARRAY) {
                                    throw new IllegalArgumentException("Bad json data with bad message data");
                                }
                                decodeMessageJson(parser, events, newEvents, dependenciesCost, header.getAppId(),
                                    DEFAULT_SOA_SERVICE_METHOD, DEFAULT_RMQ_CONSUMER_QUEUE);
                                break;
                            case 7:
                                Map<String, String> extraProperties = JSONCodecV1.decodeExtraProperties(parser);

                                header.setCluster(extraProperties.get("cluster"));
                                header.setEzone(extraProperties.get("ezone"));
                                header.setIdc(extraProperties.get("idc"));
                                header.setInstance(extraProperties.get("instance"));
                                break;
                            default:
                                throw new IllegalArgumentException("Bad json data of open-source version: invalid "
                                    + "index over 7");
                        }
                    }
                    token = parser.nextToken();//move to next value
                    index++;
                }
            } else {
                byte index = 0;
                while (token != JsonToken.END_ARRAY && token != null) {
                    if (token != JsonToken.VALUE_NULL) {
                        switch (index) {
                            case 0:
                                String appId = parser.getText();
                                header.setAppId(appId);
                                break;
                            case 1:
                                header.setHostIp(parser.getText());
                                break;
                            case 2:
                                header.setHostName(CallStackHelper.transferHostName(parser.getText()));
                                break;
                            case 3:
                                header.setRequestId(parser.getText());
                                break;
                            case 4:
                                header.setId(CallStackHelper.transferId(parser.getText()));
                                break;
                            case 5:
                                if (token != JsonToken.START_ARRAY) {
                                    throw new IllegalArgumentException("Bad json data with bad message data");
                                }
                                decodeMessageJson(parser, events, newEvents, dependenciesCost, header.getAppId(),
                                    DEFAULT_SOA_SERVICE_METHOD, DEFAULT_RMQ_CONSUMER_QUEUE);
                                break;
                            case 6:
                                header.setCluster(parser.getText());
                                break;
                            case 7:
                                header.setEzone(parser.getText());
                                break;
                            case 8:
                                header.setIdc(parser.getText());
                                break;
                            case 9:
                                break;
                            case 10:
                                break;
                            case 11:
                                break;
                            case 12:
                                header.setInstance(parser.getText());
                                break;
                            default:
                                throw new IllegalArgumentException("Bad json data of open-source version: invalid "
                                    + "index over 12. current token: " + parser.getText());
                        }
                    }
                    token = parser.nextToken();//move to next value
                    index++;
                }
            }

            header.setEntry(CallStackHelper.getRootAppId(header.getRequestId()));
            header.setRpcLevel(RPCIdHelper.getLevel(header.getId()));
            String requestId = CallStackHelper.getRequestId(header.getRequestId());
            header.setRequestId(requestId);

            if (header.getId() != null) {
                header.setMsg(requestId + "$$" + header.getId());
            }
            events.addAll(newEvents);

            List<Event> newPostEvents = new ArrayList<>();
            for (Event event : events) {
                event.setHeader(header);
                List<Event> out = event.postDecode();
                if (out != null && out.size() > 0) {
                    newPostEvents.addAll(out);
                }
            }
            for (Event event : newPostEvents) {
                event.setHeader(header);
            }
            events.addAll(newPostEvents);
            return events;
        } finally {
            if (parser != null) {
                parser.close();
            }
        }
    }

    /**
     * Parse CallStack message to special business event
     *
     * @param parser    json parse
     * @param events    special  business event result
     * @param newEvents 根据原始日志生成的newEvent 直接add to events会导致event关系混乱
     * @param appId     appId
     */
    private void decodeMessageJson(JsonParser parser, List<Event> events, List<Event> newEvents,
                                   AtomicLong dependenciesCost, String appId, String soaServiceMethod,
                                   String rmqConsumerQueue) throws IOException {
        JsonToken token = parser.nextToken();//move first filed

        long timestamp = 0;
        byte index = 0;
        String type = null;
        String name = null;
        String status = null;
        EventType eventType = null;
        Map<String, String> tags = null;
        long duration = 0;
        String data = null;
        while (token != null && token != JsonToken.END_ARRAY) {
            if (token != JsonToken.VALUE_NULL) {
                switch (index) {
                    case 0:
                        eventType = EventType.valueOf(parser.getText());
                        break;
                    case 1:
                        type = parser.getText();
                        break;
                    case 2:
                        name = parser.getText();
                        break;
                    case 3:
                        status = parser.getText();
                        break;
                    case 4:
                        //skip message id
                        break;
                    case 5:
                        timestamp = parser.getLongValue();
                        break;
                    case 6:
                        // skip isComplete
                        break;
                    case 7:
                        // Tag的信息，如果没有则为null
                        if (token == JsonToken.START_OBJECT) {
                            token = parser.nextToken();//move first tag key
                        }
                        if (tags == null) {
                            tags = newHashMap();
                        }
                        while (token != null && token != JsonToken.END_OBJECT) {
                            String key = parser.getCurrentName();
                            token = parser.nextToken();//move to value
                            String value = parser.getText();
                            if (key != null && value != null) {
                                tags.put(key, value);
                            }
                            token = parser.nextToken();//move to next key
                        }
                        break;
                    case 8:
                        if (eventType == EventType.transaction) {
                            duration = parser.getLongValue();
                        } else if (eventType == EventType.heartbeat) {
                            data = parser.getText();
                        }
                        break;
                    case 9://only for transaction children
                        token = parser.nextToken();
                        while (token != null && token != JsonToken.END_ARRAY) {
                            if (eventType == EventType.transaction) {
                                decodeMessageJson(parser, events, newEvents, dependenciesCost, appId, soaServiceMethod,
                                    rmqConsumerQueue);
                            }
                            token = parser.nextToken();//move to next message
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Bad json data of open-source version: invalid "
                            + "index over 9");
                }
            }
            // ugly. check based on index==8
            if (index == 8) {
                // parse event
                Object obj = null;
                if (eventType == EventType.transaction) {
                    obj = parseTransaction(type, name, timestamp, status, tags, duration, soaServiceMethod,
                        rmqConsumerQueue);
                } else if (eventType == EventType.event) {
                    obj = parseEvent(type, name, timestamp, status, tags, data);
                } else if (eventType == EventType.heartbeat) {
                    obj = parseHeartbeat(type, name, timestamp, status, tags, data);
                }

                int size = events.size();
                Event parent = null;
                Event root = null;
                /**
                 * todo parent可能不准确
                 * 例如一个transaction包含一组children
                 * 只有第一个child parent是root 其他都是前序的兄弟
                 */
                if (size > 0) {
                    root = events.get(0);
                    parent = events.get(size - 1);
                }

                if (obj instanceof Event) {
                    Event event = (Event)obj;
                    events.add(event);
                    if (isDependency(event)) {
                        dependenciesCost.addAndGet(((AbstractTransaction)event).getDuration());
                    }
                    postParse(newEvents, event, parent, root, appId);
                } else if (obj instanceof Collection) {
                    Collection collection = (Collection)obj;
                    for (Object o : collection) {
                        if (o instanceof Event) {
                            Event event = (Event)o;
                            events.add(event);
                            if (isDependency(event)) {
                                dependenciesCost.addAndGet(((AbstractTransaction)event).getDuration());
                            }
                            postParse(newEvents, event, parent, root, appId);
                        }
                    }
                }
            }

            token = parser.nextToken();//move to next value
            index++;
        }
    }

    private void postParse(List<Event> newEvents, Event event, Event parent, Event root, String appId) {
        postParseMessage(event, parent, root, appId);
        Object newEvent = parseNewEvent(event, parent, root, appId);
        addToEvent(newEvents, newEvent);
    }

    private boolean isDependency(Event event) {
        return event instanceof SQL;
    }

    private void addToEvent(List<Event> newEvents, Object newEvent) {
        if (newEvent == null) {
            return;
        }

        if (newEvent instanceof Event) {
            newEvents.add((Event)newEvent);
        } else if (newEvent instanceof Collection) {
            Collection collection = (Collection)newEvent;
            for (Object o : collection) {
                if (o instanceof Event) {
                    newEvents.add((Event)o);
                }
            }
        }
    }

}
