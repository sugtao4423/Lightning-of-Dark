package com.tao.lightning_of_dark;

import java.text.NumberFormat;

import com.loopj.android.image.SmartImageView;

import twitter4j.User;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomAdapter_User extends ArrayAdapter<User> {
	private LayoutInflater mInflater;
	public CustomAdapter_User(Context context) {
		super(context, android.R.layout.simple_list_item_1);
		mInflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	}
	
	class ViewHolder{
		TextView name, text, tweet_date, RetweetedUserScreenName;
		SmartImageView icon, RetweetedUserIcon;
		ImageView protect;
		ApplicationClass appClass;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		ViewHolder holder;
		User item = getItem(position);
		
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.list_item_tweet, null);
			TextView name = (TextView)convertView.findViewById(R.id.name_screenName);
			TextView text = (TextView)convertView.findViewById(R.id.tweetText);
			TextView tweet_date = (TextView)convertView.findViewById(R.id.tweet_date);
			SmartImageView icon = (SmartImageView)convertView.findViewById(R.id.icon);
			SmartImageView RetweetedUserIcon = (SmartImageView)convertView.findViewById(R.id.RetweetedUserIcon);
			TextView RetweetedUserScreenName = (TextView)convertView.findViewById(R.id.RetweetedUserScreenName);
			ImageView protect = (ImageView)convertView.findViewById(R.id.UserProtected);
			ApplicationClass appClass = (ApplicationClass)parent.getContext().getApplicationContext();
			
			holder = new ViewHolder();
			holder.name = name;
			holder.text = text;
			holder.tweet_date = tweet_date;
			holder.icon = icon;
			holder.RetweetedUserIcon = RetweetedUserIcon;
			holder.RetweetedUserScreenName = RetweetedUserScreenName;
			holder.protect = protect;
			holder.appClass = appClass;
			
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		holder.RetweetedUserIcon.setVisibility(View.GONE);
		holder.RetweetedUserScreenName.setVisibility(View.GONE);
		
		if(item.isProtected())
			holder.protect.setVisibility(View.VISIBLE);
		else
			holder.protect.setVisibility(View.GONE);
		
		if(position % 2 == 0)
			convertView.setBackgroundResource(R.drawable.position0);
		else
			convertView.setBackgroundResource(R.drawable.position1);
		
		if(holder.appClass.getGetBigIcon())
			holder.icon.setImageUrl(item.getBiggerProfileImageURL());
		else
			holder.icon.setImageUrl(item.getProfileImageURL());
		
		holder.name.setText(item.getName() + " - @" + item.getScreenName());
		holder.text.setText(item.getDescription());
		holder.tweet_date.setText("Tweet: " + numberFormat(item.getStatusesCount()) +
				"  Fav: " + numberFormat(item.getFavouritesCount()) +
				"  Follow: " + numberFormat(item.getFriendsCount()) +
				"  Follower: " + numberFormat(item.getFollowersCount()));
		
		return convertView;
	}
	public String numberFormat(int num){
		return NumberFormat.getInstance().format(num);
	}
}