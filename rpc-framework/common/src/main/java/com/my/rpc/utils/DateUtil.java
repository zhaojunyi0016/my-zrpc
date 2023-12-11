package com.my.rpc.utils;

import lombok.extern.slf4j.Slf4j;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author : Williams
 * Date : 2023/12/9 11:28
 */
@Slf4j
public class DateUtil {

    public static Date get(String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return sdf.parse(pattern);
        } catch (ParseException e) {
            log.error("日期转换异常 pattern = {}, error ={}", pattern, e);
            throw new RuntimeException(e);
        }
    }
}
