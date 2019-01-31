package sugtao4423.lod.dialog_onclick;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;

import sugtao4423.lod.App;
import sugtao4423.lod.R;
import sugtao4423.lod.ShowToast;
import twitter4j.Status;
import twitter4j.TwitterException;

public class Dialog_deletePost implements OnClickListener{

    private Status status;
    private Context context;
    private AlertDialog dialog;

    public Dialog_deletePost(Status status, Context context, AlertDialog dialog){
        this.status = status;
        this.context = context;
        this.dialog = dialog;
    }

    @Override
    public void onClick(View v){
        dialog.dismiss();
        new AlertDialog.Builder(context)
                .setMessage("本当にツイ消ししますか？")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        deletePost();
                    }
                }).show();
    }

    public void deletePost(){
        new AsyncTask<Void, Void, Boolean>(){
            @Override
            protected Boolean doInBackground(Void... params){
                try{
                    ((App)context.getApplicationContext()).getTwitter().destroyStatus(status.getId());
                    return true;
                }catch(TwitterException e){
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result){
                if(result)
                    new ShowToast(context.getApplicationContext(), R.string.success_post_delete);
                else
                    new ShowToast(context.getApplicationContext(), R.string.error_post_delete);
            }
        }.execute();
    }

}
