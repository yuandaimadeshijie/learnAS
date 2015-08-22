package com.yonyou.sns.im.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.yonyou.sns.im.entity.YMOrgStruct;

public class IMSqLiteOpenHelper extends SQLiteOpenHelper {

	/** 表名-组织结构表 */
	public static final String TABLE_NAME_STRUCTS = "structs";

	/** 数据库名 */
	protected static final String DATABASE_NAME = "ym_structs.db";

	/** 数据库版本 */
	private static final int DATABASE_VERSION_DEV = 1;
	/** version add user_jid in structs */
	private static final int DATABASE_VERSION_141210 = 2;

	public IMSqLiteOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION_141210);
	}

	/** 建表-组织结构表 */
	private static final String TABLE_CREATE_STRUCTS = "CREATE TABLE " + TABLE_NAME_STRUCTS + " (" + YMOrgStruct._ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT," + YMOrgStruct.USER_JID + " TEXT," + YMOrgStruct.JID + " TEXT,"
			+ YMOrgStruct.NAME + " TEXT," + YMOrgStruct.IS_USER + " TEXT," + YMOrgStruct.PID + " TEXT,"
			+ YMOrgStruct.IS_LEAF + " TEXT);";
	@Override
	public void onCreate(SQLiteDatabase db) {
		// 组织结构表
		db.execSQL(TABLE_CREATE_STRUCTS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch (oldVersion) {
		case DATABASE_VERSION_DEV:
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_STRUCTS);
			onCreate(db);
			break;
		default:
			break;
		}
	}

}
