package com.tao.lightning_of_dark;

import java.text.SimpleDateFormat;
import java.util.Locale;

import twitter4j.Status;

import com.loopj.android.image.SmartImageView;
import com.tao.lightning_of_dark.R;
import com.tao.lightning_of_dark.userPageFragment.UserPage;

import android.annotation.SuppressLint;
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

public class CustomAdapter extends ArrayAdapter<Status>{

	private LayoutInflater mInflater;
	private ApplicationClass appClass;

	public CustomAdapter(Context context){
		super(context, android.R.layout.simple_list_item_1);
		mInflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		appClass = (ApplicationClass)context.getApplicationContext();
	}

	class ViewHolder{
		TextView name, text, tweet_date, retweetedUserScreenName;
		SmartImageView icon, retweetedUserIcon;
		ImageView protect;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		final ViewHolder holder;
		final Status item = getItem(position);

		if(convertView == null){
			convertView = mInflater.inflate(R.layout.list_item_tweet, null);
			TextView name = (TextView)convertView.findViewById(R.id.name_screenName);
			TextView text = (TextView)convertView.findViewById(R.id.tweetText);
			TextView tweet_date = (TextView)convertView.findViewById(R.id.tweet_date);
			SmartImageView icon = (SmartImageView)convertView.findViewById(R.id.icon);
			SmartImageView retweetedUserIcon = (SmartImageView)convertView.findViewById(R.id.RetweetedUserIcon);
			TextView retweetedUserScreenName = (TextView)convertView.findViewById(R.id.RetweetedUserScreenName);
			ImageView protect = (ImageView)convertView.findViewById(R.id.UserProtected);

			holder = new ViewHolder();
			holder.name = name;
			holder.text = text;
			holder.tweet_date = tweet_date;
			holder.icon = icon;
			holder.retweetedUserIcon = retweetedUserIcon;
			holder.retweetedUserScreenName = retweetedUserScreenName;
			holder.protect = protect;

			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		// 鍵
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
		// リスト背景
		if(item.isRetweetedByMe())
			convertView.setBackgroundResource(R.drawable.retweeted_by_me);
		else if(item.isRetweet())
			convertView.setBackgroundResource(R.drawable.retweet);
		else if(item.getUser().getScreenName().equals(appClass.getMyScreenName()))
			convertView.setBackgroundResource(R.drawable.same_my_screenname);
		else if(appClass.getMentionPattern().matcher(item.getText()).find())
			convertView.setBackgroundResource(R.drawable.mention);
		else{
			if(position % 2 == 0)
				convertView.setBackgroundResource(R.drawable.position0);
			else
				convertView.setBackgroundResource(R.drawable.position1);
		}
		// アイコン、名前、スクリーンネーム、タイムスタンプ、クライアント
		if(item.isRetweet()){
			holder.retweetedUserIcon.setVisibility(View.VISIBLE);
			holder.retweetedUserScreenName.setVisibility(View.VISIBLE);

			holder.name.setText(item.getRetweetedStatus().getUser().getName() + " - @" + item.getRetweetedStatus().getUser().getScreenName());
			holder.text.setText(item.getRetweetedStatus().getText());
			holder.tweet_date.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPANESE).format(
					item.getRetweetedStatus().getCreatedAt()) + "  Retweeted by ");
			holder.retweetedUserIcon.setImageUrl(item.getUser().getProfileImageURL(), null, R.drawable.ic_action_refresh);
			holder.retweetedUserScreenName.setText("@" + item.getUser().getScreenName());
			if(appClass.getGetBigIcon())
				holder.icon.setImageUrl(item.getRetweetedStatus().getUser().getBiggerProfileImageURL(), null, R.drawable.ic_action_refresh);
			else
				holder.icon.setImageUrl(item.getRetweetedStatus().getUser().getProfileImageURL(), null, R.drawable.ic_action_refresh);
		}else{
			holder.retweetedUserIcon.setVisibility(View.GONE);
			holder.retweetedUserScreenName.setVisibility(View.GONE);
			holder.name.setText(item.getUser().getName() + " - @" + item.getUser().getScreenName());
			holder.text.setText(item.getText());
			holder.tweet_date.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPANESE).format(item.getCreatedAt())
					+ "  via " + item.getSource().replaceAll("<.+?>", ""));
			if(appClass.getGetBigIcon())
				holder.icon.setImageUrl(item.getUser().getBiggerProfileImageURL(), null, R.drawable.ic_action_refresh);
			else
				holder.icon.setImageUrl(item.getUser().getProfileImageURL(), null, R.drawable.ic_action_refresh);
		}
		holder.icon.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
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
