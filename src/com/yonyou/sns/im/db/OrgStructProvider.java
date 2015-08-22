package com.yonyou.sns.im.db;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.yonyou.sns.im.entity.YMOrgStruct;
import com.yonyou.sns.im.log.YMLogger;

public class OrgStructProvider extends YMDBTable {

	public static final String AUTHORITY = "com.yonyou.sns.im.provider.Struct";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/"
			+ IMSqLiteOpenHelper.TABLE_NAME_STRUCTS);

	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

	private static final int STRUCTS = 1;
	private static final int STRUCT_ID = 2;

	static {
		URI_MATCHER.addURI(AUTHORITY, IMSqLiteOpenHelper.TABLE_NAME_STRUCTS, STRUCTS);
		URI_MATCHER.addURI(AUTHORITY, IMSqLiteOpenHelper.TABLE_NAME_STRUCTS + "/#", STRUCT_ID);
	}

	private SQLiteOpenHelper mOpenHelper;
	
	public OrgStructProvider() {
		mOpenHelper=new IMSqLiteOpenHelper(getContext());
	}
	@Override
	public int delete(Uri url, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch (URI_MATCHER.match(url)) {

		case STRUCTS:
			count = db.delete(IMSqLiteOpenHelper.TABLE_NAME_STRUCTS, where, whereArgs);
			break;
		case STRUCT_ID:
			String segment = url.getPathSegments().get(1);

			if (TextUtils.isEmpty(where)) {
				where = "_id=" + segment;
			} else {
				where = "_id=" + segment + " AND (" + where + ")";
			}

			count = db.delete(IMSqLiteOpenHelper.TABLE_NAME_STRUCTS, where, whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Cannot delete from URL: " + url);
		}
		return count;
	}

	@Override
	public Uri insert(Uri url, ContentValues initialValues) {
		if (URI_MATCHER.match(url) != STRUCTS) {
			throw new IllegalArgumentException("Cannot insert into URL: " + url);
		}

		ContentValues values = (initialValues != null) ? new ContentValues(initialValues) : new ContentValues();

		for (String colName : YMOrgStruct.REQUIRED_COLUMNS) {
			if (values.containsKey(colName) == false) {
				throw new IllegalArgumentException("Missing column: " + colName);
			}
		}

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		long rowId = db.insert(IMSqLiteOpenHelper.TABLE_NAME_STRUCTS, null, values);

		if (rowId < 0) {
			throw new SQLException("Failed to insert row into " + url);
		}

		Uri noteUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
		return noteUri;
	}

	@Override
	public Cursor query(Uri url, String[] projectionIn, String selection, String[] selectionArgs, String sortOrder) {

		SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
		int match = URI_MATCHER.match(url);
		String groupBy = null;

		switch (match) {
		case STRUCTS:
			qBuilder.setTables(IMSqLiteOpenHelper.TABLE_NAME_STRUCTS);
			break;
		case STRUCT_ID:
			qBuilder.setTables(IMSqLiteOpenHelper.TABLE_NAME_STRUCTS);
			qBuilder.appendWhere("_id=");
			qBuilder.appendWhere(url.getPathSegments().get(1));
			break;
		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}

		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = YMOrgStruct.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}

		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor ret = qBuilder.query(db, projectionIn, selection, selectionArgs, groupBy, null, orderBy);

		if (ret == null) {
			YMLogger.i("ChatProvider.query: failed");
		} else {
			ret.setNotificationUri(getContext().getContentResolver(), url);
		}

		return ret;
	}

	@Override
	public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
		int count;
		long rowId = 0;
		int match = URI_MATCHER.match(url);
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		switch (match) {
		case STRUCTS:
			count = db.update(IMSqLiteOpenHelper.TABLE_NAME_STRUCTS, values, where, whereArgs);
			break;
		case STRUCT_ID:
			String segment = url.getPathSegments().get(1);
			rowId = Long.parseLong(segment);
			count = db.update(IMSqLiteOpenHelper.TABLE_NAME_STRUCTS, values, "_id=" + rowId, null);
			break;
		default:
			throw new UnsupportedOperationException("Cannot update URL: " + url);
		}

		return count;

	}

}
