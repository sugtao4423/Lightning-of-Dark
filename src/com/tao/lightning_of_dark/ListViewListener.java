package com.tao.lightning_of_dark;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
	
	static twitter4j.Status reply;
	
	@Override
	public void onItemClick(final AdapterView<?> parent, View view, int position, long id) {
		final Status item = (Status)parent.getItemAtPosition(position);
		
		List<String> list = new ArrayList<String>();
		
		if(MainActivity.menu_reply)
			list.add("返信");
		if(MainActivity.menu_retweet)
			list.add("リツイート");
		if(MainActivity.menu_InformalRetweet)
			list.add("非公式RT");
		if(MainActivity.menu_fav)
			list.add("ふぁぼる");
		if(MainActivity.menu_regex)
			list.add("正規表現で抽出");
		if(MainActivity.menu_talk)
			if(item.isRetweet()){
				if(item.getRetweetedStatus().getInReplyToStatusId() > 0)
					list.add("会話を表示");
			}else{
				if(item.getInReplyToStatusId() > 0)
					list.add("会話を表示");
			}
				
		list.add("@" + item.getUser().getScreenName());
		
		UserMentionEntity[] mentionEntitys = item.getUserMentionEntities();
		if(mentionEntitys != null && mentionEntitys.length > 0){
			for(int i = 0; i < mentionEntitys.length; i++){
				UserMentionEntity umEntity = mentionEntitys[i];
				if(!umEntity.getScreenName().equals(item.getUser().getScreenName()))
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
				
				if(items[which].equals("非公式RT")){
					String RTtext;
					if(item.isRetweet())
						RTtext = " RT @" + item.getRetweetedStatus().getUser().getScreenName() + ": " + item.getRetweetedStatus().getText();
					else
						RTtext = " RT @" + item.getUser().getScreenName() + ": " + item.getText();
					Intent i = new Intent(parent.getContext(), TweetActivity.class);
					i.putExtra("pakuri", RTtext).putExtra("do_setSelection", false);
					parent.getContext().startActivity(i);
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
					final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(parent.getContext());
					reg.setText(pref.getString("regularExpression", ""));
					builder.setTitle("正規表現を入力してください")
					.setView(reg)
					.setPositiveButton("OK", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String editReg = reg.getText().toString();
							pref.edit().putString("regularExpression", editReg).commit();
							Pattern pattern = Pattern.compile(editReg, Pattern.DOTALL);
							CustomAdapter content = new CustomAdapter(parent.getContext());
							for(int i = 0; parent.getCount() - 1 > i; i++){
								Status status = ((Status) parent.getAdapter().getItem(i));
								if(pattern.matcher(status.getText()).find())
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
				if(items[which].equals("会話を表示")){
					AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());
					ListView result = new ListView(parent.getContext());
					result.setOnItemClickListener(new ListViewListener());
					result.setOnItemLongClickListener(new ListViewListener());
					final CustomAdapter resultAdapter = new CustomAdapter(parent.getContext());
					result.setAdapter(resultAdapter);
					builder.setView(result);
					
					if(item.isRetweet())
						reply = item.getRetweetedStatus();
					else
						reply = item;
					final List<twitter4j.Status> StatusList = new ArrayList<twitter4j.Status>();
					
					AsyncTask<Void, Void, Boolean> getReply = new AsyncTask<Void, Void, Boolean>(){
						@Override
						protected Boolean doInBackground(Void... params) {
							try {
								for(; reply.getInReplyToStatusId() > 0;){
									reply = MainActivity.twitter.showStatus(reply.getInReplyToStatusId());
									StatusList.add(reply);
								}
								return true;
							} catch (TwitterException e) {
								return false;
							}
						}
						protected void onPostExecute(Boolean result) {
							if(result){
								if(item.isRetweet())
									resultAdapter.add(item.getRetweetedStatus());
								else
									resultAdapter.add(item);
								for(twitter4j.Status status : StatusList)
									resultAdapter.add(status);
							}else
								new MainActivity().showToast("会話の取得完了", parent.getContext());
						}
					};
					getReply.execute();
					builder.create().show();
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