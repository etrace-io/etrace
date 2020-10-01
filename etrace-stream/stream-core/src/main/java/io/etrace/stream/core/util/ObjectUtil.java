package io.etrace.stream.core.util;

import com.google.common.base.Strings;

public class ObjectUtil {
    public static double toDouble(String val) {
        if (val == null) {
            return 0;
        }
        try {
            return Double.valueOf(val);
        } catch (Exception ignore) {
            //todo zun.li  default is zero??
            return 0;
        }
    }

    public static double toDouble(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Byte) {
            return ((Byte)obj).doubleValue();
        } else if (obj instanceof Short) {
            return ((Short)obj).doubleValue();
        } else if (obj instanceof Integer) {
            return ((Integer)obj).doubleValue();
        } else if (obj instanceof Float) {
            return ((Float)obj).doubleValue();
        } else if (obj instanceof Long) {
            return (long)obj;
        } else if (obj instanceof Double) {
            return (double)obj;
        } else {
            String v = toString(obj);
            if (!Strings.isNullOrEmpty(v)) {
                try {
                    return Double.valueOf(v);
                } catch (Exception ignore) {
                }
            }
        }
        return Double.NaN;
    }

    public static long toLong(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Byte) {
            return ((Byte)obj).longValue();
        } else if (obj instanceof Short) {
            return ((Short)obj).longValue();
        } else if (obj instanceof Integer) {
            return ((Integer)obj).longValue();
        } else if (obj instanceof Float) {
            return ((Float)obj).longValue();
        } else if (obj instanceof Long) {
            return (long)obj;
        } else if (obj instanceof Double) {
            return ((Double)obj).longValue();
        } else {
            String v = toString(obj);
            if (!Strings.isNullOrEmpty(v)) {
                try {
                    return Long.valueOf(v);
                } catch (Exception ignore) {
                }
            }
        }
        return 0;
    }

    public static String toString(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return (String)obj;
        } else {
            return obj.toString();
        }
    }
}
