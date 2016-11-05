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

	private AlertDialog dialog;

	public Dialog_unOfficialRT(Status status, Context context, AlertDialog dialog){
		this.status = status;
		this.context = context;
		this.dialog = dialog;
	}

	@Override
	public void onClick(View v){
		dialog.dismiss();
		Intent i = new Intent(context, TweetActivity.class);
		i.putExtra("type", TweetActivity.TYPE_UNOFFICIALRT);
		i.putExtra("status", new StatusItem(status));
		context.startActivity(i);
	}
}
