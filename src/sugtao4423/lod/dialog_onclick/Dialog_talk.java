package sugtao4423.lod.dialog_onclick;

import twitter4j.Status;
import twitter4j.TwitterException;
import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import sugtao4423.lod.ApplicationClass;
import sugtao4423.lod.ListViewListener;
import sugtao4423.lod.R;
import sugtao4423.lod.ShowToast;
import sugtao4423.lod.tweetlistview.TweetListAdapter;
import sugtao4423.lod.tweetlistview.TweetListView;

public class Dialog_talk implements OnClickListener{

	private Status status;
	private Context context;
	private twitter4j.Status reply;

	private TweetListAdapter resultAdapter;
	private AlertDialog dialog;

	public Dialog_talk(Status status, Context context, AlertDialog dialog){
		this.status = status;
		this.context = context;
		this.dialog = dialog;
	}

	@Override
	public void onClick(View v){
		dialog.dismiss();
		TweetListView result = new TweetListView(context);
		resultAdapter = new TweetListAdapter(context);
		resultAdapter.setOnItemClickListener(new ListViewListener());
		resultAdapter.setOnItemLongClickListener(new ListViewListener());
		result.setAdapter(resultAdapter);
		new AlertDialog.Builder(context).setView(result).show();

		if(status.isRetweet())
			reply = status.getRetweetedStatus();
		else
			reply = status;
		resultAdapter.add(reply);

		new LoadConversation().execute();
	}

	private class LoadConversation extends AsyncTask<Void, Void, Status>{
		@Override
		protected twitter4j.Status doInBackground(Void... params){
			try{
				reply = ((ApplicationClass)context.getApplicationContext()).getTwitter().showStatus(reply.getInReplyToStatusId());
				return reply;
			}catch(TwitterException e){
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
					new ShowToast(context.getApplicationContext(), R.string.success_getTalkList);
			}else{
				new ShowToast(context.getApplicationContext(), R.string.error_getTalkList);
			}
		}
	}
}