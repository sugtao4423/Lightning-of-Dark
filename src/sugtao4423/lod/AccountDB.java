package sugtao4423.lod;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AccountDB extends SQLiteOpenHelper{

	public AccountDB(Context context){
		// DataBase name is "Accounts"
		super(context, "Accounts", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db){
		// create table in "Accounts"
		db.execSQL(String.format(
				"CREATE TABLE accounts(%s TEXT, %s TEXT, %s TEXT,  %s TEXT, %s TEXT, %s INTEGER, %s INTEGER, %s TEXT, %s TEXT, %s TEXT)",
				Keys.SCREEN_NAME, Keys.CK, Keys.CS, Keys.ACCESS_TOKEN, Keys.ACCESS_TOKEN_SECRET,
				Keys.LIST_AS_TIMELINE, Keys.AUTO_LOAD_TL_INTERVAL,
				Keys.SELECT_LIST_IDS, Keys.SELECT_LIST_NAMES, Keys.APP_START_LOAD_LISTS));
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		// if SQL's version was changed, call
	}
}