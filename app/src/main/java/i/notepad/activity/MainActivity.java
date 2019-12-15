package i.notepad.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Locale;

import i.notepad.R;
import i.notepad.adapter.NoteViewAdapter;
import i.notepad.bean.Note;
import i.notepad.other.AsyncLoader;
import i.notepad.util.ContentUtils;
import i.notepad.util.InternalRes;
import i.notepad.util.UiUtils;

/**
 * 主要{@code Activity}
 * <p>
 * 用于显示记事列表，并对此进行增加（复制），删除和查询的工作
 * 提供打开编辑器的入口
 * 显示帮助和关于信息
 *
 * @author 511721050589
 */

public class MainActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, View.OnLongClickListener, LoaderManager.LoaderCallbacks<List<Note>> {

    public static final int LOADER_ID = 22;
    private static final String TAG = "MainActivity";
    private NoteViewAdapter mNoteViewAdapter;
    private int mSelectedItemPosition;
    private ListView mListView;
    // 使用一个标记协助导出的异步操作
    private boolean mExportToken;

    public static void start(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = findViewById(R.id.list_main_note_list);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);

        TextView emptyView = findViewById(R.id.tv_main_empty_view);
        mListView.setEmptyView(emptyView);
        emptyView.setOnLongClickListener(this);

        registerForContextMenu(mListView);

        UiUtils.createAppShortcut(MainActivity.this, R.string.create_note, android.R.drawable.ic_menu_add,
                new Intent(EditorActivity.SHORTCUT_ACTION_CREATE));

        Bundle args = AsyncLoader.getArgBundle(AsyncLoader.ActionType.FIND_ALL, 0, null, null);
        getLoaderManager().initLoader(LOADER_ID, args, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.item_main_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Bundle bundle = AsyncLoader.getArgBundle(AsyncLoader.ActionType.SEARCH, 0, null, newText);
                getLoaderManager().restartLoader(LOADER_ID, bundle, MainActivity.this);
                return false;
            }
        });

        /*
         * 在5.0或以上系统中，将搜索视图的图标换成内置的MD风格的搜索图标
         * 并且设置为蓝绿色
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Drawable materialSearchIcon = InternalRes.getDrawable(InternalRes.R.drawable.ic_menu_search_mtrl_alpha);
            materialSearchIcon.setTint(Color.CYAN);
            searchItem.setIcon(materialSearchIcon);
        }
        UiUtils.hideCloseButtonOnSearchView(searchView);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        mSelectedItemPosition = info.position;
        new MenuInflater(this).inflate(R.menu.list, menu);

        /* 菜单选项自定义外观 */
        MenuItem shareItem = menu.findItem(R.id.item_list_share_note);
        shareItem.setTitle(InternalRes.getString(InternalRes.R.string.share));
        MenuItem deleteItem = menu.findItem(R.id.item_list_delete_note);
        deleteItem.setTitle(InternalRes.getString(InternalRes.R.string.delete));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            /* 创建日志 */
            case R.id.item_main_create_note:
                EditorActivity.actionStart(this, EditorActivity.ActionType.CREATE, 0, null);
                finish();
                break;
            /* 弹出帮助信息 */
            case R.id.item_main_help:
                UiUtils.showMessageDialog(this, R.string.help, R.string.help_content);
                break;
            /* 弹出关于信息 */
            case R.id.item_main_about:
                UiUtils.showMessageDialog(this, R.string.about, R.string.about_content);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int positionForShow = mSelectedItemPosition + 1;
        final Note note = mNoteViewAdapter.getItem(mSelectedItemPosition);
        assert note != null;
        Bundle bundle;
        switch (item.getItemId()) {
            /* 分享记事 */
            case R.id.item_list_share_note:
                ContentUtils.share(this, note.getTitle(), note.getBody());
                break;
            /* 复制记事到尾部 */
            case R.id.item_list_copy_note:
                bundle = AsyncLoader.getArgBundle(AsyncLoader.ActionType.INSERT, 0, note, null);
                getLoaderManager().restartLoader(LOADER_ID, bundle, this);
                UiUtils.showToast(this, String.format(Locale.getDefault(), getString(R.string.copy_note_hint), positionForShow));
                break;
            /* 导出记事到文件 */
            case R.id.item_list_export_note:
                bundle = AsyncLoader.getArgBundle(AsyncLoader.ActionType.EXPORT, 0, note, null);
                mExportToken = true;
                getLoaderManager().restartLoader(LOADER_ID, bundle, this);
                break;
            /* 删除记事 */
            case R.id.item_list_delete_note:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.delete_confirm)
                        .setMessage(String.format(Locale.getDefault(), getString(R.string.delete_confirm_content), positionForShow))
                        .setCancelable(false)
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getLoaderManager().restartLoader(LOADER_ID, AsyncLoader.getArgBundle(AsyncLoader.ActionType.DELETE,
                                        note.getId(),
                                        null, null), MainActivity.this);
                            }
                        }).show();
                break;
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemClick: pos=" + position + " id=" + id);

        /* 进入到编辑器 */
        EditorActivity.actionStart(this, EditorActivity.ActionType.UPDATE, id, null);
        finish();
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, @NonNull Menu menu) {
        UiUtils.setOptionalIconsVisibleOnMenu(menu);
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemLongClick: pos=" + position + " id=" + id);
        /* 在7.0或以上，上下文菜单放置在被选择列表项旁边，在第一屏不再需要序号提示 */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || parent.getFirstVisiblePosition() > 0)
            UiUtils.showToast(this, String.format(getString(R.string.ctx_menu_hint), position + 1), true);
        return false;
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == R.id.tv_main_empty_view) {
            // 测试异常
            throw new RuntimeException("Test crash");
        }
        return true;
    }

    @Override
    public Loader<List<Note>> onCreateLoader(int id, Bundle args) {
        return new AsyncLoader(this, args);
    }

    @Override
    public void onLoadFinished(Loader<List<Note>> loader, List<Note> data) {
        if (mNoteViewAdapter == null) {
            mNoteViewAdapter = new NoteViewAdapter(this, R.layout.note_item, data);
            mListView.setAdapter(mNoteViewAdapter);
        } /* 导出：弹出对话框，不需要刷新列表 */ else if (mExportToken) {
            UiUtils.showMessageDialog(this, getString(R.string.file_saved_to), data.get(0).getBody());
            mExportToken = false;
        } else mNoteViewAdapter.reload(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Note>> loader) {
    }
}
