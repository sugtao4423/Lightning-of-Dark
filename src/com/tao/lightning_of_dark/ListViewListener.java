package com.tao.lightning_of_dark;

import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

import com.loopj.android.image.SmartImageView;

import dialog_onClick.Dialog_deletePost;
import dialog_onClick.Dialog_favorite;
import dialog_onClick.Dialog_reply;
import dialog_onClick.Dialog_retweet;
import dialog_onClick.Dialog_talk;
import dialog_onClick.Dialog_unOfficialRT;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;


public class ListViewListener implements OnItemClickListener, OnItemLongClickListener {
	
	public static AlertDialog dialog;
	
	@Override
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, long id) {
		final Status item = (Status)parent.getItemAtPosition(position);
		
		ArrayAdapter<String> list = new ArrayAdapter<String>(parent.getContext(), android.R.layout.simple_list_item_1);
		if(MainActivity.option_regex)
			list.add("正規表現で抽出");
				
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

        //ダイアログタイトルinflate
        View dialog_title = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_tweet, null);
        SmartImageView icon = (SmartImageView)dialog_title.findViewById(R.id.icon);
        TextView name_screenName = (TextView)dialog_title.findViewById(R.id.name_screenName);
        TextView tweetText = (TextView)dialog_title.findViewById(R.id.tweetText);
        TextView tweetDate = (TextView)dialog_title.findViewById(R.id.tweet_date);
        ImageView protect = (ImageView)dialog_title.findViewById(R.id.UserProtected);
        
        if(item.isRetweet()){
            if(!item.getRetweetedStatus().getUser().isProtected())
        		protect.setVisibility(View.GONE);
        	else
        		protect.setVisibility(View.VISIBLE);
        	tweetText.setText(item.getRetweetedStatus().getText());
        	name_screenName.setText(item.getRetweetedStatus().getUser().getName() + " - @" + item.getRetweetedStatus().getUser().getScreenName());
        	tweetDate.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(item.getCreatedAt())
					+ "  via " + item.getRetweetedStatus().getSource().replaceAll("<.+?>", ""));
        	icon.setImageUrl(item.getRetweetedStatus().getUser().getProfileImageURL());
        }else{
            if(!item.getUser().isProtected())
        		protect.setVisibility(View.GONE);
        	else
        		protect.setVisibility(View.VISIBLE);
        	tweetText.setText(item.getText());
        	name_screenName.setText(item.getUser().getName() + " - @" + item.getUser().getScreenName());
        	tweetDate.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(item.getCreatedAt())
					+ "  via " + item.getSource().replaceAll("<.+?>", ""));
        	icon.setImageUrl(item.getUser().getProfileImageURL());
        }
        //ここまで
        
        //ダイアログ本文inflate
        View content = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_dialog, null);
        ListView dialog_list = (ListView)content.findViewById(R.id.dialog_List);
        ImageButton dialog_reply = (ImageButton)content.findViewById(R.id.dialog_reply);
        ImageButton dialog_retweet = (ImageButton)content.findViewById(R.id.dialog_retweet);
        ImageButton dialog_unOfficialRT = (ImageButton)content.findViewById(R.id.dialog_unofficialRT);
        ImageButton dialog_favorite = (ImageButton)content.findViewById(R.id.dialog_favorite);
        ImageButton dialog_talk = (ImageButton)content.findViewById(R.id.dialog_talk);
        ImageButton dialog_deletePost = (ImageButton)content.findViewById(R.id.dialog_delete);
        
        
		dialog_list.setAdapter(list);
        dialog_list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent2, View view2,
					int position2, long id2) {
				String clickedText = (String)parent2.getItemAtPosition(position2);
				
				if(clickedText.equals("正規表現で抽出")){
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
				
				if(clickedText.startsWith("http") || clickedText.startsWith("ftp")){
					Intent web = new Intent(Intent.ACTION_VIEW, Uri.parse(clickedText));
					parent.getContext().startActivity(web);
				}
				
				if(clickedText.startsWith("@")){ //UserPage
					Intent intent = new Intent(parent.getContext(), UserPage.class);
					intent.putExtra("userScreenName", clickedText.substring(1));
					parent.getContext().startActivity(intent);
				}
			}
		});
        
        dialog_reply.setOnClickListener(new Dialog_reply(item, parent.getContext()));
        dialog_retweet.setOnClickListener(new Dialog_retweet(item, parent.getContext()));
        dialog_unOfficialRT.setOnClickListener(new Dialog_unOfficialRT(item, parent.getContext()));
        dialog_favorite.setOnClickListener(new Dialog_favorite(item, parent.getContext()));
        dialog_talk.setOnClickListener(new Dialog_talk(item, parent.getContext()));
        dialog_deletePost.setOnClickListener(new Dialog_deletePost(item, parent.getContext()));
        
        if(item.isRetweet()){
        	if(!(item.getRetweetedStatus().getInReplyToStatusId() > 0)){
        		dialog_talk.setEnabled(false);
        		dialog_talk.setBackgroundColor(Color.parseColor("#a7a7a7"));
        	}
        	if(! item.getRetweetedStatus().getUser().getScreenName().equals(MainActivity.MyScreenName)){
        		dialog_deletePost.setEnabled(false);
        		dialog_deletePost.setBackgroundColor(Color.parseColor("#a7a7a7"));
        	}
        }else{
        	if(!(item.getInReplyToStatusId() > 0)){
        		dialog_talk.setEnabled(false);
        		dialog_talk.setBackgroundColor(Color.parseColor("#a7a7a7"));
        	}
        	if(! item.getUser().getScreenName().equals(MainActivity.MyScreenName)){
        		dialog_deletePost.setEnabled(false);
        		dialog_deletePost.setBackgroundColor(Color.parseColor("#a7a7a7"));
        	}
        }
        
		AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());
		builder.setCustomTitle(dialog_title).setView(content);
		
		dialog = builder.create();
		dialog.show();
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