/*
 * Copyright 2020 etrace.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.etrace.common.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeHelper {

    public final static long ONE_MINUTE = TimeUnit.MINUTES.toMillis(1);
    public final static long ONE_HOUR = TimeUnit.HOURS.toMillis(1);
    public static final long ONE_DAY = 24 * ONE_HOUR;
    private static final ThreadLocal<DateFormat> fmt_hour = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMddHH");
        }
    };
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static String key = "now()";

    public static long getHour(long time) {
        return time - time % ONE_HOUR;
    }

    public static String formatterHour(long time) {
        return fmt_hour.get().format(new Date(time));
    }

    public static short getMinute(long time) {
        return (short)((time % ONE_HOUR) / ONE_MINUTE);
    }

    public static boolean isInPeriod(long timestamp) {
        long current = System.currentTimeMillis();
        long last12Hour = current - 12 * ONE_HOUR;
        long next5Minute = current + 30 * ONE_MINUTE;
        return timestamp >= last12Hour && timestamp <= next5Minute;
    }

    public static boolean isValidRidInPeriodRange(long timestamp) {
        return isInPeriod(timestamp, 24, 2);
    }

    public static boolean isInPeriod(long timestamp, int lastXHour, int nextXHour) {
        long current = System.currentTimeMillis();
        long last = current - lastXHour * ONE_HOUR;
        long next = current + nextXHour * ONE_HOUR;
        return timestamp >= last && timestamp <= next;
    }

    public static long truncateByMinute(long timestamp) {
        return timestamp - timestamp % ONE_MINUTE;
    }

    public static int getDay(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.UTC).getDayOfMonth();
    }

    public static String formatDate(LocalDateTime date) {
        return formatter.format(date);
    }

    public static LocalDateTime parse(String dateNow) {
        return LocalDateTime.parse(dateNow, formatter);
    }

    public static long getStartTime(int type, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, type);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime().getTime();
    }

    public static long getEndTime(int type, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, type);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime().getTime();
    }

    public static long getTime(int interval, Date date, String type) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if ("s".equalsIgnoreCase(type)) {
            calendar.add(Calendar.SECOND, -interval);
        } else if ("m".equalsIgnoreCase(type)) {
            calendar.add(Calendar.MINUTE, -interval);
        } else if ("h".equalsIgnoreCase(type)) {
            calendar.add(Calendar.HOUR_OF_DAY, -interval);
        } else if ("d".equalsIgnoreCase(type)) {
            calendar.add(Calendar.DAY_OF_MONTH, -interval);
        }
        return calendar.getTime().getTime();
    }


    public static long getDateLong(String timeStr, Date date) throws ParseException {
        if (timeStr.contains(key)) {
            String[] timer = timeStr.split("/");
            String timePoint = timer[0];
            if (timePoint.equalsIgnoreCase(key)) {
                return date.getTime();
            } else {
                String[] points = timePoint.split("-");
                String prefix = points[1];
                String type = prefix.substring(prefix.length() - 1);
                int interval = Integer.parseInt(prefix.substring(0, prefix.length() - 1));
                return getTime(interval, date, type);
            }
        } else {
            if (timeStr.contains("-")) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return dateFormat.parse(timeStr).getTime();
            }
        }
        throw new RuntimeException("timeStr " + timeStr + " is not valid");
    }

    public static String getRatioDate(String time, String prefix) {
        if (prefix.trim().length() == 0) {
            return null;
        }
        if (prefix.contains("-") && prefix.length() >= 3) {
            String type = prefix.substring(prefix.length() - 1);
            int interval = Integer.parseInt(prefix.substring(1, prefix.length() - 1));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
            Date date = null;
            try {
                date = dateFormat.parse(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return getFormatDate(getTime(interval, date, type));
        }

        return null;
    }

    public static String getFormatDate(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        return dateFormat.format(time);
    }

    public static long getTimestamp(String formatDate) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        return dateFormat.parse(formatDate).getTime();
    }
}
