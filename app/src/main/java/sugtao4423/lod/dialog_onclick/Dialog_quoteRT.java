package sugtao4423.lod.dialog_onclick;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnLongClickListener;

import sugtao4423.lod.TweetActivity;
import twitter4j.Status;

public class Dialog_quoteRT implements OnLongClickListener{

    private Status status;
    private Context context;

    private AlertDialog dialog;

    public Dialog_quoteRT(Status status, Context context, AlertDialog dialog){
        this.status = status;
        this.context = context;
        this.dialog = dialog;
    }

    @Override
    public boolean onLongClick(View v){
        dialog.dismiss();
        Intent i = new Intent(context, TweetActivity.class);
        i.putExtra(TweetActivity.INTENT_EXTRA_KEY_TYPE, TweetActivity.TYPE_QUOTERT);
        i.putExtra(TweetActivity.INTENT_EXTRA_KEY_STATUS, status);
        context.startActivity(i);
        return true;
    }

}
