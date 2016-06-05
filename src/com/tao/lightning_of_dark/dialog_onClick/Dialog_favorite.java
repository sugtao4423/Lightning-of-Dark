package com.tao.lightning_of_dark.dialog_onClick;

import twitter4j.Status;
import twitter4j.TwitterException;

import com.tao.lightning_of_dark.ApplicationClass;
import com.tao.lightning_of_dark.ShowToast;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;

public class Dialog_favorite implements OnClickListener{

	private Status status;
	private Context context;
	private AlertDialog dialog;

	public Dialog_favorite(Status status, Context context, AlertDialog dialog){
		this.status = status;
		this.context = context;
		this.dialog = dialog;
	}

	@Override
	public void onClick(View v){
		dialog.dismiss();
		new AsyncTask<Void, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Void... params){
				try{
					((ApplicationClass)context.getApplicationContext()).getTwitter().createFavorite(status.getId());
					return true;
				}catch(TwitterException e){
					return false;
				}
			}

			@Override
			protected void onPostExecute(Boolean result){
				if(result)
					new ShowToast("ふぁぼりました", context, 0);
				else
					new ShowToast("ふぁぼれませんでした", context, 0);
			}
		}.execute();
	}
}
