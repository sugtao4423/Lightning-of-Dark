package dialog_onClick;

import com.tao.lightning_of_dark.ListViewListener;
import com.tao.lightning_of_dark.TweetActivity;

import twitter4j.Status;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class Dialog_unOfficialRT implements OnClickListener {
	
	Status status;
	Context context;

	public Dialog_unOfficialRT(Status status, Context context) {
		this.status = status;
		this.context = context;
	}

	@Override
	public void onClick(View v) {
		ListViewListener.dialog.dismiss();
		String RTtext;
		if(status.isRetweet())
			RTtext = " RT @" + status.getRetweetedStatus().getUser().getScreenName() + ": " + status.getRetweetedStatus().getText();
		else
			RTtext = " RT @" + status.getUser().getScreenName() + ": " + status.getText();
		Intent i = new Intent(context, TweetActivity.class);
		i.putExtra("pakuri", RTtext).putExtra("do_setSelection", false);
		context.startActivity(i);
	}

}
