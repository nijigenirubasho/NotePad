package i.notepad;

import android.app.Application;
import android.os.Process;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Log;
import android.util.LogPrinter;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.Arrays;

import i.notepad.activity.EditorActivity;
import i.notepad.util.AndroidCompat;
import i.notepad.util.FileUtils;
import i.notepad.util.OsUtils;
import i.notepad.util.StringUtils;
import i.notepad.util.UiUtils;

/**
 * 自定义{@code Application}
 *
 * @author 511721050589
 */

public class _Application extends Application implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "_Application";

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(this);

        // Miui似乎在很遥远的过去就修复了这个bug
        if (!AndroidCompat.isMiui()) UiUtils.defineSystemToast();

        /* 严格模式测试 */
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyFlashScreen()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
        }
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        long now = System.currentTimeMillis();
        String buildInfo = OsUtils.getJsonBuildInfo();
        String crashInfo = OsUtils.getJsonCrashReportInfo(BuildConfig.APPLICATION_ID,
                OsUtils.getCurrentProcessName(this),
                now,
                OsUtils.isSystemApp(this),
                getPackageManager().getInstallerPackageName(BuildConfig.APPLICATION_ID),
                e);
        String allInfo = t + System.lineSeparator() +
                crashInfo + System.lineSeparator() +
                buildInfo;

        Log.e(TAG, "uncaughtException: " + System.lineSeparator() + allInfo, e);

        if (BuildConfig.DEBUG) {
            EditorActivity.actionStart(this,
                    EditorActivity.ActionType.CREATE,
                    0,
                    new String[]{t.toString(), allInfo});
            Log.i(TAG, "uncaughtException: show on editor");
        } else {
            // 由于Log类代码在发布时已经被proguard删除掉，所以需要用另外的方法打印日志
            LogPrinter crashPrinter = new LogPrinter(Log.ERROR, TAG);
            crashPrinter.println(Arrays.asList(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE) + " crashed" + System.lineSeparator() +
                    "crash info:" + allInfo + System.lineSeparator() +
                    "raw stacktrace:" + StringUtils.printStackTraceToString(e));
        }

        // 输出崩溃日志文件
        File crashLogDir = getExternalFilesDir("crash_log");
        if (crashLogDir != null)
            FileUtils.writeTextFile(crashLogDir.getAbsolutePath() + File.separator + now + ".log", allInfo);

        // 阻塞可以阻止FC弹窗提示
        SystemClock.sleep(1000);
        Process.killProcess(Process.myPid());
        System.exit(-1);
    }
}
