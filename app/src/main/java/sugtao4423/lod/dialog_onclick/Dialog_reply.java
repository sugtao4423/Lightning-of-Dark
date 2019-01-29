package sugtao4423.lod.dialog_onclick;

import twitter4j.Status;
import twitter4j.UserMentionEntity;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import sugtao4423.icondialog.IconDialog;
import sugtao4423.icondialog.IconItem;
import sugtao4423.lod.App;
import sugtao4423.lod.R;
import sugtao4423.lod.TweetActivity;

public class Dialog_reply implements OnClickListener{

	private Status status;
	private Context context;
	private String myScreenName;

	private AlertDialog dialog;

	public Dialog_reply(Status status, Context context, AlertDialog dialog){
		this.status = status;
		this.context = context;
		this.dialog = dialog;
		this.myScreenName = ((App)context.getApplicationContext()).getCurrentAccount().getScreenName();
	}

	@Override
	public void onClick(View v){
		dialog.dismiss();

		Status item = status.isRetweet() ? status.getRetweetedStatus() : status;

		ArrayList<String> mentions = new ArrayList<String>();
		mentions.add(item.getUser().getScreenName());
		for(UserMentionEntity entity : item.getUserMentionEntities()){
			if(!entity.getScreenName().equals(myScreenName) && mentions.indexOf(entity.getScreenName()) == -1)
				mentions.add(entity.getScreenName());
		}

		if(mentions.size() > 1)
			selectReplyDialog(item);
		else
			reply(item);
	}

	public void selectReplyDialog(final Status item){
		int black = Color.parseColor(context.getString(R.color.icon));
		IconItem[] items = new IconItem[2];
		items[0] = new IconItem(context.getString(R.string.icon_reply).charAt(0), black, "reply");
		items[1] = new IconItem(context.getString(R.string.icon_replyAll).charAt(0), black, "replyAll");
		new IconDialog(context).setItems(items, new DialogInterface.OnClickListener(){
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
		reply.putExtra(TweetActivity.INTENT_EXTRA_KEY_TYPE, TweetActivity.TYPE_REPLY);
		reply.putExtra(TweetActivity.INTENT_EXTRA_KEY_STATUS, status);
		context.startActivity(reply);
	}

	public void replyAll(Status item){
		Intent reply = new Intent(context, TweetActivity.class);
		reply.putExtra(TweetActivity.INTENT_EXTRA_KEY_TYPE, TweetActivity.TYPE_REPLYALL);
		reply.putExtra(TweetActivity.INTENT_EXTRA_KEY_STATUS, status);
		context.startActivity(reply);
	}
}
