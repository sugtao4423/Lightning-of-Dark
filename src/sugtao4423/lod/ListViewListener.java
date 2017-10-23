package sugtao4423.lod;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import com.loopj.android.image.SmartImageView;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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
		showDialog(context, status, data, list);
	}

	@Override
	public boolean onItemLongClicked(Context context, ArrayList<Status> data, int position){
		Intent i = new Intent(context, TweetActivity.class);
		i.putExtra(TweetActivity.INTENT_EXTRA_KEY_TYPE, TweetActivity.TYPE_PAKUTSUI);
		i.putExtra(TweetActivity.INTENT_EXTRA_KEY_STATUS, new StatusItem(data.get(position)));
		context.startActivity(i);
		return true;
	}

	private View dialog_title;
	private SmartImageView icon;
	private TextView name_screenName, tweetText, tweetDate;
	private SimpleDateFormat statusDateFormat;
	private ImageView protect;

	private View content;
	private ListView dialog_list;
	private ImageButton dialog_reply, dialog_retweet, dialog_unOfficialRT, dialog_favorite, dialog_talk, dialog_deletePost;

	private AlertDialog dialog;

	public void createDialog(Context context){
		dialog_title = View.inflate(context, R.layout.list_item_tweet, null);
		icon = (SmartImageView)dialog_title.findViewById(R.id.icon);
		name_screenName = (TextView)dialog_title.findViewById(R.id.name_screenName);
		tweetText = (TextView)dialog_title.findViewById(R.id.tweetText);
		tweetDate = (TextView)dialog_title.findViewById(R.id.tweet_date);
		statusDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss" + (((ApplicationClass)context.getApplicationContext()).getOption_millisecond() ? ".SSS" : ""), Locale.getDefault());
		protect = (ImageView)dialog_title.findViewById(R.id.UserProtected);
		((HorizontalScrollView)dialog_title.findViewById(R.id.tweet_images_scroll)).setVisibility(View.GONE);

		content = View.inflate(context, R.layout.custom_dialog, null);
		dialog_list = (ListView)content.findViewById(R.id.dialog_List);
		dialog_reply = (ImageButton)content.findViewById(R.id.dialog_reply);
		dialog_retweet = (ImageButton)content.findViewById(R.id.dialog_retweet);
		dialog_unOfficialRT = (ImageButton)content.findViewById(R.id.dialog_unofficialRT);
		dialog_favorite = (ImageButton)content.findViewById(R.id.dialog_favorite);
		dialog_talk = (ImageButton)content.findViewById(R.id.dialog_talk);
		dialog_deletePost = (ImageButton)content.findViewById(R.id.dialog_delete);

		dialog = new AlertDialog.Builder(context).setCustomTitle(dialog_title).setView(content).create();
	}

	public void showDialog(final Context context, Status status, ArrayList<Status> allStatusData, ArrayAdapter<String> listStrings){
		if(dialog == null)
			createDialog(context);

		if(!status.getUser().isProtected())
			protect.setVisibility(View.GONE);
		else
			protect.setVisibility(View.VISIBLE);
		tweetText.setText(status.getText());
		name_screenName.setText(status.getUser().getName() + " - @" + status.getUser().getScreenName());
		String date = statusDateFormat.format(new Date((status.getId() >> 22) + 1288834974657L));
		tweetDate.setText(date + "  via " + status.getSource().replaceAll("<.+?>", ""));
		icon.setImageUrl(status.getUser().getBiggerProfileImageURL(), null, R.drawable.ic_action_refresh);

		dialog.show();

		dialog_list.setAdapter(listStrings);
		dialog_list.setOnItemClickListener(new Dialog_ListClick(context, status, allStatusData, dialog));
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

		dialog_reply.setOnClickListener(new Dialog_reply(status, context, dialog));
		dialog_retweet.setOnClickListener(new Dialog_retweet(status, context, dialog));
		dialog_retweet.setOnLongClickListener(new Dialog_quoteRT(status, context, dialog));
		dialog_unOfficialRT.setOnClickListener(new Dialog_unOfficialRT(status, context, dialog));
		dialog_favorite.setOnClickListener(new Dialog_favorite(status, context, dialog));
		dialog_talk.setOnClickListener(new Dialog_talk(status, context, dialog));
		dialog_deletePost.setOnClickListener(new Dialog_deletePost(status, context, dialog));

		dialog_talk.setEnabled(status.getInReplyToStatusId() > 0);
		dialog_deletePost.setEnabled(status.getUser().getScreenName().equals(((ApplicationClass)context.getApplicationContext()).getMyScreenName()));
	}

}