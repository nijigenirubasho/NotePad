package i.notepad.util;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * “安卓”兼容工具
 * <p>
 * 存放一些用以兼容某些特色化的Android系统环境的工具
 *
 * @author 511721050589
 */

public class AndroidCompat {

    private static final String TAG = "AndroidCompat";

    private AndroidCompat() {
    }

    /**
     * 检测是否是MIUI系统
     *
     * @return bool
     */
    @SuppressWarnings("SpellCheckingInspection")    // MIUI确实不是英语词汇
    public static boolean isMiui() {
        //android.os.SystemProperties.get("ro.miui.ui.version.name", "");
        // 如果返回值是「V10」，就是 MIUI 10
        String miuiVersionName;
        try {
            @SuppressLint("PrivateApi")
            Class SystemProperties = Class.forName("android.os.SystemProperties");
            @SuppressWarnings("unchecked")
            Method get = SystemProperties.getMethod("get", String.class, String.class);
            miuiVersionName = String.valueOf(get.invoke(null, "ro.miui.ui.version.name", ""));
        } catch (Throwable throwable) {
            Log.w(TAG, "isMiui: ", throwable);
            return false;
        }
        Log.d(TAG, "isMiui: miuiVersionName:" + miuiVersionName);
        return !TextUtils.isEmpty(miuiVersionName);
    }
}
