package sugtao4423.lod.usetime;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UseTimeDB extends SQLiteOpenHelper{

	public UseTimeDB(Context context){
		super(context, "UseTime", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db){
		db.execSQL("CREATE TABLE useTime(date INTEGER, use INTEGER)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
	}

}
