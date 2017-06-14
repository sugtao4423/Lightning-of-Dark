package com.tao.lightning_of_dark;

import java.text.SimpleDateFormat;
import java.util.Locale;

import twitter4j.ExtendedMediaEntity;
import twitter4j.Status;

import com.loopj.android.image.SmartImageView;
import com.tao.lightning_of_dark.R;
import com.tao.lightning_of_dark.swipeImageViewer.ImageFragmentActivity;
import com.tao.lightning_of_dark.userPageFragment.UserPage;
import com.tao.lightning_of_dark.utils.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
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
		HorizontalScrollView tweetImagesScroll;
		LinearLayout tweetImagesLayout;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		final Context context = parent.getContext();
		final ViewHolder holder;
		Status item = getItem(position);
		final Status origStatus = item.isRetweet() ? item.getRetweetedStatus() : item;

		if(convertView == null){
			convertView = mInflater.inflate(R.layout.list_item_tweet, null);
			TextView name = (TextView)convertView.findViewById(R.id.name_screenName);
			TextView text = (TextView)convertView.findViewById(R.id.tweetText);
			TextView tweet_date = (TextView)convertView.findViewById(R.id.tweet_date);
			SmartImageView icon = (SmartImageView)convertView.findViewById(R.id.icon);
			SmartImageView retweetedUserIcon = (SmartImageView)convertView.findViewById(R.id.RetweetedUserIcon);
			TextView retweetedUserScreenName = (TextView)convertView.findViewById(R.id.RetweetedUserScreenName);
			ImageView protect = (ImageView)convertView.findViewById(R.id.UserProtected);
			HorizontalScrollView tweetImagesScroll = (HorizontalScrollView)convertView.findViewById(R.id.tweet_images_scroll);
			LinearLayout tweetImagesLayout = (LinearLayout)convertView.findViewById(R.id.tweet_images_layout);

			holder = new ViewHolder();
			holder.name = name;
			holder.text = text;
			holder.tweet_date = tweet_date;
			holder.icon = icon;
			holder.retweetedUserIcon = retweetedUserIcon;
			holder.retweetedUserScreenName = retweetedUserScreenName;
			holder.protect = protect;
			holder.tweetImagesScroll = tweetImagesScroll;
			holder.tweetImagesLayout = tweetImagesLayout;

			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		// 鍵
		if(origStatus.getUser().isProtected())
			holder.protect.setVisibility(View.VISIBLE);
		else
			holder.protect.setVisibility(View.GONE);
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
			holder.tweet_date.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPANESE)
					.format(item.getRetweetedStatus().getCreatedAt()) + "  Retweeted by ");
			holder.retweetedUserIcon.setImageUrl(item.getUser().getProfileImageURL(), null, R.drawable.ic_action_refresh);
			holder.retweetedUserScreenName.setText("@" + item.getUser().getScreenName());
		}else{
			holder.retweetedUserIcon.setVisibility(View.GONE);
			holder.retweetedUserScreenName.setVisibility(View.GONE);
			holder.tweet_date.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPANESE).format(item.getCreatedAt())
					+ "  via " + item.getSource().replaceAll("<.+?>", ""));
		}

		holder.name.setText(origStatus.getUser().getName() + " - @" + origStatus.getUser().getScreenName());
		holder.text.setText(origStatus.getText());
		holder.icon.setImageUrl(origStatus.getUser().getBiggerProfileImageURL(), null, R.drawable.ic_action_refresh);

		holder.icon.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				Intent intent = new Intent(getContext(), UserPage.class);
				intent.putExtra("userScreenName", origStatus.getUser().getScreenName());
				getContext().startActivity(intent);
			}
		});

		ExtendedMediaEntity[] exMentitys = origStatus.getExtendedMediaEntities();
		if(exMentitys != null && exMentitys.length > 0){
			holder.tweetImagesScroll.setVisibility(View.VISIBLE);
			holder.tweetImagesLayout.removeAllViews();
			for(int i = 0; i < exMentitys.length; i++){
				LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				if(holder.tweetImagesLayout.getChildCount() != 0)
					params.setMargins(8, 0, 0, 0);
				SmartImageView child = new SmartImageView(parent.getContext());
				child.setLayoutParams(params);
				child.setMaxHeight(200);
				child.setAdjustViewBounds(true);
				holder.tweetImagesLayout.addView(child);

				if(Utils.isVideoOrGif(exMentitys[i])){
					final boolean isGif = Utils.isGif(exMentitys[i]);
					final String[] videoUrl = Utils.getVideoURLsSortByBitrate(appClass, exMentitys);
					child.setImageResource(R.drawable.video_play);
					child.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v){
							Intent intent = new Intent(context, Show_Video.class);
							intent.putExtra("URL", videoUrl[videoUrl.length - 1]);
							if(isGif)
								intent.putExtra("type", Show_Video.TYPE_GIF);
							else
								intent.putExtra("type", Show_Video.TYPE_VIDEO);
							context.startActivity(intent);
						}
					});
				}else{
					child.setImageUrl(exMentitys[i].getMediaURL() + ":small", null, R.drawable.ic_action_refresh);
					final int pos = i;
					final String[] urls = new String[exMentitys.length];
					for(int j = 0; j < urls.length; j++)
						urls[j] = exMentitys[j].getMediaURL();
					child.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v){
							Intent intent = new Intent(context, ImageFragmentActivity.class);
							intent.putExtra("urls", urls);
							intent.putExtra("position", pos);
							context.startActivity(intent);
						}
					});
				}
			}
		}else{
			holder.tweetImagesScroll.setVisibility(View.GONE);
		}
		return convertView;
	}
}
