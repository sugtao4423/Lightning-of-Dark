package sugtao4423.lod;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import com.loopj.android.image.SmartImageView;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import sugtao4423.lod.dialog_onclick.Dialog_ListClick;
import sugtao4423.lod.dialog_onclick.Dialog_deletePost;
import sugtao4423.lod.dialog_onclick.Dialog_favorite;
import sugtao4423.lod.dialog_onclick.Dialog_quoteRT;
import sugtao4423.lod.dialog_onclick.Dialog_reply;
import sugtao4423.lod.dialog_onclick.Dialog_retweet;
import sugtao4423.lod.dialog_onclick.Dialog_talk;
import sugtao4423.lod.dialog_onclick.Dialog_unOfficialRT;
import sugtao4423.lod.dialog_onclick.StatusItem;
import sugtao4423.lod.tweetlistview.TweetListAdapter.OnItemClickListener;
import sugtao4423.lod.tweetlistview.TweetListAdapter.OnItemLongClickListener;
import sugtao4423.lod.utils.Utils;

public class ListViewListener implements OnItemClickListener, OnItemLongClickListener{

	@Override
	public void onItemClicked(final Context context, ArrayList<Status> data, int position){
		Status item = data.get(position);
		ApplicationClass appClass = (ApplicationClass)context.getApplicationContext();

		ArrayAdapter<String> list = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1);
		if(appClass.getOption_regex())
			list.add("正規表現で抽出");
		if(appClass.getOption_openBrowser())
			list.add("ブラウザで開く");

		ArrayList<String> users = new ArrayList<String>();
		users.add("@" + item.getUser().getScreenName());

		UserMentionEntity[] mentionEntitys = item.getUserMentionEntities();
		if(mentionEntitys != null && mentionEntitys.length > 0){
			for(UserMentionEntity menty : mentionEntitys){
				if(users.indexOf("@" + menty.getScreenName()) == -1)
					users.add("@" + menty.getScreenName());
			}
		}
		list.addAll(users);

		URLEntity[] uentitys = item.getURLEntities();
		if(uentitys != null && uentitys.length > 0){
			for(URLEntity u : uentitys)
				list.add(u.getExpandedURL());
		}

		MediaEntity[] mentitys = item.getMediaEntities();
		if(mentitys != null && mentitys.length > 0){
			for(MediaEntity media : mentitys){
				if(Utils.isVideoOrGif(media)){
					String[] videoUrls = Utils.getVideoURLsSortByBitrate(appClass, mentitys);
					if(videoUrls.length == 0)
						list.add("ビデオの取得に失敗");
					else
						list.add(videoUrls[videoUrls.length - 1]);
				}else{
					list.add(media.getMediaURL());
				}
			}
		}

		Status status = item.isRetweet() ? item.getRetweetedStatus() : item;

		// ダイアログタイトルinflate
		View dialog_title = View.inflate(context, R.layout.list_item_tweet, null);
		SmartImageView icon = (SmartImageView)dialog_title.findViewById(R.id.icon);
		TextView name_screenName = (TextView)dialog_title.findViewById(R.id.name_screenName);
		TextView tweetText = (TextView)dialog_title.findViewById(R.id.tweetText);
		TextView tweetDate = (TextView)dialog_title.findViewById(R.id.tweet_date);
		ImageView protect = (ImageView)dialog_title.findViewById(R.id.UserProtected);
		((HorizontalScrollView)dialog_title.findViewById(R.id.tweet_images_scroll)).setVisibility(View.GONE);

		if(!status.getUser().isProtected())
			protect.setVisibility(View.GONE);
		else
			protect.setVisibility(View.VISIBLE);
		tweetText.setText(status.getText());
		name_screenName.setText(status.getUser().getName() + " - @" + status.getUser().getScreenName());
		tweetDate.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPANESE).format(status.getCreatedAt()) + "  via "
				+ status.getSource().replaceAll("<.+?>", ""));
		icon.setImageUrl(status.getUser().getBiggerProfileImageURL(), null, R.drawable.ic_action_refresh);
		// ここまで

		// ダイアログ本文inflate
		View content = View.inflate(context, R.layout.custom_dialog, null);
		ListView dialog_list = (ListView)content.findViewById(R.id.dialog_List);
		ImageButton dialog_reply = (ImageButton)content.findViewById(R.id.dialog_reply);
		ImageButton dialog_retweet = (ImageButton)content.findViewById(R.id.dialog_retweet);
		ImageButton dialog_unOfficialRT = (ImageButton)content.findViewById(R.id.dialog_unofficialRT);
		ImageButton dialog_favorite = (ImageButton)content.findViewById(R.id.dialog_favorite);
		ImageButton dialog_talk = (ImageButton)content.findViewById(R.id.dialog_talk);
		ImageButton dialog_deletePost = (ImageButton)content.findViewById(R.id.dialog_delete);

		final AlertDialog dialog = new AlertDialog.Builder(context)
				.setCustomTitle(dialog_title)
				.setView(content).show();

		dialog_list.setAdapter(list);
		dialog_list.setOnItemClickListener(new Dialog_ListClick(context, item, data, dialog));
		dialog_list.setOnItemLongClickListener(new android.widget.AdapterView.OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id){
				String clickedText = (String)parent.getItemAtPosition(position);
				if(clickedText.startsWith("http")){
					dialog.dismiss();
					new ChromeIntent(context, Uri.parse(clickedText));
				}
				return true;
			}
		});

		dialog_reply.setOnClickListener(new Dialog_reply(item, context, dialog));
		dialog_retweet.setOnClickListener(new Dialog_retweet(item, context, dialog));
		dialog_retweet.setOnLongClickListener(new Dialog_quoteRT(item, context, dialog));
		dialog_unOfficialRT.setOnClickListener(new Dialog_unOfficialRT(item, context, dialog));
		dialog_favorite.setOnClickListener(new Dialog_favorite(item, context, dialog));
		dialog_talk.setOnClickListener(new Dialog_talk(item, context, dialog));
		dialog_deletePost.setOnClickListener(new Dialog_deletePost(item, context, dialog));

		if(!(status.getInReplyToStatusId() > 0)){
			dialog_talk.setEnabled(false);
			dialog_talk.setBackgroundColor(Color.parseColor(context.getString(R.color.grayout)));
		}
		if(!status.getUser().getScreenName().equals(((ApplicationClass)context.getApplicationContext()).getMyScreenName())){
			dialog_deletePost.setEnabled(false);
			dialog_deletePost.setBackgroundColor(Color.parseColor(context.getString(R.color.grayout)));
		}
	}

	@Override
	public boolean onItemLongClicked(Context context, ArrayList<Status> data, int position){
		Intent i = new Intent(context, TweetActivity.class);
		i.putExtra(TweetActivity.INTENT_EXTRA_KEY_TYPE, TweetActivity.TYPE_PAKUTSUI);
		i.putExtra(TweetActivity.INTENT_EXTRA_KEY_STATUS, new StatusItem(data.get(position)));
		context.startActivity(i);
		return true;
	}
}