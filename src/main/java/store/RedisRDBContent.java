package store;

import java.util.Base64;

public class RedisRDBContent {
    public static String base64RDB = "UkVESVMwMDEx+glyZWRpcy12ZXIFNy4yLjD6CnJlZGlzLWJpdHPAQPoFY3RpbWXCbQi8ZfoIdXNlZC1tZW3CsMQQAPoIYW9mLWJhc2XAAP/wbjv+wP9aog==";
    public static byte[] RDBFile = Base64.getDecoder().decode(base64RDB);
}
