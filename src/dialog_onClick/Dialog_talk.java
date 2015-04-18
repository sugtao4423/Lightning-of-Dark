package dialog_onClick;

import java.util.ArrayList;
import java.util.List;

import com.tao.lightning_of_dark.CustomAdapter;
import com.tao.lightning_of_dark.ListViewListener;
import com.tao.lightning_of_dark.MainActivity;
import com.tao.lightning_of_dark.ShowToast;

import twitter4j.Status;
import twitter4j.TwitterException;
import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

public class Dialog_talk implements OnClickListener {
	
	Status status;
	Context context;
	twitter4j.Status reply;

	public Dialog_talk(Status status, Context context) {
		this.status = status;
		this.context = context;
	}

	@Override
	public void onClick(View v) {
		ListViewListener.dialog.dismiss();
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		ListView result = new ListView(context);
		result.setOnItemClickListener(new ListViewListener());
		result.setOnItemLongClickListener(new ListViewListener());
		final CustomAdapter resultAdapter = new CustomAdapter(context);
		result.setAdapter(resultAdapter);
		builder.setView(result);
		builder.create().show();
		
		if(status.isRetweet())
			reply = status.getRetweetedStatus();
		else
			reply = status;
		final List<twitter4j.Status> StatusList = new ArrayList<twitter4j.Status>();
		
		AsyncTask<Void, Void, Boolean> getReply = new AsyncTask<Void, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Void... params) {
				try {
					for(; reply.getInReplyToStatusId() > 0;){
						reply = MainActivity.twitter.showStatus(reply.getInReplyToStatusId());
						StatusList.add(reply);
					}
					return true;
				} catch (TwitterException e) {
					return false;
				}
			}
			protected void onPostExecute(Boolean result) {
				if(result){
					if(status.isRetweet())
						resultAdapter.add(status.getRetweetedStatus());
					else
						resultAdapter.add(status);
					for(twitter4j.Status status : StatusList)
						resultAdapter.add(status);
				}else
					new ShowToast("会話取得エラー", context);
			}
		};
		getReply.execute();
	}
}
