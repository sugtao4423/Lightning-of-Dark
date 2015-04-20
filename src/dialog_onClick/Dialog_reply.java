package dialog_onClick;

import com.tao.lightning_of_dark.ListViewListener;
import com.tao.lightning_of_dark.TweetActivity;

import twitter4j.Status;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class Dialog_reply implements OnClickListener {
	
	Status status;
	Context context;

	public Dialog_reply(Status status, Context context) {
		this.status = status;
		this.context = context;
	}

	@Override
	public void onClick(View v) {
		ListViewListener.dialog.dismiss();
		Intent reply = new Intent(context, TweetActivity.class);
		if(status.isRetweet()){
			reply.putExtra("ReplyUserScreenName", status.getRetweetedStatus().getUser().getScreenName());
			reply.putExtra("TweetReplyId", status.getRetweetedStatus().getId());
			reply.putExtra("ReplyTweetText", status.getRetweetedStatus().getText());
		}else{
			reply.putExtra("ReplyUserScreenName", status.getUser().getScreenName());
			reply.putExtra("TweetReplyId", status.getId());
			reply.putExtra("ReplyTweetText", status.getText());
		}
		context.startActivity(reply);
	}
}
