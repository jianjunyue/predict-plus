package com.predict.plus.common.utils;


import lombok.extern.slf4j.Slf4j;

/**
 * <p>日志工具类-日志统一出入口</p>
 *
 * @Author: fc.w
 * @Date: 2020/07/17 11:59
 */
@Slf4j
public class LogUtils {

    /**
     * 逻辑debug，底层还是info日志级别
     * @param switchStatus
     * @param className
     * @param methodName
     * @param key
     * @param data
     */
    public static void logDebug(boolean switchStatus, String className, String methodName, String key, String data) {
        if (switchStatus) {
            log.info("{}-{}-{}:{}", className, methodName, key, data);
        }
    }

    public static void logDebug(boolean switchStatus, String className, String methodName, String data) {
        if (switchStatus) {
            log.info("{}-{}-{}", className, methodName, data);
        }
    }

    public static void logDebug(boolean switchStatus, String key, String data) {
        if (switchStatus) {
            log.info("{}:{}", key, data);
        }
    }

    /**
     *
     * @param switchStatus 日志开关
     * @param className
     * @param methodName
     * @param key
     * @param data
     */
    public static void logInfo(boolean switchStatus, String className, String methodName, String key, String data) {
        if (switchStatus) {
            log.info("{}-{}-{}:{}", className, methodName, key, data);
        }
    }

    public static void logInfo(boolean switchStatus, String className, String methodName, String data) {
        if (switchStatus) {
            log.info("{}-{}-{}", className, methodName, data);
        }
    }

    public static void logInfo(boolean switchStatus, String key, String data) {
        if (switchStatus) {
            log.info("{}:{}", key, data);
        }
    }

    /**
     *
     * @param switchStatus
     * @param className
     * @param methodName
     * @param key
     * @param e
     */
    public static void logWarn(boolean switchStatus, String className, String methodName, String key, Exception e) {
        if (switchStatus) {
            log.warn("{}-{}-{}", className, methodName, key, e);
        }
    }

    public static void logWarn(boolean switchStatus, String className, String methodName, String key, String value, Exception e) {
        if (switchStatus) {
            log.warn("{}-{}-{}-{}", className, methodName, key, value, e);
        }
    }

    public static void logWarn(boolean switchStatus, String className, String methodName, String key, String value) {
        if (switchStatus) {
            log.warn("{}-{}-{}-{}", className, methodName, key, value);
        }
    }

    /**
     *
     * @param switchStatus
     * @param className
     * @param methodName
     * @param key
     * @param e
     */
    public static void logError(boolean switchStatus, String className, String methodName, String key, Exception e) {
        if (switchStatus) {
            log.error("{}-{}-{}", className, methodName, key, e);
        }
    }


    public static void logInfo1(boolean switchStatus, String className, String methodName, String...contents) {
        if (switchStatus) {
            String data = processContents(contents);
            log.info("{}-{}-{}", className, methodName, data);
        }
    }

    private static String processContents(String...contents) {
        if (null != contents) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < contents.length; ++i) {
                String content = contents[i];
                sb.append(content)
                        .append(content == null ? null : "-");
            }
            return sb.toString();
        }

        return "";
    }


    public static String parse(String openToken, String closeToken, String text, Object... args) {
        if (args == null || args.length <= 0) {
            return text;
        }
        int argsIndex = 0;

        if (text == null || text.isEmpty()) {
            return "";
        }
        char[] src = text.toCharArray();
        int offset = 0;
        // search open token
        int start = text.indexOf(openToken, offset);
        if (start == -1) {
            return text;
        }
        final StringBuilder builder = new StringBuilder();
        StringBuilder expression = null;
        while (start > -1) {
            if (start > 0 && src[start - 1] == '\\') {
                // this open token is escaped. remove the backslash and continue.
                builder.append(src, offset, start - offset - 1).append(openToken);
                offset = start + openToken.length();
            } else {
                // found open token. let's search close token.
                if (expression == null) {
                    expression = new StringBuilder();
                } else {
                    expression.setLength(0);
                }
                builder.append(src, offset, start - offset);
                offset = start + openToken.length();
                int end = text.indexOf(closeToken, offset);
                while (end > -1) {
                    if (end > offset && src[end - 1] == '\\') {
                        // this close token is escaped. remove the backslash and continue.
                        expression.append(src, offset, end - offset - 1).append(closeToken);
                        offset = end + closeToken.length();
                        end = text.indexOf(closeToken, offset);
                    } else {
                        expression.append(src, offset, end - offset);
                        offset = end + closeToken.length();
                        break;
                    }
                }
                if (end == -1) {
                    // close token was not found.
                    builder.append(src, start, src.length - start);
                    offset = src.length;
                } else {
                    ///////////////////////////////////////仅仅修改了该else分支下的个别行代码////////////////////////

                    String value = (argsIndex <= args.length - 1) ?
                            (args[argsIndex] == null ? "" : args[argsIndex].toString()) : expression.toString();
                    builder.append(value);
                    offset = end + closeToken.length();
                    argsIndex++;
                    ////////////////////////////////////////////////////////////////////////////////////////////////
                }
            }
            start = text.indexOf(openToken, offset);
        }
        if (offset < src.length) {
            builder.append(src, offset, src.length - offset);
        }
        return builder.toString();
    }

    public static String parse0(String text, Object... args) {
        return parse("${", "}", text, args);
    }


    public static String parse1(String text, Object... args) {
        return parse("{", "}", text, args);
    }

    public static void main(String[] args) {
        long a = System.currentTimeMillis();
        System.out.println(parse0("我的名字是${},结果是${}，可信度是%${}", "雷锋", true, 100));
        System.out.println(System.currentTimeMillis() - a);
        System.out.println(parse1("我的名字是{},结果是{}，可信度是{}", "雷锋", true, 100));
    }

}
