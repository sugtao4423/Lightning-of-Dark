package sugtao4423.lod;

import java.io.File;
import java.util.ArrayList;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.UserMentionEntity;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
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
import sugtao4423.lod.dialog_onclick.StatusItem;
import sugtao4423.lod.tweetlistview.TweetListAdapter;
import sugtao4423.lod.tweetlistview.TweetListView;

public class TweetActivity extends Activity{

	public static final int TYPE_NEWTWEET = 0;
	public static final int TYPE_REPLY = 1;
	public static final int TYPE_REPLYALL = 2;
	public static final int TYPE_QUOTERT = 3;
	public static final int TYPE_UNOFFICIALRT = 4;
	public static final int TYPE_PAKUTSUI = 5;
	public static final int TYPE_EXTERNALTEXT = 6;

	private EditText tweetText;
	private TextView moji140;
	private Status status;
	private int type;
	private File image;
	private ApplicationClass appClass;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tweet_activity);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);

		appClass = (ApplicationClass)getApplicationContext();

		TextView tweetAccount = (TextView)findViewById(R.id.tweetAccount);
		tweetAccount.setText("@" + appClass.getMyScreenName());

		tweetText = (EditText)findViewById(R.id.tweetText);
		moji140 = (TextView)findViewById(R.id.moji140);

		Intent intent = getIntent();
		if(intent.getSerializableExtra("status") != null){
			status = ((StatusItem)intent.getSerializableExtra("status")).getStatus();
			status = status.isRetweet() ? status.getRetweetedStatus() : status;
		}

		type = intent.getIntExtra("type", 0);

		boolean setSelectionEnd = false;

		TweetListView originStatus = (TweetListView)findViewById(R.id.originStatus);
		switch(type){
		case TYPE_REPLY:
		case TYPE_REPLYALL:
		case TYPE_QUOTERT:
			actionBar.hide();
			originStatus.setVisibility(View.VISIBLE);
			originStatus.setFocusable(false);
			TweetListAdapter adapter = new TweetListAdapter(this);
			adapter.setOnItemClickListener(new ListViewListener());
			adapter.add(status);
			originStatus.setAdapter(adapter);
			break;
		default:
			originStatus.setVisibility(View.GONE);
			break;
		}

		switch(type){
		case TYPE_NEWTWEET:
			actionBar.setTitle("New Tweet");
			break;
		case TYPE_REPLY:
			tweetText.setText("@" + status.getUser().getScreenName() + " ");
			setSelectionEnd = true;
			break;
		case TYPE_REPLYALL:
			ArrayList<String> mentionUsers = new ArrayList<String>();
			mentionUsers.add(status.getUser().getScreenName());
			UserMentionEntity[] mentionEntitys = status.getUserMentionEntities();
			for(UserMentionEntity mention : mentionEntitys){
				if(mention.getScreenName().equals(appClass.getMyScreenName()) || mentionUsers.indexOf(mention.getScreenName()) != -1)
					continue;
				mentionUsers.add(mention.getScreenName());
			}
			String replyUserScreenNames = "";
			for(String user : mentionUsers)
				replyUserScreenNames += "@" + user + " ";
			tweetText.setText(replyUserScreenNames);
			setSelectionEnd = true;
			break;
		case TYPE_QUOTERT:
			String quote = " https://twitter.com/" + status.getUser().getScreenName() + "/status/" + String.valueOf(status.getId());
			tweetText.setText(quote);
			break;
		case TYPE_UNOFFICIALRT:
			actionBar.setTitle("UnOfficialRT");
			String unOfficial = " RT @" + status.getUser().getScreenName() + ": " + status.getText();
			tweetText.setText(unOfficial);
			break;
		case TYPE_PAKUTSUI:
			actionBar.setTitle("New Tweet");
			tweetText.setText(status.getText());
			setSelectionEnd = true;
			break;
		case TYPE_EXTERNALTEXT:
			actionBar.setTitle("New Tweet");
			tweetText.setText(intent.getStringExtra("text"));
			setSelectionEnd = true;
			break;
		}

		if(setSelectionEnd)
			tweetText.setSelection(tweetText.getText().length());

		moji140.setText(String.valueOf(140 - tweetText.getText().length()));
		moji140count();
	}

	public void tweet(View v){
		((ImageButton)findViewById(R.id.tweetButton)).setEnabled(false);
		((ImageButton)findViewById(R.id.tweetClose)).setEnabled(false);
		((ImageButton)findViewById(R.id.imageSelect)).setEnabled(false);

		String text = tweetText.getText().toString();

		StatusUpdate statusUpdate = new StatusUpdate(text);
		if(image != null)
			statusUpdate.media(image);
		if(type == TYPE_REPLY || type == TYPE_REPLYALL)
			appClass.updateStatus(getApplicationContext(), statusUpdate.inReplyToStatusId(TweetActivity.this.status.getId()));
		else
			appClass.updateStatus(getApplicationContext(), statusUpdate);

		finish();
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
		if(requestCode == 114514 && resultCode == RESULT_OK){ // 音声入力
			ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			tweetText.setText(tweetText.getText().toString() + results.get(0));
			cursor_end(null);
		}
		if(requestCode == 810 && resultCode == RESULT_OK){ // 画像選択
			try{
				ContentResolver cr = getContentResolver();
				String[] columns = {MediaStore.Images.Media.DATA};
				Cursor c = cr.query(data.getData(), columns, null, null, null);
				c.moveToFirst();
				image = new File(c.getString(0));
				ImageView iv = (ImageView)findViewById(R.id.selectedImage);
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