package dialog_onClick;

import com.tao.lightning_of_dark.ListViewListener;
import com.tao.lightning_of_dark.MainActivity;
import com.tao.lightning_of_dark.ShowToast;

import twitter4j.Status;
import twitter4j.TwitterException;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;

public class Dialog_deletePost implements OnClickListener {
	
	Status status;
	Context context;

	public Dialog_deletePost(Status status, Context context) {
		this.status = status;
		this.context = context;
	}

	@Override
	public void onClick(View v) {
		ListViewListener.dialog.dismiss();
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Void... params) {
				try {
					MainActivity.twitter.destroyStatus(status.getId());
					return true;
				} catch (TwitterException e) {
					return false;
				}
			}
			protected void onPostExecute(Boolean result) {
				if(result)
					new ShowToast("ツイ消ししました", context);
				else
					new ShowToast("ツイ消しできませんでした", context);
			}
		};
		task.execute();
	}

}
