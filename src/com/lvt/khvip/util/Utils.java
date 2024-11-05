package com.lvt.khvip.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static <T extends Object> String convertDateToString(T date, String format) throws Exception {
        if (date == null) {
            return "";
        }
        String str = "";
        try {
            str = new SimpleDateFormat(format).format(date);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return str;
    }

    public static <T extends Object> String convertDateToString(T date) throws Exception {
        if (date == null) {
            return "";
        }
        String str = "";
        try {
            str = new SimpleDateFormat("dd/MM/yyyy").format(date);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return str;
    }

    public static Date convertStringToDate(String dateStr, String format) throws Exception {
        DateFormat df = new SimpleDateFormat(format);
        Date result = null;
        try {
            result = df.parse(dateStr);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    public static Date convertStringToDate(String dateStr) throws Exception {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date result = null;
        try {
            result = df.parse(dateStr);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    public static String convertByte64ToFile(String byte64, String path) throws Exception {
        byte[] result = Base64.getDecoder().decode(byte64);
        File file = new File(path + "face" + System.currentTimeMillis() + ".jpg");
        FileUtils.writeByteArrayToFile(file, result);

        return file.getPath();
    }

    public static String convertByte64ToFile(String byte64, String path, String fileName) throws Exception {
        byte[] result = Base64.getDecoder().decode(byte64);
        File file = new File(path + "/" + fileName + ".jpg");
        FileUtils.writeByteArrayToFile(file, result);

        return file.getPath();
    }

    public static String getNameFileImage(String peopleId) {
        String uuid = UUID.randomUUID().toString();
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        String name = ConfProperties.getProperty("minio.manual.reg.prefix") + "/"
                + time + "/"
                + peopleId + "_"
                + uuid + ".jpg";
        return name;
    }

    public static String dayOfWeekName(int day) {
        if (day == 6) {
            return "Thứ 7";
        } else if (day == 7) {
            return "Chủ nhật";
        } else {
            return "Thứ " + (day + 1);
        }
    }

    public static Long strTimeToLong(String hhmm) {
        if (hhmm == null)
            return 0l;
        try {
            Calendar cal = Calendar.getInstance();
            cal.set(2020, 01, 01);
            cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hhmm.split(":")[0]));
            cal.set(Calendar.MINUTE, Integer.valueOf(hhmm.split(":")[1]));
            return cal.getTimeInMillis();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return 0l;
    }

    public static Date nowYMD() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date truncDate(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static String objToJson(Object obj) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
