package i.notepad.activity;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import i.notepad.BuildConfig;
import i.notepad.R;
import i.notepad.bean.Note;
import i.notepad.other.AsyncLoader;
import i.notepad.util.ContentUtils;
import i.notepad.util.InternalRes;
import i.notepad.util.StringUtils;
import i.notepad.util.UiUtils;
import i.notepad.widget.EditorEditText;

/**
 * 编辑器{@code Activity}
 * <p>
 * 用于编辑单一记事的数据，
 * 并接受来自外部的文本数据当作记事修改及保存
 *
 * @author 511721050589
 */

public class EditorActivity extends Activity implements LoaderManager.LoaderCallbacks<List<Note>> {


    /**
     * Action：从App Shortcut打开
     */
    public static final String SHORTCUT_ACTION_CREATE = BuildConfig.APPLICATION_ID + ".CREATE_NOTE_FROM_SHORTCUT";
    public static final int LOADER_ID = 33;
    private static final String TAG = "EditorActivity";
    private static final String
            EXTRA_TAG_TYPE = "extra.type",
            EXTRA_TAG_ID = "extra.id",
            EXTRA_TAG_CONTENT = "extra.content",
            BUNDLE_TAG_TITLE = "bundle.title",
            BUNDLE_TAG_BODY = "bundle.body";
    /**
     * 标题限制长度
     */
    private static final int TITLE_MAX_LENGTH = 140;
    private ActionType mType;
    private long mId;
    private EditText mTitleEditor;
    private EditorEditText mBodyEditor;
    private String[] mExtraContent;
    private boolean mIsTextChanged, mIsCalledFromShortcut, mIsLocked, mUpdateInitWorkExecuted;

