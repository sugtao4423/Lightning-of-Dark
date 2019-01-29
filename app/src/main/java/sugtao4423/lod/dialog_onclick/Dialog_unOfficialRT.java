package sugtao4423.lod.dialog_onclick;

import twitter4j.Status;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import sugtao4423.lod.TweetActivity;

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
		i.putExtra(TweetActivity.INTENT_EXTRA_KEY_TYPE, TweetActivity.TYPE_UNOFFICIALRT);
		i.putExtra(TweetActivity.INTENT_EXTRA_KEY_STATUS, status);
		context.startActivity(i);
	}
}
