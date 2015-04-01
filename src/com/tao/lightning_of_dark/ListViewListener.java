package com.tao.lightning_of_dark;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.loopj.android.image.SmartImageView;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class ListViewListener implements OnItemClickListener, OnItemLongClickListener {
	
	@Override
	public void onItemClick(final AdapterView<?> parent, View view, int position, long id) {
		final Status item = (Status)parent.getItemAtPosition(position);
		
		List<String> list = new ArrayList<String>();
		
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(parent.getContext());
		if(pref.getBoolean("menu_reply", true))
			list.add("返信");
		if(pref.getBoolean("menu_retweet", true))
			list.add("リツイート");
		if(pref.getBoolean("menu_fav", true))
			list.add("ふぁぼる");
		if(pref.getBoolean("menu_regex", false))
			list.add("正規表現で抽出");
		
		list.add("@" + item.getUser().getScreenName());
		
		UserMentionEntity[] mentionEntitys = item.getUserMentionEntities();
		if(mentionEntitys != null && mentionEntitys.length > 0){
			for(int i = 0; i < mentionEntitys.length; i++){
				UserMentionEntity umEntity = mentionEntitys[i];
				list.add("@" + umEntity.getScreenName());
			}
		}
		URLEntity[] uentitys = item.getURLEntities();
        if(uentitys != null && uentitys.length > 0){
            for(int i = 0; i < uentitys.length; i++){
                URLEntity uentity = uentitys[i];
                list.add(uentity.getExpandedURL());
            }
        }
        MediaEntity[] mentitys = item.getMediaEntities();
        if(mentitys != null && mentitys.length > 0){
            for(int i = 0; i < mentitys.length; i++){
                MediaEntity mentity = mentitys[i];
                list.add(mentity.getMediaURL());
            }
        }
        
        final String[] items = (String[])list.toArray(new String[0]);
        
        LayoutInflater title_inf = LayoutInflater.from(parent.getContext());
        View dialog_title = title_inf.inflate(R.layout.list_item_tweet, null);
        SmartImageView icon = (SmartImageView)dialog_title.findViewById(R.id.icon);
        TextView name_screenName = (TextView)dialog_title.findViewById(R.id.name_screenName);
        TextView tweetText = (TextView)dialog_title.findViewById(R.id.tweetText);
        TextView tweetDate = (TextView)dialog_title.findViewById(R.id.tweet_date);
        
        tweetDate.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(item.getCreatedAt())
					+ "  via " + item.getSource().replaceAll("<.+?>", ""));
        if(item.isRetweet()){
        	tweetText.setText(item.getRetweetedStatus().getText());
        	name_screenName.setText(item.getRetweetedStatus().getUser().getName() + " - @" + item.getRetweetedStatus().getUser().getScreenName());
        	icon.setImageUrl(item.getRetweetedStatus().getUser().getProfileImageURL());
        }else{
        	tweetText.setText(item.getText());
        	name_screenName.setText(item.getUser().getName() + " - @" + item.getUser().getScreenName());
        	icon.setImageUrl(item.getUser().getProfileImageURL());
        }
        
		AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());
		builder.setCustomTitle(dialog_title)
		.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				
				if(items[which].equals("返信")){
					Intent reply = new Intent(parent.getContext(), TweetActivity.class);
					if(item.isRetweet()){
						reply.putExtra("ReplyUserScreenName", item.getRetweetedStatus().getUser().getScreenName());
						reply.putExtra("TweetReplyId", item.getRetweetedStatus().getId());
						reply.putExtra("ReplyTweetText", item.getRetweetedStatus().getText());
					}else{
						reply.putExtra("ReplyUserScreenName", item.getUser().getScreenName());
						reply.putExtra("TweetReplyId", item.getId());
						reply.putExtra("ReplyTweetText", item.getText());
					}
					parent.getContext().startActivity(reply);
				}
				
				if(items[which].equals("リツイート")){
					AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>(){
						@Override
						protected Boolean doInBackground(Void... params) {
							try {
								MainActivity.twitter.retweetStatus(item.getId());
								return true;
							} catch (TwitterException e) {
								return false;
							}
						}
						protected void onPostExecute(Boolean result) {
							if(result)
								new MainActivity().showToast("リツイートしました", parent.getContext());
							else
								new MainActivity().showToast("リツイートできませんでした", parent.getContext());
						}
					};
					task.execute();
				}
				
				if(items[which].equals("ふぁぼる")){
					AsyncTask<Void, Void, Boolean> fav = new AsyncTask<Void, Void, Boolean>(){

						@Override
						protected Boolean doInBackground(Void... params) {
							try {
								MainActivity.twitter.createFavorite(item.getId());
								return true;
							} catch (TwitterException e) {
								return false;
							}
						}
						protected void onPostExecute(Boolean result) {
							if(result)
								new MainActivity().showToast("ふぁぼりました", parent.getContext());
							else
								new MainActivity().showToast("ふぁぼれませんでした", parent.getContext());
						}
					};
					fav.execute();
				}
				
				if(items[which].equals("正規表現で抽出")){
					AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());
					final EditText reg = new EditText(parent.getContext());
					reg.setText(pref.getString("regularExpression", ""));
					builder.setTitle("正規表現を入力してください")
					.setView(reg)
					.setPositiveButton("OK", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String editReg = reg.getText().toString();
							pref.edit().putString("regularExpression", editReg).commit();
							CustomAdapter content = new CustomAdapter(parent.getContext());
							for(int i = 0; parent.getCount() - 1 > i; i++){
								Status status = ((Status) parent.getAdapter().getItem(i));
								if(status.getText()
										.toString()
										.matches(editReg))
									content.add(status);
							}
							AlertDialog.Builder b = new AlertDialog.Builder(parent.getContext());
							ListView l = new ListView(parent.getContext());
							if(content.isEmpty())
								l.setAdapter(new ArrayAdapter<String>(parent.getContext(),android.R.layout.simple_list_item_1, new String[]{"なし"}));
							else{
								l.setAdapter(content);
								l.setOnItemClickListener(new ListViewListener());
								l.setOnItemLongClickListener(new ListViewListener());
							}
							b.setView(l).create().show();
						}
					});
					builder.setNegativeButton("キャンセル", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
					builder.create().show();
				}
				
				if(items[which].startsWith("http") || items[which].startsWith("ftp")){
					Intent web = new Intent(Intent.ACTION_VIEW, Uri.parse(items[which]));
					parent.getContext().startActivity(web);
				}
				
				if(items[which].startsWith("@")){ //UserPage
					Intent intent = new Intent(parent.getContext(), UserPage.class);
					intent.putExtra("userScreenName", items[which].substring(1));
					parent.getContext().startActivity(intent);
				}
			}
		});
		builder.create().show();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
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