    /**
     * 启动编辑器
     *
     * @param context context
     * @param type    动作类型{@code ActionType}
     * @param id      身份标识，修改记事时使用
     * @param content [title,body] 从extra打开时使用
     */
    public static void actionStart(Context context, @NonNull ActionType type, long id, String[] content) {
        Intent intent = new Intent(context, EditorActivity.class);
        intent.putExtra(EXTRA_TAG_TYPE, type);
        intent.putExtra(EXTRA_TAG_ID, id);
        intent.putExtra(EXTRA_TAG_CONTENT, content);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mTitleEditor = findViewById(R.id.et_editor_title_editor);
        mBodyEditor = findViewById(R.id.et_editor_body_editor);

        /* 标题编辑框加粗字体和限制字数 */
        mTitleEditor.getPaint().setFakeBoldText(true);
        mTitleEditor.setFilters(new InputFilter[]{new InputFilter.LengthFilter(TITLE_MAX_LENGTH)});

        /* 判断动作类型，用来分别在打开编辑器的不同场景 */
        Intent intent = getIntent();
        String action = intent.getAction();
        Log.d(TAG, "onCreate: action=" + action);
        boolean isFromSend = Intent.ACTION_SEND.equals(action) && "text/plain".equals(intent.getType());
        if (!isFromSend) {
            mType = (ActionType) intent.getSerializableExtra(EXTRA_TAG_TYPE);
            if (mType == null && SHORTCUT_ACTION_CREATE.equals(intent.getAction())) {
                Log.i(TAG, "onCreate: create note from shortcut");
                mType = ActionType.CREATE;
                mIsCalledFromShortcut = true;
            }
            mId = intent.getLongExtra(EXTRA_TAG_ID, 0);
            mExtraContent = intent.getStringArrayExtra(EXTRA_TAG_CONTENT);
        } else mType = ActionType.SEND;

        if (savedInstanceState != null) {
            Log.d(TAG, "onCreate: savedInstanceState");
            mTitleEditor.setText(savedInstanceState.getString(BUNDLE_TAG_TITLE));
            mBodyEditor.setText(savedInstanceState.getString(BUNDLE_TAG_BODY));
        }

        /* 预先填充待编辑的文本 */
        if (mType.equals(ActionType.UPDATE) && mId != 0) {
            Log.i(TAG, "onCreate: change note " + mId);
            getLoaderManager().initLoader(LOADER_ID, AsyncLoader.getArgBundle(AsyncLoader.ActionType.FIND_BY_ID, mId, null, null), this);
        } else if (mType.equals(ActionType.CREATE) && mExtraContent != null && mExtraContent.length == 2) {
            Log.i(TAG, "onCreate: create note from extra");
            mTitleEditor.setText(mExtraContent[0]);
            mBodyEditor.setText(mExtraContent[1]);
        } else if (isFromSend) {
            Log.i(TAG, "onCreate: create note from send");
            mTitleEditor.setText(intent.getStringExtra(Intent.EXTRA_SUBJECT));
            mBodyEditor.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
        }

        /* 检测文字是否改动 注意：在此之后不要再调用setText方法，会误触发 */
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                /* 由于设定监听比在编辑框中填写详细信息的时机更前，因此在更新的时候必须判断编辑框已经填写了信息 */
                if (mType.equals(ActionType.UPDATE)) {
                    if (mUpdateInitWorkExecuted) mIsTextChanged = true;
                } else mIsTextChanged = true;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        mTitleEditor.addTextChangedListener(watcher);
        mBodyEditor.addTextChangedListener(watcher);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        save();
        if (!mType.equals(ActionType.SEND) && !mIsCalledFromShortcut) MainActivity.start(this);
        super.onBackPressed();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        UiUtils.setOptionalIconsVisibleOnMenu(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor, menu);

        /* 菜单选项定义外观 */
        MenuItem countItem = menu.findItem(R.id.item_editor_count);
        countItem.setIcon(InternalRes.getDrawable(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ?
                InternalRes.R.drawable.ic_menu_find_holo_dark : InternalRes.R.drawable.ic_menu_find_mtrl_alpha));
        MenuItem copyItem = menu.findItem(R.id.item_editor_copy);
        copyItem.setIcon(InternalRes.getDrawable(InternalRes.R.drawable.ic_menu_copy_holo_dark));
        MenuItem lockItem = menu.findItem(R.id.item_editor_edit_lock);
        lockItem.setTitle(R.string.lock);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = InternalRes.getDrawable(InternalRes.R.drawable.ic_lock_outline_wht_24dp);
            drawable.setTint(Color.RED);
            lockItem.setIcon(drawable);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            /* 放弃保存 */
            case R.id.item_editor_discard:
                MainActivity.start(this);
                UiUtils.showToast(this, R.string.note_discard_hint);
                finish();
                break;
            /* 清除编辑内容 */
            case R.id.item_editor_clear:
                mTitleEditor.setText(null);
                mBodyEditor.setText(null);
                break;
            /* 复制到剪贴板 */
            case R.id.item_editor_copy:
                ContentUtils.copyToClipboard(this, String.format(getString(R.string.content_copy_template), mTitleEditor.getText(), mBodyEditor.getText()));
                UiUtils.showToast(this, InternalRes.getString(InternalRes.R.string.text_copied));
                break;
            /* 统计字数 */
            case R.id.item_editor_count:
                String title = mTitleEditor.getText().toString(), body = mBodyEditor.getText().toString();
                UiUtils.showMessageDialog(this, null, String.format(
                        Locale.getDefault(),
                        getString(R.string.count_words_content),
                        title.length(),
                        StringUtils.trimAll(title).length(),
                        body.length(),
                        StringUtils.trimAll(body).length()));
                break;
            /* 锁定或者解锁编辑器（仅查看，防止误编辑） */
            case R.id.item_editor_edit_lock:
                item.setTitle(mIsLocked ? R.string.lock : R.string.unlock);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Drawable drawable = InternalRes.getDrawable(mIsLocked ?
                            InternalRes.R.drawable.ic_lock_outline_wht_24dp :
                            InternalRes.R.drawable.ic_lock_open_wht_24dp);
                    drawable.setTint(mIsLocked ? Color.RED : Color.GREEN);
                    item.setIcon(drawable);
                }
                UiUtils.setEditTextEditable(mTitleEditor, mIsLocked);
                UiUtils.setEditTextEditable(mBodyEditor, mIsLocked);
                mIsLocked = !mIsLocked;
                break;
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_TAG_TITLE, mTitleEditor.getText().toString());
        outState.putString(BUNDLE_TAG_BODY, mBodyEditor.getText().toString());
    }


    /* 保存的主要逻辑 */
    private void save() {
        if (!mType.equals(ActionType.SEND) && !mIsTextChanged && mExtraContent == null) return;
        UiUtils.curtain(false, mTitleEditor, mBodyEditor);
        String title = mTitleEditor.getText().toString();
        String body = mBodyEditor.getText().toString();
        if (StringUtils.isEmptyAfterTrim(title) && StringUtils.isEmptyAfterTrim(body)) {
            Log.e(TAG, "onBackPressed: title and body are empty");
            Toast.makeText(this, R.string.content_null, Toast.LENGTH_SHORT).show();
            return;
        }
        String nowTime = DateFormat.getDateTimeInstance().format(new Date());
        if (StringUtils.isEmptyAfterTrim(title)) {
            Log.w(TAG, "onBackPressed: title is empty");
            mTitleEditor.setText(nowTime);
        } else if (StringUtils.isEmptyAfterTrim(body)) {
            Log.w(TAG, "onBackPressed: body is empty");
            mTitleEditor.setText(nowTime);
            mBodyEditor.setText(title);
        }
        switch (mType) {
            case CREATE:
            case SEND:
                getLoaderManager().initLoader(LOADER_ID, AsyncLoader.getArgBundle(AsyncLoader.ActionType.INSERT, 0, fetchNoteFromEditor(), null), this);
                break;
            case UPDATE:
                getLoaderManager().restartLoader(LOADER_ID, AsyncLoader.getArgBundle(AsyncLoader.ActionType.UPDATE, mId, fetchNoteFromEditor(), null), this);
                break;
        }
        UiUtils.showToast(this, R.string.note_saved, true);
    }

    private Note fetchNoteFromEditor() {
        Note note = new Note();
        note.setTitle(mTitleEditor.getText().toString());
        note.setBody(mBodyEditor.getText().toString());
        return note;
    }

    @Override
    public Loader<List<Note>> onCreateLoader(int id, Bundle args) {
        return new AsyncLoader(this, args);
    }

    @Override
    public void onLoadFinished(Loader<List<Note>> loader, List<Note> data) {
        Note note = data.get(0);
        switch (mType) {
            case CREATE:
            case SEND:
                Log.i(TAG, "onLoadFinished: insert -> id:" + note.getId());
                break;
            case UPDATE:
                Log.i(TAG, "onLoadFinished: update -> id:" + note.getId());
                if (!mUpdateInitWorkExecuted) {
                    mTitleEditor.setText(note.getTitle());
                    mBodyEditor.setText(note.getBody());
                    Log.d(TAG, "onLoadFinished: update data loaded");
                    mUpdateInitWorkExecuted = true;
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Note>> loader) {
    }

    public enum ActionType {
        CREATE, UPDATE, SEND
    }
}
