package com.tao.lightning_of_dark;

import java.text.SimpleDateFormat;

import twitter4j.Status;

import com.loopj.android.image.SmartImageView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
		ImageView protect, state;
	}
	
	public View getView(int position, View convertView, ViewGroup parent){
		final ViewHolder holder;
		final Status item = getItem(position);
		
		if (convertView == null){
			convertView = mInflater.inflate(R.layout.list_item_tweet, null);
			TextView name = (TextView)convertView.findViewById(R.id.name_screenName);
			TextView text = (TextView)convertView.findViewById(R.id.tweetText);
			TextView tweet_date = (TextView)convertView.findViewById(R.id.tweet_date);
			SmartImageView icon = (SmartImageView)convertView.findViewById(R.id.icon);
			SmartImageView RetweetedUserIcon = (SmartImageView)convertView.findViewById(R.id.RetweetedUserIcon);
			TextView RetweetedUserScreenName = (TextView)convertView.findViewById(R.id.RetweetedUserScreenName);
			ImageView protect = (ImageView)convertView.findViewById(R.id.UserProtected);
			ImageView state = (ImageView)convertView.findViewById(R.id.tweetState);
			
			holder = new ViewHolder();
			holder.name = name;
			holder.text = text;
			holder.tweet_date = tweet_date;
			holder.icon = icon;
			holder.RetweetedUserIcon = RetweetedUserIcon;
			holder.RetweetedUserScreenName = RetweetedUserScreenName;
			holder.protect = protect;
			holder.state = state;
			
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		//鍵
		if(item.isRetweet()){
			if(!item.getRetweetedStatus().getUser().isProtected())
				holder.protect.setVisibility(View.GONE);
			else
				holder.protect.setVisibility(View.VISIBLE);
		}else{
			if(!item.getUser().isProtected())
				holder.protect.setVisibility(View.GONE);
			else
				holder.protect.setVisibility(View.VISIBLE);
		}
		//リスト背景
		if(item.isRetweetedByMe())
			convertView.setBackgroundResource(R.drawable.retweeted_by_me);
		else if(item.isRetweet())
			convertView.setBackgroundResource(R.drawable.retweet);
		else if(item.getUser().getScreenName().equals(MainActivity.MyScreenName))
			convertView.setBackgroundResource(R.drawable.same_my_screenname);
		else if(MainActivity.mentionPattern.matcher(item.getText()).find())
			convertView.setBackgroundResource(R.drawable.mention);
		else{
			if(position % 2 == 0)
				convertView.setBackgroundResource(R.drawable.position0);
			else
				convertView.setBackgroundResource(R.drawable.position1);
		}
		//アイコン、名前、スクリーンネーム、タイムスタンプ、クライアント
		if(item.isRetweet()){
			holder.RetweetedUserIcon.setVisibility(View.VISIBLE);
			holder.RetweetedUserScreenName.setVisibility(View.VISIBLE);
			
			holder.name.setText(item.getRetweetedStatus().getUser().getName() + " - @" + item.getRetweetedStatus().getUser().getScreenName());
			holder.text.setText(item.getRetweetedStatus().getText());
			holder.tweet_date.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(item.getRetweetedStatus().getCreatedAt())
					+ "  Retweeted by ");
			holder.RetweetedUserIcon.setImageUrl(item.getUser().getProfileImageURL());
			holder.RetweetedUserScreenName.setText("@" + item.getUser().getScreenName());
			if(MainActivity.getBigIcon)
				holder.icon.setImageUrl(item.getRetweetedStatus().getUser().getBiggerProfileImageURL());
			else
				holder.icon.setImageUrl(item.getRetweetedStatus().getUser().getProfileImageURL());
		}else{
			holder.RetweetedUserIcon.setVisibility(View.GONE);
			holder.RetweetedUserScreenName.setVisibility(View.GONE);
			holder.name.setText(item.getUser().getName() + " - @" + item.getUser().getScreenName());
			holder.text.setText(item.getText());
			holder.tweet_date.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(item.getCreatedAt())
					+ "  via " + item.getSource().replaceAll("<.+?>", ""));
			if(MainActivity.getBigIcon)
				holder.icon.setImageUrl(item.getUser().getBiggerProfileImageURL());
			else
				holder.icon.setImageUrl(item.getUser().getProfileImageURL());
		}
		holder.icon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getContext(), UserPage.class);
				if(item.isRetweet())
					intent.putExtra("userScreenName", item.getRetweetedStatus().getUser().getScreenName());
				else
					intent.putExtra("userScreenName", item.getUser().getScreenName());
				getContext().startActivity(intent);
			}
		});
		return convertView;
	}
}
