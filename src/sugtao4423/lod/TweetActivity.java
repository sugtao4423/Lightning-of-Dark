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
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import sugtao4423.lod.dataclass.Music;
import sugtao4423.lod.tweetlistview.TweetListAdapter;
import sugtao4423.lod.tweetlistview.TweetListView;

public class TweetActivity extends Activity{

	public static final String INTENT_EXTRA_KEY_TYPE = "type";
	public static final String INTENT_EXTRA_KEY_TEXT = "text";
	public static final String INTENT_EXTRA_KEY_STATUS = "status";

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
	private App app;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tweet_activity);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);

		app = (App)getApplicationContext();
		setTypeface();

		TextView tweetAccount = (TextView)findViewById(R.id.tweetAccount);
		tweetAccount.setText("@" + app.getCurrentAccount().getScreenName());

		tweetText = (EditText)findViewById(R.id.tweetText);
		moji140 = (TextView)findViewById(R.id.moji140);

		Intent intent = getIntent();
		if(intent.getSerializableExtra(INTENT_EXTRA_KEY_STATUS) != null){
			status = (Status)intent.getSerializableExtra(INTENT_EXTRA_KEY_STATUS);
			status = status.isRetweet() ? status.getRetweetedStatus() : status;
		}

		type = intent.getIntExtra(INTENT_EXTRA_KEY_TYPE, 0);

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
			adapter.setOnItemLongClickListener(new ListViewListener());
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
				if(mention.getScreenName().equals(app.getCurrentAccount().getScreenName()) || mentionUsers.indexOf(mention.getScreenName()) != -1)
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
			tweetText.setText(intent.getStringExtra(INTENT_EXTRA_KEY_TEXT));
			setSelectionEnd = true;
			break;
		}

		if(setSelectionEnd)
			tweetText.setSelection(tweetText.getText().length());

		moji140.setText(String.valueOf(140 - tweetText.getText().length()));
		moji140count();
	}

	public void setTypeface(){
		Button[] btn = new Button[7];
		btn[0] = (Button)findViewById(R.id.tweetButton);
		btn[1] = (Button)findViewById(R.id.imageSelect);
		btn[2] = (Button)findViewById(R.id.tweetClose);
		btn[3] = (Button)findViewById(R.id.cursor_start);
		btn[4] = (Button)findViewById(R.id.cursor_end);
		btn[5] = (Button)findViewById(R.id.tweetMic);
		btn[6] = (Button)findViewById(R.id.tweetMusic);
		Typeface tf = app.getFontAwesomeTypeface();
		for(Button b : btn){
			b.setTypeface(tf);
		}
	}

	public void tweet(View v){
		((Button)findViewById(R.id.tweetButton)).setEnabled(false);
		((Button)findViewById(R.id.tweetClose)).setEnabled(false);
		((Button)findViewById(R.id.imageSelect)).setEnabled(false);

		String text = tweetText.getText().toString();

		StatusUpdate statusUpdate = new StatusUpdate(text);
		if(image != null)
			statusUpdate.media(image);
		if(type == TYPE_REPLY || type == TYPE_REPLYALL)
			app.updateStatus(statusUpdate.inReplyToStatusId(TweetActivity.this.status.getId()));
		else
			app.updateStatus(statusUpdate);

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

	public void music(View v){
		Music music = app.getMusic();
		if(music == null)
			return;
		String nowplayingFormat = app.getOptions().getNowplayingFormat();
		if(nowplayingFormat.equals("")){
			nowplayingFormat = "%artist% - %track% #nowplaying";
		}
		String str = nowplayingFormat.replaceAll("%track%", music.getTrack()).replaceAll("%artist%", music.getArtist()).replaceAll("%album%", music.getAlbum());
		tweetText.setText(tweetText.getText().toString() + str);
		tweetText.setSelection(tweetText.getText().length());
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
				new ShowToast(getApplicationContext(), R.string.success_selectPic);
			}catch(Exception e){
				new ShowToast(getApplicationContext(), R.string.error_selectPic);
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
	public void onResume(){
		super.onResume();
		app.getUseTime().start();
	}

	@Override
	public void onPause(){
		super.onPause();
		app.getUseTime().stop();
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		if(image != null)
			image = null;
	}
}