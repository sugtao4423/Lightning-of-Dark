package com.tao.lightning_of_dark.dialog_onClick;

import com.tao.lightning_of_dark.ApplicationClass;
import com.tao.lightning_of_dark.TweetActivity;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnLongClickListener;
import twitter4j.Status;

public class Dialog_quoteRT implements OnLongClickListener{

	private Status status;
	private Context context;

	private boolean tweet_do_back;

	public Dialog_quoteRT(Status status, Context context, boolean tweet_do_back){
		if(status.isRetweet())
			this.status = status.getRetweetedStatus();
		else
			this.status = status;
		this.context = context;
		this.tweet_do_back = tweet_do_back;
	}

	@Override
	public boolean onLongClick(View v){
		((ApplicationClass)context.getApplicationContext()).getListViewDialog().dismiss();
		String text = " https://twitter.com/" + status.getUser().getScreenName() + "/status/" + String.valueOf(status.getId());
		Intent i = new Intent(context, TweetActivity.class);
		i.putExtra("pakuri", text).putExtra("do_setSelection", false).putExtra("do_back", tweet_do_back);
		context.startActivity(i);
		return true;
	}
}
