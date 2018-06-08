package sugtao4423.lod.usetime;

import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

public class UseTime{

	private SQLiteDatabase db;
	private long startTime;

	public UseTime(Context context){
		db = new UseTimeDB(context).getWritableDatabase();
	}

	public void start(){
		startTime = System.currentTimeMillis();
	}

	public void stop(){
		final long useTime = System.currentTimeMillis() - startTime;
		new AsyncTask<Void, Void, Void>(){
			@Override
			protected Void doInBackground(Void... params){
				save(useTime);
				return null;
			}
		}.execute();
	}

	private long todayUnixTime(){
		Calendar today = Calendar.getInstance();
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
		return cal.getTimeInMillis();
	}

	private boolean existsTodayRow(){
		String query = "SELECT COUNT(*) FROM useTime WHERE date = " + todayUnixTime();
		Cursor c = db.rawQuery(query, null);
		c.moveToFirst();
		int count = c.getInt(0);
		c.close();
		return count > 0;
	}

	private void save(long useTime){
		if(existsTodayRow()){
			updateUseTime(useTime);
		}else{
			insertUseTime(useTime);
		}
	}

	private void insertUseTime(long useTime){
		String sql = "INSERT INTO useTime VALUES (" + todayUnixTime() + "," + useTime + ")";
		db.execSQL(sql);
	}

	private void updateUseTime(long useTime){
		long todayDate = todayUnixTime();
		String query = "SELECT use FROM useTime WHERE date = " + todayDate;
		Cursor c = db.rawQuery(query, null);
		c.moveToFirst();
		long oldUseTime = c.getLong(0);
		useTime += oldUseTime;
		String update = "UPDATE useTime SET use = " + useTime + " WHERE date = " + todayDate;
		db.execSQL(update);
	}

}
