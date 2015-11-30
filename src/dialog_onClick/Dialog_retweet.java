package dialog_onClick;

import com.tao.lightning_of_dark.ApplicationClass;
import com.tao.lightning_of_dark.ShowToast;

import twitter4j.Status;
import twitter4j.TwitterException;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;

public class Dialog_retweet implements OnClickListener{

	private Status status;
	private Context context;

	public Dialog_retweet(Status status, Context context){
		this.status = status;
		this.context = context;
	}

	@Override
	public void onClick(View v){
		((ApplicationClass)context.getApplicationContext()).getListViewDialog().dismiss();
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>(){
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
					new ShowToast("リツイートしました", context, 0);
				else
					new ShowToast("リツイートできませんでした", context, 0);
			}
		};
		task.execute();
	}
}
