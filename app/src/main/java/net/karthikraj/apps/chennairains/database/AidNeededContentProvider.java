package net.karthikraj.apps.chennairains.database;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by Karthikraj Duraisamy on 4/12/15.
 */
public class AidNeededContentProvider extends ContentProvider {

    // database
    private RestClientDBHelper database;

    // used for the UriMacher
    private static final int URI_MATCHER = 1;
    private static final int URI_MATCHER_ID = 2;

    private static final String AUTHORITY = "net.karthikraj.apps.chennairains.database.aidneededcontentprovider";

    private static final String BASE_PATH_AIDNEEDED = "aidneeded";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + BASE_PATH_AIDNEEDED);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/aidneeded";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/aidneededtable";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_AIDNEEDED, URI_MATCHER);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_AIDNEEDED + "/#", URI_MATCHER_ID);
    }

    @Override
    public boolean onCreate() {
        database = new RestClientDBHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // Uisng SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // check if the caller has requested a column which does not exists
        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(AidNeededTable.AIDNEED_TABLE);


        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case URI_MATCHER:
                break;
            case URI_MATCHER_ID:
                // adding the ID to the original query
                queryBuilder.appendWhere(AidNeededTable.COLUMN_ID + "="
                        + uri.getLastPathSegment());

                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {


        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        long id = 0;
        switch (uriType) {
            case URI_MATCHER:
                // Zero count means empty table.
                id = sqlDB.insert(AidNeededTable.AIDNEED_TABLE, null, values);

                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH_AIDNEEDED + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType) {
            case URI_MATCHER:
                rowsDeleted = sqlDB.delete(AidNeededTable.AIDNEED_TABLE, selection,
                        selectionArgs);
                break;
            case URI_MATCHER_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(AidNeededTable.AIDNEED_TABLE,
                            AidNeededTable.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(AidNeededTable.AIDNEED_TABLE,
                            AidNeededTable.COLUMN_ID + "=" + id
                                    + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
            case URI_MATCHER:
                rowsUpdated = sqlDB.update(AidNeededTable.AIDNEED_TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case URI_MATCHER_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(AidNeededTable.AIDNEED_TABLE,
                            values,
                            AidNeededTable.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(AidNeededTable.AIDNEED_TABLE,
                            values,
                            AidNeededTable.COLUMN_ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        String[] available = {
                AidNeededTable.COLUMN_ID, AidNeededTable.AIDNEED_AREA, AidNeededTable.AIDNEED_CONT_NUMBER, AidNeededTable.AIDNEED_NAME,
                AidNeededTable.AIDNEED_ORIGINAL_SOURCE, AidNeededTable.AIDNEED_OTHERS, AidNeededTable.AIDNEED_TIMESTAMP, AidNeededTable.AIDNEED_WHATKIND};
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }
}