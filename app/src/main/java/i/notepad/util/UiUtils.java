package i.notepad.util;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;

/**
 * 用户界面工具
 *
 * @author 511721050589
 */

public class UiUtils {

    private static final String TAG = "UiUtils";

    private UiUtils() {
    }

    public static void showToast(Context context, @StringRes int msg, boolean isLong) {
        showToast(context, context.getString(msg), isLong);
    }

    public static void showToast(Context context, String msg, boolean isLong) {
        boolean isMi = AndroidCompat.isMiui();
        // MIUI的Toast在文本前面添加应用名称很不合理，因为Toast显示时间有限，需要尽快让用户注意最重要的内容
        Toast toast = Toast.makeText(context.getApplicationContext(), isMi ? null : msg, isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        if (isMi) toast.setText(msg);
        toast.show();
    }

    public static void showToast(Context context, String msg) {
        showToast(context, msg, false);
    }

    public static void showToast(Context context, @StringRes int msg) {
        showToast(context, msg, false);
    }

    /**
     * 使菜单上的图标可见
     *
     * @param menu 菜单
     */
    public static void setOptionalIconsVisibleOnMenu(Menu menu) {
        if (menu != null) {
            try {
                Method setOptionalIconsVisible = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                setOptionalIconsVisible.setAccessible(true);
                setOptionalIconsVisible.invoke(menu, true);
            } catch (Exception e) {
                Log.w(TAG, "setOptionalIconsVisibleOnMenu: ", e);
            }
        }
    }

    public static void showMessageDialog(Context context, @StringRes int title, @StringRes int msg) {
        showMessageDialog(context, context.getString(title), context.getString(msg));
    }

    public static void showMessageDialog(Context context, String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.show();
    }

    /**
     * 在搜索视图上隐藏搜索图标
     *
     * @param view 待处理搜索视图
     */
    public static void hideCloseButtonOnSearchView(SearchView view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Class search = SearchView.class;
            try {
                @SuppressWarnings("JavaReflectionMemberAccess")
                Field mCloseButton = search.getDeclaredField("mCloseButton");
                mCloseButton.setAccessible(true);
                ImageView mCloseButtonInstance = (ImageView) mCloseButton.get(view);
                if (mCloseButtonInstance != null) {
                    mCloseButtonInstance.setEnabled(false);
                    mCloseButtonInstance.setImageDrawable(null);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                Log.e(TAG, "hideCloseButtonOnSearchView: ", e);
            }
        }
    }

    /**
     * 使一组视图不可见
     *
     * @param isGone 是否使用{@code View.GONE}隐藏，否则使用{@code View.INVISIBLE}
     * @param view   待处理视图们
     * @param <V>    视图子类
     * @see View#setVisibility(int)
     */
    @SafeVarargs
    public static <V extends View> void curtain(boolean isGone, V... view) {
        for (V v : view) v.setVisibility(isGone ? View.GONE : View.INVISIBLE);
    }

    /**
     * 定义系统Toast
     * <p>
     * 防止通知权限被关闭而无法弹出Toast
     */
    @SuppressWarnings("JavaReflectionMemberAccess")
    public static void defineSystemToast() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            try {
                @SuppressLint("DiscouragedPrivateApi")
                Method getServiceMethod = Toast.class.getDeclaredMethod("getService");
                getServiceMethod.setAccessible(true);
                final Object iNotificationManagerObj = getServiceMethod.invoke(null);
                @SuppressLint("PrivateApi")
                Class iNotificationManagerCls = Class.forName("android.app.INotificationManager");
                Object iNotificationManagerProxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                        new Class[]{iNotificationManagerCls}, new InvocationHandler() {
                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                if ("enqueueToast".equals(method.getName())) {
                                    Log.d(TAG, "methodName:" + method.getName() + " D:" + args[2]);
                                    args[0] = "android";
                                }
                                return method.invoke(iNotificationManagerObj, args);
                            }
                        });
                Field sServiceField = Toast.class.getDeclaredField("sService");
                sServiceField.setAccessible(true);
                sServiceField.set(null, iNotificationManagerProxy);
            } catch (Exception e) {
                Log.e(TAG, "defineSystemToast: ", e);
            }
        }
    }

    /**
     * 创建App Shortcut
     *
     * @param context 上下文
     * @param title   标题资源身份标识
     * @param icon    图标资源身份标识
     * @param i       启动意图
     */
    public static void createAppShortcut(Context context, @StringRes int title, @DrawableRes int icon, Intent i) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            String titleStr = context.getString(title);
            ShortcutInfo shortcutInfo = new ShortcutInfo.Builder(context, titleStr)
                    .setShortLabel(titleStr)
                    .setIcon(Icon.createWithResource(context, icon))
                    .setIntent(i)
                    .build();
            ShortcutManager manager = context.getSystemService(ShortcutManager.class);
            if (manager != null) {
                manager.setDynamicShortcuts(Collections.singletonList(shortcutInfo));
            }
        }
    }

    public static void setEditTextEditable(EditText editText, boolean editable) {
        if (!editable) {
            InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(editText.getApplicationWindowToken(), 0);
        }
        editText.setFocusable(editable);
        editText.setFocusableInTouchMode(editable);
        editText.setLongClickable(editable);
    }
}
