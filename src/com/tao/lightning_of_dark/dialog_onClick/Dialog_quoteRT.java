package com.tao.lightning_of_dark.dialog_onClick;

import com.tao.lightning_of_dark.TweetActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnLongClickListener;
import twitter4j.Status;

public class Dialog_quoteRT implements OnLongClickListener{

	private Status status;
	private Context context;

	private AlertDialog dialog;

	public Dialog_quoteRT(Status status, Context context, AlertDialog dialog){
		this.status = status;
		this.context = context;
		this.dialog = dialog;
	}

	@Override
	public boolean onLongClick(View v){
		dialog.dismiss();
		Intent i = new Intent(context, TweetActivity.class);
		i.putExtra("type", TweetActivity.TYPE_QUOTERT);
		i.putExtra("status", new StatusItem(status));
		context.startActivity(i);
		return true;
	}
}
