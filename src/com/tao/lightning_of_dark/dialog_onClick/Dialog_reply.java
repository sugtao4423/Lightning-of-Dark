package com.tao.lightning_of_dark.dialog_onClick;

import twitter4j.Status;

import com.tao.lightning_of_dark.ApplicationClass;
import com.tao.lightning_of_dark.TweetActivity;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class Dialog_reply implements OnClickListener{

	private Status status;
	private Context context;

	private boolean tweet_do_back;

	public Dialog_reply(Status status, Context context, boolean tweet_do_back){
		this.status = status;
		this.context = context;
		this.tweet_do_back = tweet_do_back;
	}

	@Override
	public void onClick(View v){
		((ApplicationClass)context.getApplicationContext()).getListViewDialog().dismiss();
		Intent reply = new Intent(context, TweetActivity.class);
		if(status.isRetweet()) {
			reply.putExtra("ReplyUserScreenName", status.getRetweetedStatus().getUser().getScreenName());
			reply.putExtra("TweetReplyId", status.getRetweetedStatus().getId());
			reply.putExtra("ReplyTweetText", status.getRetweetedStatus().getText());
		}else{
			reply.putExtra("ReplyUserScreenName", status.getUser().getScreenName());
			reply.putExtra("TweetReplyId", status.getId());
			reply.putExtra("ReplyTweetText", status.getText());
		}
		reply.putExtra("do_back", tweet_do_back);
		context.startActivity(reply);
	}
}
