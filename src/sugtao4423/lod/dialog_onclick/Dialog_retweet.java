package sugtao4423.lod.dialog_onclick;

import twitter4j.Status;
import twitter4j.TwitterException;
import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import sugtao4423.lod.App;
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
					((App)context.getApplicationContext()).getTwitter().retweetStatus(status.getId());
					return true;
				}catch(TwitterException e){
					return false;
				}
			}

			@Override
			protected void onPostExecute(Boolean result){
				if(result)
					new ShowToast(context.getApplicationContext(), R.string.success_retweet);
				else
					new ShowToast(context.getApplicationContext(), R.string.error_retweet);
			}
		}.execute();
	}
}
