package sugtao4423.lod.dialog_onclick;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sugtao4423.lod.ChromeIntent;
import sugtao4423.lod.IntentActivity;
import sugtao4423.lod.Keys;
import sugtao4423.lod.ListViewListener;
import sugtao4423.lod.R;
import sugtao4423.lod.ShowToast;
import sugtao4423.lod.Show_Video;
import sugtao4423.lod.swipe_image_viewer.ImageFragmentActivity;
import sugtao4423.lod.tweetlistview.TweetListAdapter;
import sugtao4423.lod.tweetlistview.TweetListView;
import sugtao4423.lod.userpage_fragment.UserPage;
import sugtao4423.lod.utils.Regex;
import sugtao4423.lod.utils.Utils;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.User;

public class Dialog_ListClick implements OnItemClickListener{

    private Context context;
    private Status status;
    private ArrayList<Status> listData;
    private AlertDialog dialog;

    public Dialog_ListClick(Context context, Status status, ArrayList<Status> listData, AlertDialog dialog){
        this.context = context;
        this.status = status;
        this.listData = listData;
        this.dialog = dialog;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        dialog.dismiss();
        String clickedText = (String)parent.getItemAtPosition(position);

        if(clickedText.equals(context.getString(R.string.extract_with_regex))){
            View regView = View.inflate(context, R.layout.reg_dialog, null);
            final EditText regEdit = (EditText)regView.findViewById(R.id.regDialog_edit);
            GridLayout gridLayout = (GridLayout)regView.findViewById(R.id.regDialog_grid);
            String[] regItems = new String[]{".", "*", "|", "+", "?", "\\", "^", "$", "(", ")", "[", "]", "{", "}"};
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(130, 130);
            for(int i = 0; i < regItems.length; i++){
                Button btn = new Button(context);
                btn.setLayoutParams(params);
                btn.setText(regItems[i]);
                btn.setOnClickListener(new Dialog_regButtonClick(regEdit));
                gridLayout.addView(btn);
            }

            final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            regEdit.setText(pref.getString(Keys.REGULAR_EXPRESSION, ""));
            new AlertDialog.Builder(context)
                    .setTitle(R.string.input_regex)
                    .setView(regView)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.ok, new OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which){
                            InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(regEdit.getWindowToken(), 0);

                            String editReg = regEdit.getText().toString();
                            pref.edit().putString(Keys.REGULAR_EXPRESSION, editReg).commit();
                            Pattern pattern = Pattern.compile(editReg, Pattern.DOTALL);
                            TweetListAdapter adapter = new TweetListAdapter(context);
                            int find = 0;
                            for(Status s : listData){
                                if(pattern.matcher(s.getText()).find()){
                                    adapter.add(s);
                                    find++;
                                }
                            }
                            if(find == 0){
                                new ShowToast(context.getApplicationContext(), R.string.nothing);
                            }else{
                                TweetListView l = new TweetListView(context);
                                l.setAdapter(adapter);
                                adapter.setOnItemClickListener(new ListViewListener());
                                adapter.setOnItemLongClickListener(new ListViewListener());
                                new AlertDialog.Builder(context).setView(l).show();
                                String resultCount = context.getString(R.string.param_regex_result_count, listData.size(), find);
                                new ShowToast(context.getApplicationContext(), resultCount, Toast.LENGTH_LONG);
                            }
                        }
                    }).show();
        }else if(clickedText.startsWith("http") || clickedText.startsWith("ftp")){
            Matcher image = Regex.media_image.matcher(clickedText);
            Matcher video = Regex.media_video.matcher(clickedText);
            Matcher gif = Regex.media_gif.matcher(clickedText);
            Matcher state = Regex.statusUrl.matcher(clickedText);
            Intent intent;
            if(image.find()){
                ArrayList<String> urls = new ArrayList<String>();
                MediaEntity[] mentitys = status.getMediaEntities();
                if(mentitys != null && mentitys.length > 0){
                    for(MediaEntity media : mentitys){
                        if(!Utils.isVideoOrGif(media))
                            urls.add(media.getMediaURLHttps());
                    }
                }
                int pos = urls.indexOf(clickedText);
                String[] arr = (String[])urls.toArray(new String[0]);
                intent = new Intent(context, ImageFragmentActivity.class);
                intent.putExtra(ImageFragmentActivity.INTENT_EXTRA_KEY_URLS, arr);
                intent.putExtra(ImageFragmentActivity.INTENT_EXTRA_KEY_POSITION, pos);
            }else if(video.find()){
                intent = new Intent(context, Show_Video.class);
                intent.putExtra(Show_Video.INTENT_EXTRA_KEY_URL, clickedText);
                intent.putExtra(Show_Video.INTENT_EXTRA_KEY_TYPE, Show_Video.TYPE_VIDEO);
            }else if(gif.find()){
                intent = new Intent(context, Show_Video.class);
                intent.putExtra(Show_Video.INTENT_EXTRA_KEY_URL, clickedText);
                intent.putExtra(Show_Video.INTENT_EXTRA_KEY_TYPE, Show_Video.TYPE_GIF);
            }else if(state.find()){
                intent = new Intent(context, IntentActivity.class);
                intent.putExtra(IntentActivity.TWEET_ID, Long.parseLong(state.group(Regex.statusUrlStatusIdGroup)));
            }else{
                new ChromeIntent(context, Uri.parse(clickedText));
                return;
            }
            context.startActivity(intent);
        }else if(clickedText.equals(context.getString(R.string.open_in_browser))){
            Status orig = status.isRetweet() ? status.getRetweetedStatus() : status;
            String tweet_sn = orig.getUser().getScreenName();
            String tweet_id = String.valueOf(orig.getId());
            String url = "https://twitter.com/" + tweet_sn + "/status/" + tweet_id;
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        }else if(clickedText.startsWith("@")){ // UserPage
            Intent intent = new Intent(context, UserPage.class);
            String usersn = clickedText.substring(1);
            User user = status.getUser().getScreenName().equals(usersn) ? status.getUser() : null;
            if(user != null){
                intent.putExtra(UserPage.INTENT_EXTRA_KEY_USER_OBJECT, user);
            }else{
                intent.putExtra(UserPage.INTENT_EXTRA_KEY_USER_SCREEN_NAME, usersn);
            }
            context.startActivity(intent);
        }
    }

    public void regEditPlus(EditText edit, String text){
        edit.setText(edit.getText().toString() + text);
    }

}