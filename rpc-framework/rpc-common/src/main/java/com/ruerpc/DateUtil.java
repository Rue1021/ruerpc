package com.ruerpc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Rue
 * @date 2025/6/6 10:58
 */
public class DateUtil {

    public static Date get(String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return sdf.parse(pattern);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
