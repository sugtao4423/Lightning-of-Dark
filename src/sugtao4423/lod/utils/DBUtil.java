package sugtao4423.lod.utils;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import sugtao4423.lod.AccountDB;
import sugtao4423.lod.Keys;
import sugtao4423.lod.dataclass.Account;

public class DBUtil{

	private SQLiteDatabase db;

	public DBUtil(Context context){
		db = new AccountDB(context).getWritableDatabase();
	}

	public Account getAccount(String screenName){
		Cursor c = db.rawQuery(String.format("SELECT * FROM accounts WHERE %s='%s'", Keys.SCREEN_NAME, screenName), null);
		if(!c.moveToFirst()){
			return null;
		}
		Account account = getAccount(c);
		c.close();
		return account;
	}

	public Account[] getAccounts(){
		Cursor c = db.rawQuery("SELECT * FROM accounts", null);
		ArrayList<Account> accountList = new ArrayList<Account>();

		boolean mov = c.moveToFirst();
		while(mov){
			accountList.add(getAccount(c));
			mov = c.moveToNext();
		}
		c.close();
		return (Account[])accountList.toArray(new Account[0]);
	}

	private Account getAccount(Cursor c){
		String screen_name = c.getString(0);
		String ck = c.getString(1);
		String cs = c.getString(2);
		String at = c.getString(3);
		String ats = c.getString(4);
		long listAsTL = c.getLong(5);
		int autoLoadTLInterval = c.getInt(6);
		String listIds = c.getString(7);
		String listNames = c.getString(8);
		String appLoadLists = c.getString(9);

		long[] selectListIds;
		if(listIds.equals("")){
			selectListIds = new long[0];
		}else{
			String[] strarr = listIds.split(",");
			selectListIds = new long[strarr.length];
			for(int i = 0; i < strarr.length; i++){
				selectListIds[i] = Long.parseLong(strarr[i]);
			}
		}
		
		String[] selectListNames = listNames.equals("") ? new String[0] : listNames.split(",");
		String[] startAppLoadLists = appLoadLists.equals("") ? new String[0] : appLoadLists.split(",");

		return new Account(screen_name, ck, cs, at, ats, listAsTL, autoLoadTLInterval, selectListIds, selectListNames, startAppLoadLists);
	}

	public boolean existsAccount(String screenName){
		Cursor c = db.rawQuery(String.format("SELECT COUNT(*) FROM accounts WHERE %s='%s'", Keys.SCREEN_NAME, screenName), null);
		c.moveToFirst();
		int count = c.getInt(0);
		c.close();
		return count > 0;
	}

	public void addAcount(Account account){
		db.execSQL(String.format("INSERT INTO accounts VALUES('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')",
				account.getScreenName(), account.getConsumerKey(), account.getConsumerSecret(),
				account.getAccessToken(), account.getAccessTokenSecret(), account.getListAsTL(), account.getAutoLoadTLInterval(),
				Utils.implode(account.getSelectListIds()),
				Utils.implode(account.getSelectListNames()),
				Utils.implode(account.getStartAppLoadLists())));
	}

	public void deleteAccount(Account account){
		db.execSQL(String.format("DELETE FROM accounts WHERE %s='%s'", Keys.SCREEN_NAME, account.getScreenName()));
	}

	public void updateListAsTL(long listAsTL, String screenName){
		db.execSQL(getUpdate1ColumnFromEq1Column(Keys.LIST_AS_TIMELINE, listAsTL, screenName));
	}

	public void updateAutoLoadTLInterval(int autoLoadTLInterval, String screenName){
		db.execSQL(getUpdate1ColumnFromEq1Column(Keys.AUTO_LOAD_TL_INTERVAL, autoLoadTLInterval, screenName));
	}

	public void updateSelectListIds(String ids, String screenName){
		db.execSQL(getUpdate1ColumnFromEq1Column(Keys.SELECT_LIST_IDS, ids, screenName));
	}

	public void updateSelectListNames(String names, String screenName){
		db.execSQL(getUpdate1ColumnFromEq1Column(Keys.SELECT_LIST_NAMES, names, screenName));
	}

	public void updateStartAppLoadLists(String startAppLoadLists, String screenName){
		db.execSQL(getUpdate1ColumnFromEq1Column(Keys.APP_START_LOAD_LISTS, startAppLoadLists, screenName));
	}

	private String getUpdate1ColumnFromEq1Column(Object updateKey, Object update, String screenName){
		return String.format("UPDATE accounts SET %s='%s' WHERE %s='%s'", updateKey, update, Keys.SCREEN_NAME, screenName);
	}

	public String[] getSelectListNames(String screenName){
		Cursor c = db.rawQuery(String.format("SELECT %s FROM accounts WHERE %s='%s'",
				Keys.SELECT_LIST_NAMES, Keys.SCREEN_NAME, screenName), null);
		c.moveToFirst();
		String[] result = c.getString(0).split(",");
		c.close();
		return result;
	}

	public String[] getNowStartAppLoadList(String screenName){
		Cursor c = db.rawQuery(String.format("SELECT %s FROM accounts WHERE %s='%s'",
				Keys.APP_START_LOAD_LISTS, Keys.SCREEN_NAME, screenName), null);
		c.moveToNext();
		String[] result = c.getString(0).split(",");
		c.close();
		return result;
	}
}
