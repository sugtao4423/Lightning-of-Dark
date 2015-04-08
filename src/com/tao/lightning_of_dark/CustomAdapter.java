package com.tao.lightning_of_dark;

import java.text.SimpleDateFormat;
import twitter4j.Status;

import com.loopj.android.image.SmartImageView;

import android.app.Activity;
import android.content.Context;
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

	class ViewHolder{
		TextView name, text, tweet_date, RetweetedUserScreenName;
		SmartImageView icon, RetweetedUserIcon;
	}
	
	public View getView(int position, View convertView, ViewGroup parent){
		final ViewHolder holder;
		Status item = getItem(position);
		
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
		
		if(item.isRetweetedByMe())
			convertView.setBackgroundResource(R.drawable.retweeted_by_me);
		else if(item.isRetweet())
			convertView.setBackgroundResource(R.drawable.retweet);
		else if(item.getUser().getScreenName().equals(MainActivity.MyScreenName))
			convertView.setBackgroundResource(R.drawable.same_my_screenname);
		else if(MainActivity.mentionPattern.matcher(item.getText()).find())
			convertView.setBackgroundResource(R.drawable.mention);
		else
			if(position % 2 == 0)
				convertView.setBackgroundResource(R.drawable.position0);
			else
				convertView.setBackgroundResource(R.drawable.position1);
		
		if(item.isRetweet()){
			holder.RetweetedUserIcon.setVisibility(View.VISIBLE);
			holder.RetweetedUserScreenName.setVisibility(View.VISIBLE);
			
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
			holder.RetweetedUserIcon.setVisibility(View.GONE);
			holder.RetweetedUserScreenName.setVisibility(View.GONE);
		}
		return convertView;
	}
}
