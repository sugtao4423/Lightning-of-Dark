package sugtao4423.lod;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.image.SmartImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import sugtao4423.lod.dialog_onclick.Dialog_ListClick;
import sugtao4423.lod.dialog_onclick.Dialog_deletePost;
import sugtao4423.lod.dialog_onclick.Dialog_favorite;
import sugtao4423.lod.dialog_onclick.Dialog_quoteRT;
import sugtao4423.lod.dialog_onclick.Dialog_reply;
import sugtao4423.lod.dialog_onclick.Dialog_retweet;
import sugtao4423.lod.dialog_onclick.Dialog_talk;
import sugtao4423.lod.dialog_onclick.Dialog_unOfficialRT;
import sugtao4423.lod.tweetlistview.TweetListAdapter.OnItemClickListener;
import sugtao4423.lod.tweetlistview.TweetListAdapter.OnItemLongClickListener;
import sugtao4423.lod.utils.Utils;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

public class ListViewListener implements OnItemClickListener, OnItemLongClickListener{

    @Override
    public void onItemClicked(final Context context, ArrayList<Status> data, int position){
        Status item = data.get(position);
        App app = (App)context.getApplicationContext();

        ArrayAdapter<String> list = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1);
        if(app.getOptions().getIsRegex()){
            list.add("正規表現で抽出");
        }
        if(app.getOptions().getIsOpenBrowser()){
            list.add("ブラウザで開く");
        }

        ArrayList<String> users = new ArrayList<String>();
        users.add("@" + item.getUser().getScreenName());

        UserMentionEntity[] mentionEntitys = item.getUserMentionEntities();
        if(mentionEntitys != null && mentionEntitys.length > 0){
            for(UserMentionEntity menty : mentionEntitys){
                if(users.indexOf("@" + menty.getScreenName()) == -1){
                    users.add("@" + menty.getScreenName());
                }
            }
        }
        list.addAll(users);

        URLEntity[] uentitys = item.getURLEntities();
        if(uentitys != null && uentitys.length > 0){
            for(URLEntity u : uentitys){
                list.add(u.getExpandedURL());
            }
        }

        MediaEntity[] mentitys = item.getMediaEntities();
        if(mentitys != null && mentitys.length > 0){
            for(MediaEntity media : mentitys){
                if(Utils.isVideoOrGif(media)){
                    String[] videoUrls = Utils.getVideoURLsSortByBitrate(app, mentitys);
                    if(videoUrls.length == 0){
                        list.add("ビデオの取得に失敗");
                    }else{
                        list.add(videoUrls[videoUrls.length - 1]);
                    }
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
        i.putExtra(TweetActivity.INTENT_EXTRA_KEY_STATUS, data.get(position));
        context.startActivity(i);
        return true;
    }

    private View dialog_title;
    private SmartImageView icon;
    private TextView name_screenName, tweetText, tweetDate, protect;
    private SimpleDateFormat statusDateFormat;

    private View content;
    private ListView dialog_list;
    private Button[] dialogBtn;

    private AlertDialog dialog;

    public void createDialog(Context context){
        dialog_title = View.inflate(context, R.layout.list_item_tweet, null);
        icon = (SmartImageView)dialog_title.findViewById(R.id.icon);
        name_screenName = (TextView)dialog_title.findViewById(R.id.name_screenName);
        tweetText = (TextView)dialog_title.findViewById(R.id.tweetText);
        tweetDate = (TextView)dialog_title.findViewById(R.id.tweet_date);
        statusDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss" + (((App)context.getApplicationContext()).getOptions().getIsMillisecond() ? ".SSS" : ""), Locale.getDefault());
        protect = (TextView)dialog_title.findViewById(R.id.UserProtected);
        ((HorizontalScrollView)dialog_title.findViewById(R.id.tweet_images_scroll)).setVisibility(View.GONE);

        content = View.inflate(context, R.layout.custom_dialog, null);
        dialog_list = (ListView)content.findViewById(R.id.dialog_List);
        dialogBtn = new Button[6];
        dialogBtn[0] = (Button)content.findViewById(R.id.dialog_reply);
        dialogBtn[1] = (Button)content.findViewById(R.id.dialog_retweet);
        dialogBtn[2] = (Button)content.findViewById(R.id.dialog_unofficialRT);
        dialogBtn[3] = (Button)content.findViewById(R.id.dialog_favorite);
        dialogBtn[4] = (Button)content.findViewById(R.id.dialog_talk);
        dialogBtn[5] = (Button)content.findViewById(R.id.dialog_delete);

        Typeface tf = ((App)context.getApplicationContext()).getFontAwesomeTypeface();
        float density = context.getResources().getDisplayMetrics().density;
        int black = Color.parseColor(context.getString(R.color.icon));
        for(Button btn : dialogBtn){
            btn.setTypeface(tf);
            btn.setTextSize(9 * density);
            btn.setTextColor(black);
        }
        protect.setTypeface(tf);

        dialog = new AlertDialog.Builder(context).setCustomTitle(dialog_title).setView(content).create();
    }

    public void showDialog(final Context context, Status status, ArrayList<Status> allStatusData, ArrayAdapter<String> listStrings){
        if(dialog == null){
            createDialog(context);
        }

        if(!status.getUser().isProtected()){
            protect.setVisibility(View.GONE);
        }else{
            protect.setVisibility(View.VISIBLE);
        }
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

        dialogBtn[0].setOnClickListener(new Dialog_reply(status, context, dialog));
        dialogBtn[1].setOnClickListener(new Dialog_retweet(status, context, dialog));
        dialogBtn[1].setOnLongClickListener(new Dialog_quoteRT(status, context, dialog));
        dialogBtn[2].setOnClickListener(new Dialog_unOfficialRT(status, context, dialog));
        dialogBtn[3].setOnClickListener(new Dialog_favorite(status, context, dialog));
        dialogBtn[4].setOnClickListener(new Dialog_talk(status, context, dialog));
        dialogBtn[5].setOnClickListener(new Dialog_deletePost(status, context, dialog));

        dialogBtn[4].setEnabled(status.getInReplyToStatusId() > 0);
        dialogBtn[5].setEnabled(status.getUser().getScreenName().equals(((App)context.getApplicationContext()).getCurrentAccount().getScreenName()));
    }

}