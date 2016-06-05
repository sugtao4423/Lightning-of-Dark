package com.tao.lightning_of_dark.dialog_onClick;

import twitter4j.Status;
import twitter4j.TwitterException;

import com.tao.lightning_of_dark.ApplicationClass;
import com.tao.lightning_of_dark.ShowToast;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;

public class Dialog_deletePost implements OnClickListener{

	private Status status;
	private Context context;
	private AlertDialog dialog;

	public Dialog_deletePost(Status status, Context context, AlertDialog dialog){
		this.status = status;
		this.context = context;
		this.dialog = dialog;
	}

	@Override
	public void onClick(View v){
		dialog.dismiss();
		new AlertDialog.Builder(context)
		.setMessage("本当にツイ消ししますか？")
		.setNegativeButton("No", null)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				deletePost();
			}
		}).show();
	}

	public void deletePost(){
		new AsyncTask<Void, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Void... params){
				try{
					((ApplicationClass)context.getApplicationContext()).getTwitter().destroyStatus(status.getId());
					return true;
				}catch(TwitterException e){
					return false;
				}
			}

			@Override
			protected void onPostExecute(Boolean result){
				if(result)
					new ShowToast("ツイ消ししました", context, 0);
				else
					new ShowToast("ツイ消しできませんでした", context, 0);
			}
		}.execute();
	}
}
