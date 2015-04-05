package com.tao.lightning_of_dark;

import java.io.File;

import twitter4j.StatusUpdate;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class TweetActivity extends Activity {
	
	static EditText TweetText;
	static TextView moji140;
	static long TweetReplyId;
	static String ReplyUserScreenName, ReplyTweetText, pakuri;
	static File image;
	static boolean do_back, do_setSelection;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tweet_activity);
		
		getActionBar().setDisplayShowHomeEnabled(false);
		
		TweetText = (EditText)findViewById(R.id.tweetText);
		moji140 = (TextView)findViewById(R.id.moji140);
		
		ReplyTweetText = getIntent().getStringExtra("ReplyTweetText");
		ReplyUserScreenName = getIntent().getStringExtra("ReplyUserScreenName");
		TweetReplyId = getIntent().getLongExtra("TweetReplyId", -1);
		pakuri = getIntent().getStringExtra("pakuri");
		do_back = getIntent().getBooleanExtra("do_back", true);
		do_setSelection = getIntent().getBooleanExtra("do_setSelection", true);
		
		if(TweetReplyId == -1){
			getActionBar().setTitle("New Tweet");
			TweetText.setText(pakuri);
		}else{
			getActionBar().setTitle(ReplyUserScreenName + " : " + ReplyTweetText);
			if(ReplyTweetText.length() > 10)
				ReplyTweetText = ReplyTweetText.substring(0, 10);
			TweetText.setText("@" + ReplyUserScreenName + " ");
		}
		if(do_setSelection)
			TweetText.setSelection(TweetText.getText().length());
		
		moji140.setText(String.valueOf(140 - TweetText.getText().length()));
		moji140count();
	}
	
	public void tweet(View v){
		final ImageButton tweetbtn, back, img;
		tweetbtn = (ImageButton)findViewById(R.id.imageButton1);
		back = (ImageButton)findViewById(R.id.imageButton3);
		img = (ImageButton)findViewById(R.id.imageButton2);
		tweetbtn.setEnabled(false); back.setEnabled(false); img.setEnabled(false);
		
		final String tweetText = TweetText.getText().toString();
		
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Void... params) {
				final StatusUpdate status = new StatusUpdate(tweetText);
				try{
					if(image != null)
						status.media(image);
					if(TweetReplyId == -1)
						MainActivity.twitter.updateStatus(status);
					else
						MainActivity.twitter.updateStatus(status.inReplyToStatusId(TweetReplyId));
					return true;
				}catch(Exception e){
					return false;
				}
			}
			@Override
			protected void onPostExecute(Boolean result) {
				if(result)
					showToast("ツイートしました");
				else
					showToast("ツイートできませんでした");
				if(image != null)
					image = null;
				finish();
			}
		};
		task.execute();
		if(do_back){
			Intent main = new Intent(this, MainActivity.class);
			startActivity(main);
		}
	}
	
	public void image(View v){
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_PICK);
		startActivityForResult(intent, 0);
	}
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
	              ContentResolver cr = getContentResolver();
	              String[] columns = {MediaStore.Images.Media.DATA};
	              Cursor c = cr.query(data.getData(), columns, null, null, null);
	              c.moveToFirst();
	              image = new File(c.getString(0));
	              ImageView iv = (ImageView)findViewById(R.id.imageView1);
	              iv.setImageURI(data.getData());
	              showToast("画像を選択しました");
            } catch (Exception e) {
            	showToast("画像を選択できませんでした");
            }
        }
    }
	
	public void moji140count(){
		TweetText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				moji140.setText(String.valueOf(140 - s.length()));
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}
	
	public void showToast(String text){
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}
	
	public void back(View v){
		finish();
	}
	
	public void setting(View v){
		startActivity(new Intent(this, MyPreferenceActivity.class));
	}
	
	public void onDestroy(){
		super.onDestroy();
		if(image != null)
			image = null;
	}
	
	public void background(View v){
		InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}
}