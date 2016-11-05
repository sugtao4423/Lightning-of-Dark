package com.tao.lightning_of_dark.dialog_onClick;

import twitter4j.Status;
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

	private AlertDialog dialog;

	public Dialog_reply(Status status, Context context, AlertDialog dialog){
		this.status = status;
		this.context = context;
		this.dialog = dialog;
	}

	@Override
	public void onClick(View v){
		dialog.dismiss();

		Status item = status.isRetweet() ? status.getRetweetedStatus() : status;

		if(item.getUserMentionEntities().length > 1)
			selectReplyDialog(item);
		else
			reply(item);
	}

	public void selectReplyDialog(final Status item){
		new AlertDialog.Builder(context)
		.setItems(new String[]{"reply", "replyAll"}, new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which){
				switch(which){
				case 0:
					reply(item);
					break;
				case 1:
					replyAll(item);
					break;
				}
			}
		}).show();
	}

	public void reply(Status item){
		Intent reply = new Intent(context, TweetActivity.class);
		reply.putExtra("type", TweetActivity.TYPE_REPLY);
		reply.putExtra("status", new StatusItem(status));
		context.startActivity(reply);
	}

	public void replyAll(Status item){
		Intent reply = new Intent(context, TweetActivity.class);
		reply.putExtra("type", TweetActivity.TYPE_REPLYALL);
		reply.putExtra("status", new StatusItem(status));
		context.startActivity(reply);
	}
}
