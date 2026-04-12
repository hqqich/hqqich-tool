package io.github.hqqich.tool.base.text;

/**
 * 全局正则
 */
public class GlobalRegexp {

    // 私有构造，避免被实例化
    private GlobalRegexp() {}

    public static final String FIELDS = "^[_a-zA-Z0-9]+(,[_a-zA-Z0-9]+)*$"; // 字段
    public static final String IDCARD = "^\\d{17}[\\d|xX]|\\d{15}$"; // 身份证号码
    public static final String U_INTS = "^\\d+(,\\d+)*$"; // 无符号整数集
    public static final String INTS = "^-?\\d+(,-?\\d+)*$"; // 整数集
    public static final String ORDERS = "^(asc|desc)+(,(asc|desc)+)*$"; // 排序方式
    public static final String PHONE = "^(?!.*\\p{IsHan}).+$"; // 非中文的任意字符
    public static final String HEX = "^[a-fA-F0-9]+$";
    public static final String MD5 = "^[a-fA-F0-9]{32}$";
    public static final String JWT = "^([A-Za-z0-9\\-_~+\\/]+[=]{0,2})\\." +
            "([A-Za-z0-9\\-_~+\\/]+[=]{0,2})(?:\\.([A-Za-z0-9\\-_~+\\/]+[=]{0,2}))?$";
    public static final String MAC = "^([a-fA-F0-9][a-fA-F0-9]:){5}([a-fA-F0-9][a-fA-F0-9])$";
    public static final String MAC_NO_COLON = "^([a-fA-F0-9]){12}$";
    public static final String HOST = "^((?!-)[a-zA-Z0-9-]{1,63}(?<!-)\\.)+[a-zA-Z]{2,6}$";
    public static final String BASE_32 = "^[a-zA-Z2-7]+={0,6}$";
    public static final String BASE_64 = "^[a-zA-Z0-9\\+\\/]+={0,2}$";
    public static final String DATA_URI = "^data:([a-z]+\\/[a-z0-9\\-\\+]+)?" + "(;charset=[a-zA-Z0-9\\-]+)?(;base64)?,.+$";

}
