package com.tao.lightning_of_dark;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class IntentActivity extends Activity {
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Intent i;
		String uri;
		if(MainActivity.twitter == null)
			new MainActivity().onlyLogin(this);

		if(Intent.ACTION_VIEW.equals(getIntent().getAction())){
			uri = getIntent().getData().toString();
			Matcher m = Pattern.compile("http(s)?://twitter.com/(\\w*)").matcher(uri);
			if(m.find()){
				i = new Intent(this, UserPage.class);
				i.putExtra("userScreenName", m.group(2));
				startActivity(i);
			}
		}else if(Intent.ACTION_SEND.equals(getIntent().getAction())){
			uri = getIntent().getExtras().getCharSequence(Intent.EXTRA_TEXT).toString();
			i = new Intent(this, TweetActivity.class);
			i.putExtra("pakuri", uri);
			i.putExtra("do_back", false);
			startActivity(i);
		}
		finish();
	}
}
