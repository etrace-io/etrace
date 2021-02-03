package io.etrace.stream.biz.app;

import com.google.common.base.Strings;
import io.etrace.common.constant.Constants;

import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class CallStackHelper {
    private static final Pattern INT_PATTERN = Pattern.compile("^\\d+$");
    private static final String PIPELINE_HEAD = "pipeline:";

    public static String getRootAppId(String requestId) {
        if (requestId != null && requestId.contains("^^")) {
            return requestId.substring(0, requestId.indexOf("^^"));
        }
        return requestId;
    }

    public static String transferStatus(String status) {
        if (!Strings.isNullOrEmpty(status)) {
            status = status.equals("0") ? Constants.SUCCESS_STR : status;
        } else {
            status = Constants.UNKNOWN;
        }
        return status;
    }

    private static String extractGZSShardId(String eosid) {
        try {
            if (eosid == null) {
                return Constants.UNKNOWN;
            }

            if (eosid.length() < 19) {
                return Constants.UNKNOWN;
            }
            String binaryValueStr = Long.toBinaryString(Long.parseLong(eosid));
            int size = binaryValueStr.length();
            String binaryShardId = binaryValueStr.substring(size - 23, size - 18);
            return String.valueOf(Integer.valueOf(binaryShardId, 2));
        } catch (Exception e) {
            return Constants.UNKNOWN;
        }
    }

    public static long parseUnsignedLong(String tagValue) {
        if (tagValue == null) {
            return 0;
        }
        if (!INT_PATTERN.matcher(tagValue).find()) {
            return 0;
        }
        try {
            long value = Long.parseLong(tagValue);
            if (value < 0) {
                return 0;
            }
            return value;
        } catch (Exception e) {
            return 0;
        }
    }

    public static String transferIp(String ip) {
        if (ip != null) {
            int index = ip.indexOf(":");
            if (index >= 0) {
                return ip.substring(0, index);
            }
        }
        return ip;
    }

    public static String transferCommand(String command) {
        if (Strings.isNullOrEmpty(command)) {
            return Constants.UNKNOWN;
        }
        if (!command.startsWith(PIPELINE_HEAD)) {
            return command;
        }
        command = command.substring(PIPELINE_HEAD.length());
        String[] commands = command.split(",");
        Set<String> commandSet = new TreeSet<>();
        for (String c : commands) {
            if (!Strings.isNullOrEmpty(c)) {
                commandSet.add(c);
            }
        }
        if (commandSet.size() == 0) {
            return command;
        }
        return PIPELINE_HEAD + String.join(",", commandSet);
    }

    public static int transferDistribute(long duration) {
        int d = 1;
        if (duration >= 65536) {
            d = 65536;
        } else {
            while (d < duration) {
                d <<= 1;
            }
        }
        return d;
    }

    public static int transferLengthDistribute(long length) {
        int d = 0;
        //max length 10MB
        if (length >= 10485760) {
            d = 10485760;
        } else {
            if (length == 0) {
                d = 10240;
            } else if (length < 102400) {
                while (d < length) {
                    d = d + 10240;
                }
            } else if (102400 <= length && length < 1024000) {
                while (d < length) {
                    d = d + 102400;
                }
            } else if (1024000 <= length && length < 10485760) {
                if (1024000 <= length && length < 1048576) {
                    d = d + 2097152;
                } else {
                    while (d < length) {
                        d = d + 1048576;
                    }
                }
            }
        }
        return d;
    }

    public static String getRequestId(String requestId) {
        if (requestId != null && requestId.contains("^^")) {
            return requestId.substring(requestId.indexOf("^^") + 2);
        }
        return requestId;
    }

    /**
     * can apply hostName transformation via your CMDB info.
     */
    public static String transferHostName(String hostName) {
        return hostName;
    }

    public static String transferId(String id) {
        String result = id;
        int clientAppIndex = result.indexOf("|");
        if (clientAppIndex >= 0) {
            result = result.substring(clientAppIndex + 1);
        }
        if (result.length() < 127) {
            return result;
        } else {
            return null;
        }
    }

    public static String transferSOAInterfaceNew(String serviceName) {
        if (Strings.isNullOrEmpty(serviceName)) {
            return "unknown";
        } else {
            int last = serviceName.lastIndexOf(".");
            if (last > 0) {
                int secondLast = serviceName.substring(0, last).lastIndexOf(".");
                if (secondLast >= 0) {
                    return serviceName.substring(secondLast + 1);
                }
            }

            return serviceName;
        }
    }

    public static String transferEventStatus(String status) {
        if (Strings.isNullOrEmpty(status)) {
            return Constants.UNKNOWN;
        } else {
            return Constants.SUCCESS.equals(status) ? Constants.SUCCESS_STR : Constants.FAILURE;
        }
    }
}

