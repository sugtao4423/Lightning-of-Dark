package com.tao.lightning_of_dark;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLHelper extends SQLiteOpenHelper{

	public SQLHelper(Context context){
		// DataBase name is "Accounts"
		super(context, "Accounts", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db){
		// create table in "Accounts"
		db.execSQL("create table accounts(screen_name text, CK text, CS text, AT text, ATS text, "
				+ "showList text, SelectListCount text, SelectListIds text, SelectListNames text, startApp_loadLists text)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		// if SQL's version was changed, call
	}
}