package i.notepad.util;

import android.text.TextUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 字符串工具
 * <p>
 * 提供字符串处理和转化相关功能
 *
 * @author 511721050589
 */

public class StringUtils {

    private StringUtils() {
    }

    /**
     * 将堆栈跟踪信息转化为字符串
     *
     * @param t 堆栈跟踪信息
     * @return 字符串
     */
    public static String printStackTraceToString(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw, true));
        return sw.getBuffer().toString();
    }

    /**
     * 检测字符串是否有非空内容
     *
     * @param s 字符串
     * @return 如果有非空内容返回真，否则反之
     */
    public static boolean isEmptyAfterTrim(String s) {
        return TextUtils.isEmpty(s) || s.trim().length() == 0;
    }

    /**
     * 删除字符串中的所有空格
     *
     * @param src 源字符串
     * @return 目标字符串
     */
    public static String trimAll(String src) {
        return src.replaceAll("\\s+", "");
    }

    /**
     * 删除文件名中的非法字符
     *
     * @param fileName 文件名
     * @return 合法的文件名
     */
    public static String trimFileName(String fileName) {
        return fileName.replaceAll("[/\\\\:*?\"<>|]", "");
    }

    /**
     * 返回以秒为单位的最短时间身份标识
     *
     * @return 例如：20191210233336
     */
    @SuppressWarnings("SpellCheckingInspection")
    public static String getTimeIdString() {
        return new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
    }
}
