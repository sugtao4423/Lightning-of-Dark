package com.tao.lightning_of_dark;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import com.loopj.android.image.SmartImageView;

import dialog_onClick.Dialog_ListClick;
import dialog_onClick.Dialog_deletePost;
import dialog_onClick.Dialog_favorite;
import dialog_onClick.Dialog_reply;
import dialog_onClick.Dialog_retweet;
import dialog_onClick.Dialog_talk;
import dialog_onClick.Dialog_unOfficialRT;
import twitter4j.ExtendedMediaEntity;
import twitter4j.ExtendedMediaEntity.Variant;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class ListViewListener implements OnItemClickListener, OnItemLongClickListener{

	private AlertDialog dialog;
	private boolean tweet_do_back;

	public ListViewListener(boolean tweet_do_back){
		this.tweet_do_back = tweet_do_back;
	}

	@SuppressLint("InflateParams")
	@Override
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, long id){
		final Status item = (Status)parent.getItemAtPosition(position);
		ApplicationClass appClass = (ApplicationClass)parent.getContext().getApplicationContext();

		ArrayAdapter<String> list = new ArrayAdapter<String>(parent.getContext(), android.R.layout.simple_list_item_1);
		if(appClass.getOption_regex())
			list.add("正規表現で抽出");
		if(appClass.getOption_openBrowser())
			list.add("ブラウザで開く");

		list.add("@" + item.getUser().getScreenName());

		UserMentionEntity[] mentionEntitys = item.getUserMentionEntities();
		if(mentionEntitys != null && mentionEntitys.length > 0) {
			for(int i = 0; i < mentionEntitys.length; i++){
				if(!mentionEntitys[i].getScreenName().equals(item.getUser().getScreenName()))
					list.add("@" + mentionEntitys[i].getScreenName());
			}
		}
		URLEntity[] uentitys = item.getURLEntities();
		if(uentitys != null && uentitys.length > 0) {
			for(URLEntity u : uentitys)
				list.add(u.getExpandedURL());
		}
		ExtendedMediaEntity[] exMentitys = item.getExtendedMediaEntities();
		if(exMentitys != null && exMentitys.length > 0) {
			for(ExtendedMediaEntity ex : exMentitys){
				if(ex.getType().equals("video") || ex.getType().equals("animated_gif")) {
					ArrayList<VideoURLs> urls = new ArrayList<VideoURLs>();
					for(Variant v : ex.getVideoVariants()){
						boolean find = false;
						if(appClass.getIsWebm() && v.getContentType().equals("video/webm"))
							find = true;
						else if(!appClass.getIsWebm() && v.getContentType().equals("video/mp4"))
							find = true;

						if(find) {
							VideoURLs video = new VideoURLs(v.getBitrate(), v.getUrl());
							urls.add(video);
						}
					}
					if(urls.size() == 0) {
						for(Variant v : ex.getVideoVariants()){
							boolean find = false;
							if(v.getContentType().equals("video/mp4"))
								find = true;
							else if(v.getContentType().equals("video/webm"))
								find = true;

							if(find) {
								VideoURLs video = new VideoURLs(v.getBitrate(), v.getUrl());
								urls.add(video);
							}
						}
					}
					Collections.sort(urls);
					if(urls.size() == 0)
						list.add("ビデオの取得に失敗");
					else
						list.add(urls.get(urls.size() - 1).url);
				}else{
					list.add(ex.getMediaURL());
				}
			}
		}

		// ダイアログタイトルinflate
		View dialog_title = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_tweet, null);
		SmartImageView icon = (SmartImageView)dialog_title.findViewById(R.id.icon);
		TextView name_screenName = (TextView)dialog_title.findViewById(R.id.name_screenName);
		TextView tweetText = (TextView)dialog_title.findViewById(R.id.tweetText);
		TextView tweetDate = (TextView)dialog_title.findViewById(R.id.tweet_date);
		ImageView protect = (ImageView)dialog_title.findViewById(R.id.UserProtected);

		if(item.isRetweet()) {
			if(item.getRetweetedStatus().getUser().isProtected())
				protect.setVisibility(View.VISIBLE);
			else
				protect.setVisibility(View.GONE);
			tweetText.setText(item.getRetweetedStatus().getText());
			name_screenName.setText(item.getRetweetedStatus().getUser().getName() + " - @" + item.getRetweetedStatus().getUser().getScreenName());
			tweetDate.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPANESE).format(item.getCreatedAt()) + "  via "
					+ item.getRetweetedStatus().getSource().replaceAll("<.+?>", ""));
			if(appClass.getGetBigIcon())
				icon.setImageUrl(item.getRetweetedStatus().getUser().getBiggerProfileImageURL());
			else
				icon.setImageUrl(item.getRetweetedStatus().getUser().getProfileImageURL());
		}else{
			if(!item.getUser().isProtected())
				protect.setVisibility(View.GONE);
			else
				protect.setVisibility(View.VISIBLE);
			tweetText.setText(item.getText());
			name_screenName.setText(item.getUser().getName() + " - @" + item.getUser().getScreenName());
			tweetDate.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPANESE).format(item.getCreatedAt()) + "  via "
					+ item.getSource().replaceAll("<.+?>", ""));
			if(appClass.getGetBigIcon())
				icon.setImageUrl(item.getUser().getBiggerProfileImageURL());
			else
				icon.setImageUrl(item.getUser().getProfileImageURL());
		}
		// ここまで

		// ダイアログ本文inflate
		View content = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_dialog, null);
		ListView dialog_list = (ListView)content.findViewById(R.id.dialog_List);
		ImageButton dialog_reply = (ImageButton)content.findViewById(R.id.dialog_reply);
		ImageButton dialog_retweet = (ImageButton)content.findViewById(R.id.dialog_retweet);
		ImageButton dialog_unOfficialRT = (ImageButton)content.findViewById(R.id.dialog_unofficialRT);
		ImageButton dialog_favorite = (ImageButton)content.findViewById(R.id.dialog_favorite);
		ImageButton dialog_talk = (ImageButton)content.findViewById(R.id.dialog_talk);
		ImageButton dialog_deletePost = (ImageButton)content.findViewById(R.id.dialog_delete);

		dialog_list.setAdapter(list);
		dialog_list.setOnItemClickListener(new Dialog_ListClick(item, parent, tweet_do_back));
		dialog_list.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id){
				String clickedText = (String)parent.getItemAtPosition(position);
				if(clickedText.startsWith("http")) {
					dialog.dismiss();
					parent.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(clickedText)));
				}
				return true;
			}
		});

		dialog_reply.setOnClickListener(new Dialog_reply(item, parent.getContext(), tweet_do_back));
		dialog_retweet.setOnClickListener(new Dialog_retweet(item, parent.getContext()));
		dialog_unOfficialRT.setOnClickListener(new Dialog_unOfficialRT(item, parent.getContext(), tweet_do_back));
		dialog_favorite.setOnClickListener(new Dialog_favorite(item, parent.getContext()));
		dialog_talk.setOnClickListener(new Dialog_talk(item, parent.getContext(), tweet_do_back));
		dialog_deletePost.setOnClickListener(new Dialog_deletePost(item, parent.getContext()));

		if(item.isRetweet()) {
			if(!(item.getRetweetedStatus().getInReplyToStatusId() > 0)) {
				dialog_talk.setEnabled(false);
				dialog_talk.setBackgroundColor(Color.parseColor("#a7a7a7"));
			}
			if(!item.getRetweetedStatus().getUser().getScreenName()
					.equals(((ApplicationClass)parent.getContext().getApplicationContext()).getMyScreenName())) {
				dialog_deletePost.setEnabled(false);
				dialog_deletePost.setBackgroundColor(Color.parseColor("#a7a7a7"));
			}
		}else{
			if(!(item.getInReplyToStatusId() > 0)) {
				dialog_talk.setEnabled(false);
				dialog_talk.setBackgroundColor(Color.parseColor("#a7a7a7"));
			}
			if(!item.getUser().getScreenName()
					.equals(((ApplicationClass)parent.getContext().getApplicationContext()).getMyScreenName())) {
				dialog_deletePost.setEnabled(false);
				dialog_deletePost.setBackgroundColor(Color.parseColor("#a7a7a7"));
			}
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());
		builder.setCustomTitle(dialog_title).setView(content);

		dialog = builder.create();
		((ApplicationClass)parent.getContext().getApplicationContext()).setListViewDialog(dialog);
		dialog.show();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id){
		Status item = (Status)parent.getItemAtPosition(position);
		Intent pakuri = new Intent(parent.getContext(), TweetActivity.class);
		if(item.isRetweet())
			pakuri.putExtra("pakuri", item.getRetweetedStatus().getText());
		else
			pakuri.putExtra("pakuri", item.getText());
		parent.getContext().startActivity(pakuri);
		return true;
	}
}

class VideoURLs implements Comparable<VideoURLs>{

	int bitrate;
	String url;

	public VideoURLs(int bitrate, String url){
		this.bitrate = bitrate;
		this.url = url;
	}

	@Override
	public int compareTo(VideoURLs another){
		return this.bitrate - another.bitrate;
	}
}