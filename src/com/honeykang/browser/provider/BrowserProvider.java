/**
 * Teamdouche
 */
package com.honeykang.browser.provider;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.honeykang.browser.Constants;

/**
 * BrowserProvider.java
 * 
 * @author optedoblivion
 */
public class BrowserProvider extends ContentProvider {

    /**
     * Authority
     */
    public final static String AUTHORITY =
                              "com.honeykang.browser.provider.BrowserProvider";

    /**
     * Log Tag
     */
    private static final String TAG = "BrowserProvider";

    /**
     * URI Matcher
     */
    private static UriMatcher sUriMatcher = null;

    /**
     * Database name
     */
    private static final String DATABASE_NAME = "tdbrowser.db";

    /**
     * Database version
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Suggest Projection
     */
    private static final String[] SUGGEST_PROJECTION = new String[] {
        "_id", "url", "title"
    };

    /**
     * Suggest Selection
     */
    private static final String SUGGEST_SELECTION =
        "(url LIKE ? OR url LIKE ? OR url LIKE ? OR url LIKE ?"
            + " OR title LIKE ?)";// AND user_entered = 1";

    /**
     * Order By
     */
    private static final String ORDER_BY = "visits DESC, date DESC";

    /**
     * Suggest Args
     */
    private String[] SUGGEST_ARGS = new String[5];

    /**
     * History table
     */
    private static final String HISTORY_TABLE = "history";

    /**
     * Bookmark table
     */
    private static final String BOOKMARK_TABLE = "bookmark"; 

    /**
     * History URI
     */
    public static final Uri HISTORY_URI = Uri.parse("content://" + AUTHORITY 
                                                        + "/" + HISTORY_TABLE);

    /**
     * History ID
     */
    private static final int HISTORY_ID = 1;

    /**
     * Bookmark URI
     */
    public static final Uri BOOKMARK_URI = Uri.parse("content://" + AUTHORITY
                                                       + "/" + BOOKMARK_TABLE);

    /**
     * History Table Create SQL
     */
    private static final String CREATE_HISTORY_TABLE_SQL = " CREATE TABLE "
        + HISTORY_TABLE
        + " ( "
        + "_id INTEGER PRIMARY KEY autoincrement,"
        + "title TEXT,"
        + "url TEXT NOT NULL,"
        + "visits INTEGER,"
        + "date LONG," 
        + "created LONG,"
        + "description TEXT,"
        + "favicon BLOB DEFAULT NULL,"
        + "thumbnail BLOB DEFAULT NULL,"
        + "touch_icon BLOB DEFAULT NULL"
        + " ) ";

    /**
     * Bookmark Table Create SQL
     */
    private static final String CREATE_BOOKMARK_TABLE_SQL = " CREATE TABLE "
        + BOOKMARK_TABLE
        + " ( "
        + "_id INTEGER PRIMARY KEY autoincrement,"
        + "title TEXT,"
        + "url TEXT NOT NULL,"
        + "visits INTEGER,"
        + "date LONG," 
        + "created LONG,"
        + "description TEXT,"
        + "favicon BLOB DEFAULT NULL,"
        + "thumbnail BLOB DEFAULT NULL,"
        + "touch_icon BLOB DEFAULT NULL"
        + " ) ";

    /**
     * Bookmark ID
     */
    private static final int BOOKMARK_ID = 2;

    /**
     * URI suggest
     */
    private static final int URI_MATCH_SUGGEST = 3;

    /**
     * DatabaseHelper
     * 
     * @author Martin Brabham
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        /**
         * onCreate
         * 
         * @param db {@link android.database.sqlite.SQLiteDatabase}
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(CREATE_HISTORY_TABLE_SQL);
                db.execSQL(CREATE_BOOKMARK_TABLE_SQL);
                db.execSQL(
                        "INSERT INTO "
                        + HISTORY_TABLE
                        + " (title, url, visits, date, created, description "
                        + " ) VALUES "
                        + " ( 'CyanogenMod', 'http://www.cyanogenmod.com', "
                        + " 1, 0, 0, 'Android and Bacon')"
                );
            } catch (Exception e){
                Log.e(TAG, e.toString());
                if (Constants.DEBUG){
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldver, int newver) {
            Log.w(TAG, "Upgrading database from " + oldver + " to " + newver);
            try{
                db.execSQL("DROP TABLE IF EXISTS " + HISTORY_TABLE);
                db.execSQL("DROP TABLE IF EXISTS " + BOOKMARK_TABLE);
                onCreate(db);
            } catch (Exception e){
                Log.e(TAG, e.toString());
                if (Constants.DEBUG){
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Database Helper
     */
    private DatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {
        try{
            dbHelper = new DatabaseHelper(getContext());
            return true;
        } catch (Exception e){
            Log.e(TAG, e.toString());
            return false;
        }
    }

