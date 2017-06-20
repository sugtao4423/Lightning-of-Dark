package sugtao4423.lod.dialog_onclick;

import twitter4j.Status;
import twitter4j.TwitterException;
import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import sugtao4423.lod.ApplicationClass;
import sugtao4423.lod.R;
import sugtao4423.lod.ShowToast;

public class Dialog_retweet implements OnClickListener{

	private Status status;
	private Context context;
	private AlertDialog dialog;

	public Dialog_retweet(Status status, Context context, AlertDialog dialog){
		this.status = status;
		this.context = context;
		this.dialog = dialog;
	}

	@Override
	public void onClick(View v){
		dialog.dismiss();
		new AsyncTask<Void, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Void... params){
				try{
					((ApplicationClass)context.getApplicationContext()).getTwitter().retweetStatus(status.getId());
					return true;
				}catch(TwitterException e){
					return false;
				}
			}

			@Override
			protected void onPostExecute(Boolean result){
				if(result)
					new ShowToast(R.string.success_retweet, context, 0);
				else
					new ShowToast(R.string.error_retweet, context, 0);
			}
		}.execute();
	}
}
