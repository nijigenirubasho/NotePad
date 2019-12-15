package i.notepad.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import i.notepad.BuildConfig;

/**
 * 记事内容提供器
 *
 * @author 511721050589
 */

public class NoteProvider extends ContentProvider implements BaseColumns {

    public static final String TABLE_NAME = "note";
    public static final String COLUMN_NAME_TITLE = "title";
    public static final String COLUMN_NAME_BODY = "body";
    public static final String SCHEME = "content://";
    public static final String TABLE_TAG_FOR_CUSTOM = "custom";
    public static final String CONTENT_AUTHORITY = BuildConfig.APPLICATION_ID + ".NoteProvider";
    private static final String TAG = "NoteProvider";
    private static final UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(CONTENT_AUTHORITY, TABLE_NAME, UriMatchCode.NOTE_DIR);
        sUriMatcher.addURI(CONTENT_AUTHORITY, TABLE_NAME + "/#", UriMatchCode.NOTE_ITEM);
        sUriMatcher.addURI(CONTENT_AUTHORITY, TABLE_TAG_FOR_CUSTOM, UriMatchCode.CUSTOM);
    }

    private DatabaseHelper mDatabaseHelper;

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        Cursor cursor = null;
        switch (sUriMatcher.match(uri)) {
            case UriMatchCode.NOTE_DIR:
                cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
                break;
            case UriMatchCode.NOTE_ITEM:
                String id = uri.getPathSegments().get(1);
                final String findByIdSql = "SELECT * FROM " + TABLE_NAME + " WHERE " + _ID + "=?";
                cursor = db.rawQuery(findByIdSql, new String[]{id});
                break;
            case UriMatchCode.CUSTOM:
                cursor = db.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                Log.e(TAG, "query: !");
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        Uri ret = null;
        switch (sUriMatcher.match(uri)) {
            case UriMatchCode.NOTE_DIR:
            case UriMatchCode.NOTE_ITEM:
                long id = db.insert(TABLE_NAME, null, values);
                ret = Uri.parse(SCHEME + CONTENT_AUTHORITY + "/" + TABLE_NAME + "/" + id);
                break;
            default:
                Log.e(TAG, "insert: !");
        }
        return ret;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        int row = 0;
        switch (sUriMatcher.match(uri)) {
            case UriMatchCode.NOTE_DIR:
                Log.w(TAG, "delete: Will not be implemented.");
                break;
            case UriMatchCode.NOTE_ITEM:
                String id = uri.getPathSegments().get(1);
                row = db.delete(TABLE_NAME, _ID + "=?", new String[]{id});
                break;
            default:
                Log.e(TAG, "delete: !");
        }
        return row;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        int row = 0;
        switch (sUriMatcher.match(uri)) {
            case UriMatchCode.NOTE_DIR:
                Log.w(TAG, "update: Will not be implemented.");
                break;
            case UriMatchCode.NOTE_ITEM:
                String id = uri.getPathSegments().get(1);
                row = db.update(TABLE_NAME, values, _ID + "=?", new String[]{id});
                break;
            default:
                Log.e(TAG, "update: !");
                break;
        }
        return row;
    }

    private interface UriMatchCode {
        int
                NOTE_DIR = 1,
                NOTE_ITEM = 2,
                CUSTOM = 3;
    }

    private class DatabaseHelper extends SQLiteOpenHelper {

        static final String DB_NAME = "notes.db";
        static final int DB_VERSION = 1;

        DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // 建表语句
            final String createSql = "CREATE TABLE " + TABLE_NAME + " ("
                    + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_NAME_TITLE + " TEXT,"
                    + COLUMN_NAME_BODY + " TEXT"
                    + ")";
            db.execSQL(createSql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "onUpgrade: drop table. ver( " + oldVersion + " -> " + newVersion + " )");
            final String upgradeSql = "DROP TABLE IF EXISTS " + TABLE_NAME;
            db.execSQL(upgradeSql);
            onCreate(db);
        }
    }
}
