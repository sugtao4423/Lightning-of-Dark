package com.tao.lightning_of_dark;

import java.io.File;
import java.util.ArrayList;

import com.tao.lightning_of_dark.R;

import twitter4j.StatusUpdate;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class TweetActivity extends Activity{

	private EditText tweetText;
	private TextView moji140;
	private long tweetReplyId;
	private String replyUserScreenName, replyTweetText, pakuri;
	private File image;
	private boolean do_back, do_setSelection;
	private ApplicationClass appClass;

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tweet_activity);

		appClass = (ApplicationClass)this.getApplicationContext();
		getActionBar().setDisplayShowHomeEnabled(false);

		TextView tweetAccount = (TextView)findViewById(R.id.tweetAccount);
		tweetAccount.setText("@" + appClass.getMyScreenName());

		tweetText = (EditText)findViewById(R.id.tweetText);
		moji140 = (TextView)findViewById(R.id.moji140);

		replyTweetText = getIntent().getStringExtra("ReplyTweetText");
		replyUserScreenName = getIntent().getStringExtra("ReplyUserScreenName");
		tweetReplyId = getIntent().getLongExtra("TweetReplyId", -1);
		pakuri = getIntent().getStringExtra("pakuri");
		do_back = getIntent().getBooleanExtra("do_back", true);
		do_setSelection = getIntent().getBooleanExtra("do_setSelection", true);

		if(tweetReplyId == -1) {
			getActionBar().setTitle("New Tweet");
			tweetText.setText(pakuri);
		}else{
			getActionBar().setTitle(replyUserScreenName + " : " + replyTweetText);
			if(replyTweetText.length() > 10)
				replyTweetText = replyTweetText.substring(0, 10);
			tweetText.setText("@" + replyUserScreenName + " ");
		}
		if(do_setSelection)
			tweetText.setSelection(tweetText.getText().length());

		moji140.setText(String.valueOf(140 - tweetText.getText().length()));
		moji140count();
	}

	public void tweet(View v){
		final ImageButton tweetbtn, back, img;
		tweetbtn = (ImageButton)findViewById(R.id.imageButton1);
		back = (ImageButton)findViewById(R.id.imageButton3);
		img = (ImageButton)findViewById(R.id.imageButton2);
		tweetbtn.setEnabled(false);
		back.setEnabled(false);
		img.setEnabled(false);

		final String text = tweetText.getText().toString();

		new AsyncTask<Void, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Void... params){
				final StatusUpdate status = new StatusUpdate(text);
				try{
					if(image != null)
						status.media(image);
					if(tweetReplyId == -1)
						appClass.getTwitter().updateStatus(status);
					else
						appClass.getTwitter().updateStatus(status.inReplyToStatusId(tweetReplyId));
					return true;
				}catch(Exception e){
					return false;
				}
			}

			@Override
			protected void onPostExecute(Boolean result){
				if(result)
					new ShowToast("ツイートしました", TweetActivity.this, 0);
				else
					new ShowToast("ツイートできませんでした", TweetActivity.this, 0);
				if(image != null)
					image = null;
				finish();
			}
		}.execute();
		if(do_back) {
			Intent main = new Intent(this, MainActivity.class);
			startActivity(main);
		}
	}

	public void image(View v){
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_PICK);
		startActivityForResult(intent, 810);
	}

	public void mic(View v){
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "音声入力");
		startActivityForResult(intent, 114514);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 114514 && resultCode == RESULT_OK) { // 音声入力
			ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			tweetText.setText(tweetText.getText().toString() + results.get(0));
			cursor_end(null);
		}
		if(requestCode == 810 && resultCode == RESULT_OK) { // 画像選択
			try{
				ContentResolver cr = getContentResolver();
				String[] columns = {MediaStore.Images.Media.DATA};
				Cursor c = cr.query(data.getData(), columns, null, null, null);
				c.moveToFirst();
				image = new File(c.getString(0));
				ImageView iv = (ImageView)findViewById(R.id.UserProtected);
				iv.setImageURI(data.getData());
				new ShowToast("画像を選択しました", TweetActivity.this, 0);
			}catch(Exception e){
				new ShowToast("画像を選択できませんでした", TweetActivity.this, 0);
			}
		}
	}

	public void moji140count(){
		tweetText.addTextChangedListener(new TextWatcher(){
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count){
				moji140.setText(String.valueOf(140 - s.length()));
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after){
			}

			@Override
			public void afterTextChanged(Editable s){
			}
		});
	}

	public void cursor_start(View v){
		tweetText.setSelection(0);
	}

	public void cursor_end(View v){
		tweetText.setSelection(tweetText.getText().length());
	}

	public void back(View v){
		finish();
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		if(image != null)
			image = null;
	}
}