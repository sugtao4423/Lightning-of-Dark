package dialog_onClick;

import com.tao.lightning_of_dark.ApplicationClass;
import com.tao.lightning_of_dark.CustomAdapter;
import com.tao.lightning_of_dark.ListViewListener;
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
	
	private Status status;
	private Context context;
	private twitter4j.Status reply;
	
	private CustomAdapter resultAdapter;

	public Dialog_talk(Status status, Context context) {
		this.status = status;
		this.context = context;
	}

	@Override
	public void onClick(View v) {
		((ApplicationClass)context.getApplicationContext()).getDialog().dismiss();
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		ListView result = new ListView(context);
		result.setOnItemClickListener(new ListViewListener());
		result.setOnItemLongClickListener(new ListViewListener());
		resultAdapter = new CustomAdapter(context);
		result.setAdapter(resultAdapter);
		builder.setView(result);
		builder.create().show();
		
		if(status.isRetweet())
			reply = status.getRetweetedStatus();
		else
			reply = status;
		resultAdapter.add(reply);
		
		new LoadConversation().execute();
	}
	private class LoadConversation extends AsyncTask<Void, Void, Status> {
		@Override
		protected twitter4j.Status doInBackground(Void... params) {
			try {
				reply = ((ApplicationClass)context.getApplicationContext()).getTwitter().showStatus(reply.getInReplyToStatusId());
				return reply;
			} catch (TwitterException e) {
				return null;
			}
		}
		@Override
		public void onPostExecute(twitter4j.Status result){
			if(result != null){
				resultAdapter.add(result);
				if(result.getInReplyToStatusId() > 0)
					new LoadConversation().execute();
				else
					new ShowToast("会話取得完了", context);
			}else
				new ShowToast("会話取得エラー", context);
		}
	}
}