    @Override
    public int delete(Uri uri, String whereClause, String[] whereArgs) {
        int count = 0;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch(sUriMatcher.match(uri)){
            case HISTORY_ID:
                count = db.delete(HISTORY_TABLE, whereClause, whereArgs);
                break;
            case BOOKMARK_ID:
                count = db.delete(BOOKMARK_TABLE, whereClause, whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unkown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        String type = null;
        switch(sUriMatcher.match(uri)){
            case HISTORY_ID:
                type = "vnd.android.cursor.dir/vnd.honeykang.history";
                break;
            case BOOKMARK_ID:
                type = "vnd.android.cursor.dir/vnd.honeykang.bookmark";
                break;
            case URI_MATCH_SUGGEST:
                return SearchManager.SUGGEST_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unkown URI " + uri);
        }
        return type;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = 0;
        Uri returnUri = null;
        Uri tableUri = null;
        switch(sUriMatcher.match(uri)){
            case HISTORY_ID:
                String[] columns = {"_id", "visits"};
                String selection = "url = ?";
                String[] selectionArgs = {values.getAsString("url")};
                Cursor result = db.query(HISTORY_TABLE, columns, selection,
                                              selectionArgs, null, null, null);
                result.moveToFirst();
                if (result.getCount() > 0){
                    rowId = result.getLong(result.getColumnIndex("_id"));
                    ContentValues newVals = new ContentValues();
                    newVals.put("visits", result.getInt(
                                             result.getColumnIndex("visits")));
                    String[] whereArgs = {String.valueOf(rowId)};
                    db.update(HISTORY_TABLE, newVals, "_id = ?", whereArgs);
                } else {
                    rowId = db.insert(HISTORY_TABLE, null, values);
                }
                tableUri = HISTORY_URI;
                break;
            case BOOKMARK_ID:
                rowId = db.insert(BOOKMARK_TABLE, null, values);
                tableUri = BOOKMARK_URI;
                break;
            default:
                throw new IllegalArgumentException("Unkown URI " + uri);
        }
        if (rowId > 0){
            returnUri = ContentUris.withAppendedId(tableUri, rowId);
            getContext().getContentResolver().notifyChange(returnUri, null);
            return returnUri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                                    String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        switch(sUriMatcher.match(uri)){
            case HISTORY_ID:
                cursor = db.query(HISTORY_TABLE, projection, selection,
                                         selectionArgs, null, null, sortOrder);
                break;
            case BOOKMARK_ID:
                cursor = db.query(BOOKMARK_TABLE, projection, selection,
                                         selectionArgs, null, null, sortOrder);
                break;
            case URI_MATCH_SUGGEST:
                String suggestSelection;
                String [] myArgs;
                if (selectionArgs[0] == null || selectionArgs[0].equals("")) {
                    suggestSelection = null;
                    myArgs = null;
                    Log.i(TAG, "Selection is empty");
                } else {
                    String like = selectionArgs[0] + "%";
                    if (selectionArgs[0].startsWith("http")
                            || selectionArgs[0].startsWith("file")) {
                        myArgs = new String[1];
                        myArgs[0] = like;
                        suggestSelection = selection;
                        Log.i(TAG, "Selection is URL");
                    } else {
                        SUGGEST_ARGS[0] = "http://" + like;
                        SUGGEST_ARGS[1] = "http://www." + like;
                        SUGGEST_ARGS[2] = "https://" + like;
                        SUGGEST_ARGS[3] = "https://www." + like;
                        // To match against titles.
                        SUGGEST_ARGS[4] = like;
                        myArgs = SUGGEST_ARGS;
                        suggestSelection = SUGGEST_SELECTION;
                        Log.i(TAG, "Selection is keyword");
                    }
                }
                cursor = db.query(HISTORY_TABLE, SUGGEST_PROJECTION,
                        suggestSelection, myArgs, null, null, ORDER_BY);
                break;
            default:
                throw new IllegalArgumentException("Unkown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                                                      String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch(sUriMatcher.match(uri)){
            case HISTORY_ID:
                count = db.update(HISTORY_TABLE, values,
                                                     selection, selectionArgs);
                break;
            case BOOKMARK_ID:
                count = db.update(BOOKMARK_TABLE, values,
                                                     selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unkown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, HISTORY_TABLE, HISTORY_ID);
        sUriMatcher.addURI(AUTHORITY, BOOKMARK_TABLE, BOOKMARK_ID);
        sUriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY,
                                                            URI_MATCH_SUGGEST);
    }

}
