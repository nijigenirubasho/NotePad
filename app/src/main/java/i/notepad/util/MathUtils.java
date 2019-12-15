package i.notepad.util;

/**
 * 数学工具
 *
 * @author 511721050589
 */

public class MathUtils {

    private MathUtils() {
    }

    /**
     * 取数值的位数
     *
     * @param num 待计算数值
     * @return 举一个例子，如果{@code num}是10000-99999的任何一个数值，返回5
     */
    public static int getDigitLength(long num) {
        if (num == 0) return 1;
        num = Math.abs(num);
        return (int) Math.log10(num) + 1;
    }

    /**
     * 求一组整数的平均值
     *
     * @param numberList 一组整数
     * @return 平均值
     */
    public static long average(long... numberList) {
        long len = numberList.length, sum = 0;
        for (long num : numberList) sum += num;
        return sum / len;
    }
}
