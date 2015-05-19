package dialog_onClick;

import com.tao.lightning_of_dark.ApplicationClass;
import com.tao.lightning_of_dark.ShowToast;

import twitter4j.Status;
import twitter4j.TwitterException;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;

public class Dialog_deletePost implements OnClickListener {
	
	private Status status;
	private Context context;

	public Dialog_deletePost(Status status, Context context) {
		this.status = status;
		this.context = context;
	}

	@Override
	public void onClick(View v) {
		((ApplicationClass)context.getApplicationContext()).getDialog().dismiss();
		AlertDialog.Builder builder = new AlertDialog.Builder(context)
		.setMessage("本当にツイ消ししますか？")
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				deletePost();
			}
		});
		builder.setNegativeButton("No", null).create().show();
	}
	
	public void deletePost(){
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Void... params) {
				try {
					((ApplicationClass)context.getApplicationContext()).getTwitter().destroyStatus(status.getId());
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
