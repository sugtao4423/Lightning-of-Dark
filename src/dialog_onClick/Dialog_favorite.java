package dialog_onClick;

import com.tao.lightning_of_dark.ApplicationClass;
import com.tao.lightning_of_dark.ShowToast;

import twitter4j.Status;
import twitter4j.TwitterException;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;

public class Dialog_favorite implements OnClickListener {

	private Status status;
	private Context context;
	
	public Dialog_favorite(Status status, Context context) {
		this.status = status;
		this.context = context;
	}

	@Override
	public void onClick(View v) {
		((ApplicationClass)context.getApplicationContext()).getDialog().dismiss();
		AsyncTask<Void, Void, Boolean> fav = new AsyncTask<Void, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Void... params) {
				try {
					((ApplicationClass)context.getApplicationContext()).getTwitter().createFavorite(status.getId());
					return true;
				} catch (TwitterException e) {
					return false;
				}
			}
			protected void onPostExecute(Boolean result) {
				if(result)
					new ShowToast("ふぁぼりました", context);
				else
					new ShowToast("ふぁぼれませんでした", context);
			}
		};
		fav.execute();
	}
}
