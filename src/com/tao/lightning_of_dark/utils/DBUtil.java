package com.tao.lightning_of_dark.utils;

import java.util.ArrayList;

import com.tao.lightning_of_dark.SQLHelper;
import com.tao.lightning_of_dark.dataclass.Account;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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
		db.execSQL("insert into accounts values('" +
						account.getScreenName() + "', '" +
						account.getCK() + "', '" +
						account.getCS() + "', '" +
						account.getAT() + "', '" +
						account.getATS() + "', '" +
						String.valueOf(account.getShowList()) + "', '" +
						String.valueOf(account.getSelectListCount()) + "', '" +
						account.getSelectListIds() + "', '" +
						account.getSelectListNames() + "', '" +
						account.getStartAppLoadLists() + "')");
	}

	public void deleteAccount(Account account){
		db.execSQL("delete from accounts where screen_name='" + account.getScreenName() + "' and CK='" +
						account.getCK() + "' and CS='" +
						account.getCS() + "' and AT='" +
						account.getAT() + "' and ATS='" +
						account.getATS() + "' and showList='" +
						String.valueOf(account.getShowList()) + "' and SelectListCount='" +
						String.valueOf(account.getSelectListCount()) + "' and SelectListIds='" +
						account.getSelectListIds() + "' and SelectListNames='" +
						account.getSelectListNames() + "' and startApp_loadLists='" +
						account.getStartAppLoadLists() + "'");
	}
}
