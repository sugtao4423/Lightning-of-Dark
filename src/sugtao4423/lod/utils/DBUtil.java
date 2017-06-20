package sugtao4423.lod.utils;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import sugtao4423.lod.Keys;
import sugtao4423.lod.SQLHelper;
import sugtao4423.lod.dataclass.Account;

public class DBUtil{

	private SQLiteDatabase db;

	public DBUtil(Context context){
		db = new SQLHelper(context).getWritableDatabase();
	}

	public Account[] getAccounts(){
		Cursor c = db.rawQuery("select * from accounts", null);
		ArrayList<Account> accountList = new ArrayList<Account>();

		boolean mov = c.moveToFirst();
		while(mov){
			String screen_name = c.getString(0);
			String ck = c.getString(1);
			String cs = c.getString(2);
			String at = c.getString(3);
			String ats = c.getString(4);
			boolean showList = Boolean.parseBoolean(c.getString(5));
			int selectListCount = c.getInt(6);
			String selectListIds = c.getString(7);
			String selectListNames = c.getString(8);
			String startAppLoadLists = c.getString(9);

			Account account = new Account(screen_name, ck, cs, at, ats, showList, selectListCount, selectListIds, selectListNames, startAppLoadLists);
			accountList.add(account);

			mov = c.moveToNext();
		}

		return (Account[])accountList.toArray(new Account[0]);
	}

	public void addAcount(Account account){
		db.execSQL(String.format("insert into accounts values('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')",
				account.getScreenName(), account.getCK(), account.getCS(), account.getAT(), account.getATS(),
				account.getShowList(), account.getSelectListCount(),
				account.getSelectListIds(), account.getSelectListNames(), account.getStartAppLoadLists()));
	}

	public void deleteAccount(Account account){
		db.execSQL(String.format(
				"delete from accounts where %s='%s' and %s='%s' and %s='%s' and %s='%s' and %s='%s' "
				+ "and %s='%s' and %s='%s' and %s='%s' and %s='%s' and %s='%s'",
				Keys.SCREEN_NAME, account.getScreenName(), Keys.CK, account.getCK(), Keys.CS, account.getCS(),
				Keys.ACCESS_TOKEN, account.getAT(), Keys.ACCESS_TOKEN_SECRET, account.getATS(),
				Keys.SHOW_LIST, account.getShowList(), Keys.SELECT_LIST_COUNT, account.getSelectListCount(),
				Keys.SELECT_LIST_IDS, account.getSelectListIds(), Keys.SELECT_LIST_NAMES, account.getSelectListNames(),
				Keys.APP_START_LOAD_LISTS, account.getStartAppLoadLists()));
	}

	public void updateShowList(boolean showList, String screenName){
		db.execSQL(getUpdate1ColumnFromEq1Column(Keys.SHOW_LIST, showList, Keys.SCREEN_NAME, screenName));
	}

	public void updateStartAppLoadLists(String startAppLoadLists, String screenName){
		db.execSQL(getUpdate1ColumnFromEq1Column(Keys.APP_START_LOAD_LISTS, startAppLoadLists, Keys.SCREEN_NAME, screenName));
	}

	public void updateSelectListCount(int count, String screenName){
		db.execSQL(getUpdate1ColumnFromEq1Column(Keys.SELECT_LIST_COUNT, count, Keys.SCREEN_NAME, screenName));
	}

	public void updateSelectListIds(String ids, String screenName){
		db.execSQL(getUpdate1ColumnFromEq1Column(Keys.SELECT_LIST_IDS, ids, Keys.SCREEN_NAME, screenName));
	}

	public void updateSelectListNames(String names, String screenName){
		db.execSQL(getUpdate1ColumnFromEq1Column(Keys.SELECT_LIST_NAMES, names, Keys.SCREEN_NAME, screenName));
	}

	private String getUpdate1ColumnFromEq1Column(Object updateKey, Object update, Object whereKey, Object where){
		return String.format("update accounts set %s='%s' where %s='%s'", updateKey, update, whereKey, where);
	}

	public String[] getSelectListNames(String screenName){
		Cursor c = db.rawQuery(String.format("select %s from accounts where %s='%s'",
				Keys.SELECT_LIST_NAMES, Keys.SCREEN_NAME, screenName), null);
		String c_str = null;
		while(c.moveToNext())
			c_str = c.getString(0);
		c.close();
		return c_str.split(",", 0);
	}

	public String[] getNowStartAppLoadList(String screenName){
		Cursor c = db.rawQuery(String.format("select %s from accounts where %s='%s'",
				Keys.APP_START_LOAD_LISTS, Keys.SCREEN_NAME, screenName), null);
		c.moveToNext();
		String[] result = c.getString(0).split(",", 0);
		c.close();
		return result;
	}
}
