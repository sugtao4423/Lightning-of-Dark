package com.tao.lightning_of_dark.dialog_onClick;

import twitter4j.Status;
import twitter4j.UserMentionEntity;

import java.util.ArrayList;

import com.tao.lightning_of_dark.ApplicationClass;
import com.tao.lightning_of_dark.TweetActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class Dialog_reply implements OnClickListener{

	private Status status;
	private Context context;

	private boolean tweet_do_back;
	private AlertDialog dialog;

	public Dialog_reply(Status status, Context context, boolean tweet_do_back, AlertDialog dialog){
		this.status = status;
		this.context = context;
		this.tweet_do_back = tweet_do_back;
		this.dialog = dialog;
	}

	@Override
	public void onClick(View v){
		dialog.dismiss();

		Status item;
		if(status.isRetweet())
			item = status.getRetweetedStatus();
		else
			item = status;

		if(item.getUserMentionEntities().length > 1)
			selectReplyDialog(item, ((ApplicationClass)context.getApplicationContext()).getMyScreenName());
		else
			reply(item);
	}

	public void selectReplyDialog(final Status item, final String myScreenName){
		new AlertDialog.Builder(context)
		.setItems(new String[]{"reply", "replyAll"}, new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which){
				switch(which){
				case 0:
					reply(item);
					break;
				case 1:
					replyAll(item, myScreenName);
					break;
				}
			}
		}).show();
	}

	public void reply(Status item){
		Intent reply = new Intent(context, TweetActivity.class);
		reply.putExtra("ReplyUserScreenName", item.getUser().getScreenName());
		reply.putExtra("TweetReplyId", item.getId());
		reply.putExtra("ReplyTweetText", item.getText());

		reply.putExtra("do_back", tweet_do_back);
		context.startActivity(reply);
	}

	public void replyAll(Status item, String myScreenName){
		ArrayList<String> mentionUsers = new ArrayList<String>();
		UserMentionEntity[] mentionEntitys = item.getUserMentionEntities();
		if(mentionEntitys != null && mentionEntitys.length > 0) {
			for(UserMentionEntity mention : mentionEntitys){
				if(!mention.getScreenName().equals(myScreenName))
					mentionUsers.add(mention.getScreenName());
			}
		}
		String replyUserScreenNames = item.getUser().getScreenName();
		for(String user : mentionUsers)
			replyUserScreenNames += " @" + user;
		Intent reply = new Intent(context, TweetActivity.class);
		reply.putExtra("ReplyUserScreenName", replyUserScreenNames);
		reply.putExtra("TweetReplyId", item.getId());
		reply.putExtra("ReplyTweetText", item.getText());

		reply.putExtra("do_back", tweet_do_back);
		context.startActivity(reply);
	}
}
