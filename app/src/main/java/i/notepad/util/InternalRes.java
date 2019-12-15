package i.notepad.util;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Arrays;

/**
 * 内部资源工具类
 * <p>
 * 访问Android内部存在的但不直接暴露的资源
 * 好处是尽可能减小应用体积，提高资源重用性，对于字符串资源能无成本做到本地化
 *
 * @author 511721050589
 */

public class InternalRes {

    private static final String TAG = "InternalRes";
    private static final Resources sSysRes = Resources.getSystem();

    private InternalRes() {
    }

    private static int getAndroidResId(String resType, String resName) {
        int id = sSysRes.getIdentifier(resName, resType, "android");
        if (id == 0)
            Log.e(TAG, "getAndroidResId: " + Arrays.asList(resType, resName) + " not found on " + Build.VERSION.SDK_INT);
        return id;
    }

    public static String getString(@NonNull String resName) {
        return sSysRes.getString(getAndroidResId("string", resName));
    }

    public static Drawable getDrawable(@NonNull String resName) {
        return sSysRes.getDrawable(getAndroidResId("drawable", resName));
    }


    public final class R {

        public final class string {
            public static final String share = "share";
            public static final String delete = "delete";
            public static final String text_copied = "text_copied";
            public static final String storage_internal = "storage_internal";
        }

        @SuppressWarnings("SpellCheckingInspection")
        public final class drawable {
            public static final String ic_menu_search_mtrl_alpha = "ic_menu_search_mtrl_alpha";
            public static final String ic_menu_find_holo_dark = "ic_menu_find_holo_dark";
            public static final String ic_menu_find_mtrl_alpha = "ic_menu_find_mtrl_alpha";
            public static final String ic_menu_copy_holo_dark = "ic_menu_copy_holo_dark";
            public static final String ic_lock_outline_wht_24dp = "ic_lock_outline_wht_24dp";
            public static final String ic_lock_open_wht_24dp = "ic_lock_open_wht_24dp";
        }
    }
}
