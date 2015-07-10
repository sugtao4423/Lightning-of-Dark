package dialog_onClick;

import com.tao.lightning_of_dark.ApplicationClass;
import com.tao.lightning_of_dark.TweetActivity;

import twitter4j.Status;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class Dialog_unOfficialRT implements OnClickListener {
	
	private Status status;
	private Context context;
	
	private boolean tweet_do_back;

	public Dialog_unOfficialRT(Status status, Context context, boolean tweet_do_back) {
		this.status = status;
		this.context = context;
		this.tweet_do_back = tweet_do_back;
	}

	@Override
	public void onClick(View v) {
		((ApplicationClass)context.getApplicationContext()).getDialog().dismiss();
		String RTtext;
		if(status.isRetweet())
			RTtext = " RT @" + status.getRetweetedStatus().getUser().getScreenName() + ": " + status.getRetweetedStatus().getText();
		else
			RTtext = " RT @" + status.getUser().getScreenName() + ": " + status.getText();
		Intent i = new Intent(context, TweetActivity.class);
		i.putExtra("pakuri", RTtext).putExtra("do_setSelection", false).putExtra("do_back", tweet_do_back);
		context.startActivity(i);
	}

}
