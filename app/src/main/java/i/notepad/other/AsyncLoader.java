package i.notepad.other;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.IntDef;

import java.io.File;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import i.notepad.BuildConfig;
import i.notepad.R;
import i.notepad.activity.EditorActivity;
import i.notepad.activity.MainActivity;
import i.notepad.adapter.NoteDataAdapter;
import i.notepad.bean.Note;
import i.notepad.util.FileUtils;
import i.notepad.util.InternalRes;
import i.notepad.util.StringUtils;

/**
 * 异步加载器
 * <p>
 * 异步处理具体的耗时事务，防止UI线程卡顿
 *
 * @author 511721050589
 */

public class AsyncLoader extends AsyncTaskLoader<List<Note>> {

    private static final String TAG = "AsyncLoader";

    static {
        LoaderManager.enableDebugLogging(BuildConfig.DEBUG);
    }

    private List<Note> mNoteList = new ArrayList<>();
    private Bundle mArgs;

    public AsyncLoader(Context context, Bundle args) {
        super(context);
        mArgs = args;
    }

    /**
     * 取参数{@code Bundle}
     *
     * @param action  操作类型
     * @param id      身份标识
     * @param note    记事数据
     * @param keyword 关键词（搜索用）
     * @return {@code Bundle}
     */
    public static Bundle getArgBundle(@ActionTypeDef int action, long id, Note note, String keyword) {
        Bundle bundle = new Bundle();
        bundle.putInt(ArgKey.ACTION, action);
        bundle.putLong(ArgKey.ID, id);
        if (note != null) bundle.putParcelable(ArgKey.NOTE, note);
        if (keyword != null) bundle.putString(ArgKey.KEYWORD, keyword);
        return bundle;
    }

    @Override
    public List<Note> loadInBackground() {
        long start = SystemClock.currentThreadTimeMillis();
        int action = mArgs.getInt(ArgKey.ACTION);
        Log.i(TAG, "loadInBackground: action=" + action + " thread=" + Thread.currentThread());
        long id = mArgs.getLong(ArgKey.ID);
        Note note = mArgs.getParcelable(ArgKey.NOTE);
        NoteDataAdapter adapter = NoteDataAdapter.getInstance(getContext());
        switch (action) {
            case ActionType.FIND_ALL:
                mNoteList = adapter.findAll();
                break;
            case ActionType.FIND_BY_ID:
                mNoteList = Collections.singletonList(adapter.findById(id));
                break;
            case ActionType.SEARCH:
                /* 当搜索标题的结果不存在时，再搜索内容 */
                String keyWord = mArgs.getString(ArgKey.KEYWORD);
                if (TextUtils.isEmpty(keyWord)) {
                    Log.v(TAG, "loadInBackground: keyword is empty");
                    mNoteList = adapter.findAll();
                } else {
                    mNoteList = adapter.findByTitle(keyWord);
                    if (mNoteList.size() == 0) {
                        Log.w(TAG, "loadInBackground: findByTitle(keyWord) is empty");
                        mNoteList = adapter.findByBody(keyWord);
                    }
                }
                break;
            case ActionType.INSERT:
                long insertRow = adapter.insert(note);
                if (getId() == MainActivity.LOADER_ID) mNoteList = adapter.findAll();
                else {
                    if (note != null) note.setId(insertRow);
                    Log.v(TAG, "loadInBackground: return note to EditorActivity. From " + action);
                    mNoteList = Collections.singletonList(note);
                }
                Log.d(TAG, "loadInBackground: insertRow=" + insertRow);
                break;
            case ActionType.UPDATE:
                adapter.update(id, note);
                if (getId() == EditorActivity.LOADER_ID) {
                    Log.v(TAG, "loadInBackground: return note to EditorActivity. From " + action);
                    mNoteList = Collections.singletonList(note);
                }
                break;
            case ActionType.DELETE:
                adapter.delete(id);
                mNoteList = adapter.findAll();
                break;
            case ActionType.EXPORT:
                assert note != null;
                String title = note.getTitle(), body = note.getBody();
                String fullPath = Objects.requireNonNull(
                        getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)).getAbsolutePath() + File.separator +
                        StringUtils.trimFileName(title + "." + StringUtils.getTimeIdString() + ".txt");
                FileUtils.writeTextFile(fullPath,
                        String.format(getContext().getString(R.string.content_copy_template), title, body));
                Note ret = new Note();
                // 将路径通过记事内容返回
                ret.setBody(fullPath.replace(Environment.getExternalStoragePublicDirectory("").getAbsolutePath(),
                        InternalRes.getString(InternalRes.R.string.storage_internal)));
                mNoteList = Collections.singletonList(ret);
                break;
            default:
                Log.e(TAG, "loadInBackground: !");
        }
        Log.i(TAG, "loadInBackground: duration=" + (SystemClock.currentThreadTimeMillis() - start));
        return mNoteList;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public void deliverResult(List<Note> data) {
        super.deliverResult(data);
    }

    private interface ArgKey {
        String
                ACTION = "action",
                ID = "id",
                NOTE = "note",
                KEYWORD = "keyword";
    }

    public interface ActionType {
        int
                FIND_ALL = 1,
                FIND_BY_ID = 2,
                SEARCH = 3,
                INSERT = 4,
                UPDATE = 5,
                DELETE = 6,
                EXPORT = 7;
    }

    @Documented
    @IntDef({
            ActionType.FIND_ALL,
            ActionType.DELETE,
            ActionType.FIND_BY_ID,
            ActionType.INSERT,
            ActionType.SEARCH,
            ActionType.UPDATE,
            ActionType.EXPORT
    })
    @Target({
            ElementType.PARAMETER,
            ElementType.FIELD,
            ElementType.METHOD,
    })
    @Retention(RetentionPolicy.SOURCE)
    private @interface ActionTypeDef {
    }
}