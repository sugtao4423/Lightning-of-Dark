package sugtao4423.lod;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

import sugtao4423.lod.dialog_onclick.Dialog_ListClick;
import sugtao4423.lod.dialog_onclick.Dialog_deletePost;
import sugtao4423.lod.dialog_onclick.Dialog_favorite;
import sugtao4423.lod.dialog_onclick.Dialog_quoteRT;
import sugtao4423.lod.dialog_onclick.Dialog_reply;
import sugtao4423.lod.dialog_onclick.Dialog_retweet;
import sugtao4423.lod.dialog_onclick.Dialog_talk;
import sugtao4423.lod.dialog_onclick.Dialog_unOfficialRT;
import sugtao4423.lod.tweetlistview.TweetListAdapter;
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
            list.add(context.getString(R.string.extract_with_regex));
        }
        if(app.getOptions().getIsOpenBrowser()){
            list.add(context.getString(R.string.open_in_browser));
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
                        list.add(context.getString(R.string.error_get_video));
                    }else{
                        list.add(videoUrls[videoUrls.length - 1]);
                    }
                }else{
                    list.add(media.getMediaURLHttps());
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

    private TweetListAdapter adapter;

    private View content;
    private ListView dialogList;
    private Button[] dialogBtn;

    private AlertDialog dialog;

    public void createDialog(Context context){
        RecyclerView tweetListView = new RecyclerView(context);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        tweetListView.setLayoutManager(llm);
        adapter = new TweetListAdapter(context);
        adapter.setHideImages(true);
        tweetListView.setAdapter(adapter);

        content = View.inflate(context, R.layout.custom_dialog, null);
        dialogList = (ListView)content.findViewById(R.id.dialog_List);
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

        dialog = new AlertDialog.Builder(context).setCustomTitle(tweetListView).setView(content).create();
    }

    public void showDialog(final Context context, Status status, ArrayList<Status> allStatusData, ArrayAdapter<String> listStrings){
        if(dialog == null){
            createDialog(context);
        }
        adapter.clear();
        adapter.add(status);
        dialog.show();

        dialogList.setAdapter(listStrings);
        dialogList.setOnItemClickListener(new Dialog_ListClick(context, status, allStatusData, dialog));
        dialogList.setOnItemLongClickListener(new android.widget.AdapterView.OnItemLongClickListener(){
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