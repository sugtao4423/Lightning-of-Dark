package com.tao.lightning_of_dark.dialog_onClick;

import twitter4j.Status;

import com.tao.lightning_of_dark.TweetActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class Dialog_unOfficialRT implements OnClickListener{

	private Status status;
	private Context context;

	private boolean tweet_do_back;
	private AlertDialog dialog;

	public Dialog_unOfficialRT(Status status, Context context, boolean tweet_do_back, AlertDialog dialog){
		if(status.isRetweet())
			this.status = status.getRetweetedStatus();
		else
			this.status = status;
		this.context = context;
		this.tweet_do_back = tweet_do_back;
		this.dialog = dialog;
	}

	@Override
	public void onClick(View v){
		dialog.dismiss();
		String text = " RT @" + status.getUser().getScreenName() + ": " + status.getText();
		Intent i = new Intent(context, TweetActivity.class);
		i.putExtra("pakuri", text).putExtra("do_setSelection", false).putExtra("do_back", tweet_do_back);
		context.startActivity(i);
	}
}
