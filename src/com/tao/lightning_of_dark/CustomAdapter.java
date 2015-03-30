package com.tao.lightning_of_dark;

import java.text.SimpleDateFormat;

import twitter4j.Status;
import twitter4j.TwitterException;

import com.loopj.android.image.SmartImageView;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomAdapter extends ArrayAdapter<Status> {
	private LayoutInflater mInflater;
	public CustomAdapter(Context context){
		super(context, android.R.layout.simple_list_item_1);
		mInflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	}

	static class ViewHolder{
		TextView name, text, tweet_date, RetweetedUserScreenName;
		SmartImageView icon, RetweetedUserIcon;
	}
	public View getView(int position, View convertView, ViewGroup parent){
		final ViewHolder holder;
		if (convertView == null){
			convertView = mInflater.inflate(R.layout.list_item_tweet, null);
			TextView name = (TextView)convertView.findViewById(R.id.name_screenName);
			TextView text = (TextView)convertView.findViewById(R.id.tweetText);
			TextView tweet_date = (TextView)convertView.findViewById(R.id.tweet_date);
			SmartImageView icon = (SmartImageView)convertView.findViewById(R.id.icon);
			SmartImageView RetweetedUserIcon = (SmartImageView)convertView.findViewById(R.id.RetweetedUserIcon);
			TextView RetweetedUserScreenName = (TextView)convertView.findViewById(R.id.RetweetedUserScreenName);
			
			holder = new ViewHolder();
			holder.name = name;
			holder.text = text;
			holder.tweet_date = tweet_date;
			holder.icon = icon;
			holder.RetweetedUserIcon = RetweetedUserIcon;
			holder.RetweetedUserScreenName = RetweetedUserScreenName;
			
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		Status item = getItem(position);
		
		if(item.isRetweeted() || item.isRetweetedByMe())
			convertView.setBackgroundColor(Color.parseColor("#c9f999"));
		else if(item.isRetweet())
			convertView.setBackgroundColor(Color.parseColor("#c9d9f9"));
		else if(item.getUser().getScreenName().equals(MainActivity.MyScreenName))
			convertView.setBackgroundColor(Color.parseColor("#c9f999"));
		else if(item.getText().matches(".*@" + MainActivity.MyScreenName + ".*"))
			convertView.setBackgroundColor(Color.parseColor("#f9c9c9"));
		else
			if(position % 2 == 0)
				convertView.setBackgroundColor(Color.parseColor("#f9f9f9"));
			else
				convertView.setBackgroundColor(Color.parseColor("#e9e9e9"));
		
		if(item.isRetweetedByMe()){
			holder.name.setText(item.getUser().getName() + " - @" + item.getUser().getScreenName());
			holder.text.setText(item.getText());
			holder.tweet_date.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(item.getCreatedAt())
					+ "  Retweeted by Me");
			holder.RetweetedUserIcon.setImageResource(R.raw.empty);
			holder.RetweetedUserScreenName.setText("");
			holder.icon.setImageUrl(item.getUser().getProfileImageURL());
		}else if(item.isRetweet()){
			holder.name.setText(item.getRetweetedStatus().getUser().getName() + " - @" + item.getRetweetedStatus().getUser().getScreenName());
			holder.text.setText(item.getRetweetedStatus().getText());
			holder.tweet_date.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(item.getRetweetedStatus().getCreatedAt())
					+ "  Retweeted by ");
			holder.RetweetedUserIcon.setImageUrl(item.getUser().getProfileImageURL());
			holder.RetweetedUserScreenName.setText("@" + item.getUser().getScreenName());
			holder.icon.setImageUrl(item.getRetweetedStatus().getUser().getProfileImageURL());
		}else{
			holder.name.setText(item.getUser().getName() + " - @" + item.getUser().getScreenName());
			holder.text.setText(item.getText());
			holder.tweet_date.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(item.getCreatedAt())
					+ "  via " + item.getSource().replaceAll("<.+?>", ""));
			holder.icon.setImageUrl(item.getUser().getProfileImageURL());
			holder.RetweetedUserIcon.setImageResource(R.raw.empty);
			holder.RetweetedUserScreenName.setText("");
		}
		return convertView;
	}
}
