package io.github.hqqich.tool.base;



import io.github.hqqich.tool.base.text.GlobalRegexp;

import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * 验证工具
 */
public class VerifyUtils {


    public static boolean isIP(String str) {
        if (isBlank(str)) {
            return false;
        }

        String[] parts = str.split("\\.");
        if (parts.length != 4) {
            return false;
        }

        for (String part : parts) {
            try {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return true;
    }


    public static boolean isHOST(String string) {
        if (isBlank(string)) {
            return false;
        }
        return Pattern.matches(GlobalRegexp.HOST, string)
                || "localhost".equalsIgnoreCase(string);
    }

    public static boolean isURL(String string) {
        if (isBlank(string)) {
            return false;
        }
        return true;
    }

    public static boolean isMAC(String string) {
        if (isBlank(string)) {
            return false;
        }
        return Pattern.matches(GlobalRegexp.MAC, string);
    }

    public static boolean isMacNoColon(String string) {
        if (isBlank(string)) {
            return false;
        }
        return Pattern.matches(GlobalRegexp.MAC_NO_COLON, string);
    }

    public static boolean isHEX(String string) {
        if (isBlank(string)) {
            return false;
        }
        return Pattern.matches(GlobalRegexp.HEX, string);
    }

    public static boolean isMD5(String string) {
        if (isBlank(string)) {
            return false;
        }
        return Pattern.matches(GlobalRegexp.MD5, string);
    }

    public static boolean isJWT(String string) {
        if (isBlank(string)) {
            return false;
        }
        return Pattern.matches(GlobalRegexp.JWT, string);
    }

    public static boolean isUUID(String string) {
        if (isBlank(string)) {
            return false;
        }
        return true;
    }

    public static boolean isJSON(String string) {
        if (isBlank(string)) {
            return false;
        }
        return true;
    }

    public static boolean isBase16(String string) {
        if (isBlank(string)) {
            return false;
        }
        return string.length() % 2 == 0 && Pattern.matches(GlobalRegexp.HEX, string);
    }

    public static boolean isBase32(String string) {
        if (isBlank(string)) {
            return false;
        }
        return string.length() % 8 == 0 && Pattern.matches(GlobalRegexp.BASE_32, string);
    }

    public static boolean isBase64(String string) {
        if (isBlank(string)) {
            return false;
        }
        return string.length() % 4 == 0 && Pattern.matches(GlobalRegexp.BASE_64, string);
    }

    public static boolean isDataURI(String string) {
        if (isBlank(string)) {
            return false;
        }
        return string.length() > 6 && Pattern.matches(GlobalRegexp.DATA_URI, string);
    }

    public static boolean isInts(String string) {
        if (isBlank(string)) {
            return false;
        }
        return Pattern.matches(GlobalRegexp.INTS, string);
    }

    public static boolean isUInts(String string) {
        if (isBlank(string)) {
            return false;
        }
        return Pattern.matches(GlobalRegexp.U_INTS, string);
    }

    /**
     * 验证字符串是否是符合指定模式的有效日期时间 例1 验证 "20090229" 是否是符合模式 "yyyyMMdd" 的有效日期 例2 验证 "23:29:59" 是否是符合模式 "HH:mm:ss" 的有效时间
     *
     * @param pattern 指定模式
     * @param string  待验字符串
     * @return boolean
     */
    public static boolean isDateTime(String pattern, String string) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            simpleDateFormat.setLenient(false);
            simpleDateFormat.parse(string);
            return pattern.length() == string.length();
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean isFields(String string) {
        if (isBlank(string)) {
            return false;
        }
        return Pattern.matches(GlobalRegexp.FIELDS, string);
    }

    public static boolean isIdcard(String string) {
        if (isBlank(string)) {
            return false;
        }
        return Pattern.matches(GlobalRegexp.IDCARD, string);
    }

    public static boolean isIndexs(String string) {
        if (isBlank(string)) {
            return false;
        }
        return Pattern.matches(GlobalRegexp.U_INTS, string);
    }

    public static boolean isOrders(String string) {
        if (isBlank(string)) {
            return false;
        }
        return Pattern.compile(GlobalRegexp.ORDERS, Pattern.CASE_INSENSITIVE)
                .matcher(string).matches();
    }

    public static boolean isPhone(String string) {
        // 1. 如果字符串为 null 或空，直接返回 true（不校验必填）
        if (string == null || string.trim().isEmpty()) {
            return true; // 改为 true，表示不校验必填
        }

        // 2. 检查长度是否在合理范围内（3~20，避免过短或过长）
        if (string.length() < 3 || string.length() > 20) {
            return false;
        }


        return true;
    }

    /**
     * Validates if the given string is a valid email address
     * @param email the string to validate
     * @return true if the string is a valid email, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }

        String emailRegex = "^[\\w.-]+@([\\w-]+\\.)+[\\w-]{2,}$";
        return email.matches(emailRegex);
    }



}
