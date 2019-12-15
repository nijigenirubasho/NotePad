package i.notepad.util;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * 内容工具类
 * <p>
 * 提供内容访问转移等相关操作
 *
 * @author 511721050589
 */

public class ContentUtils {

    private static final String TAG = "ContentUtils";

    private ContentUtils() {
    }

    /**
     * 分享
     * <p>
     * 弹出分享菜单
     *
     * @param context 上下文
     * @param subject 主题（标题）
     * @param text    更详细的文本内容
     */
    public static void share(@NonNull Context context, String subject, String text) {
        Intent intentSend = new Intent(Intent.ACTION_SEND);
        intentSend.setType("text/plain");
        intentSend.putExtra(Intent.EXTRA_SUBJECT, subject);
        intentSend.putExtra(Intent.EXTRA_TEXT, text);
        intentSend.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(Intent.createChooser(intentSend, subject)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "share: ActivityNotFoundException");
        }
    }

    /**
     * 复制到剪贴板
     *
     * @param context 上下文
     * @param text    要复制的文本
     */
    public static void copyToClipboard(Context context, String text) {
        ((ClipboardManager) Objects.requireNonNull(context.getSystemService(Context.CLIPBOARD_SERVICE)))
                .setPrimaryClip(ClipData.newPlainText(null, text));
    }
}
