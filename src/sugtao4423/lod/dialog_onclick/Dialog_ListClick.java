package sugtao4423.lod.dialog_onclick;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.User;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemClickListener;
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

		if(clickedText.equals("正規表現で抽出")){
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
			.setTitle("正規表現を入力してください")
			.setView(regView)
			.setNegativeButton("キャンセル", null)
			.setPositiveButton("OK", new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which){
					String editReg = regEdit.getText().toString();
					pref.edit().putString(Keys.REGULAR_EXPRESSION, editReg).commit();
					Pattern pattern = Pattern.compile(editReg, Pattern.DOTALL);
					TweetListAdapter adapter = new TweetListAdapter(context);
					boolean exists = false;
					for(Status s : listData){
						if(pattern.matcher(s.getText()).find()){
							adapter.add(s);
							exists = true;
						}
					}
					if(!exists){
						new ShowToast(R.string.nothing, context, 0);
					}else{
						TweetListView l = new TweetListView(context);
						l.setAdapter(adapter);
						adapter.setOnItemClickListener(new ListViewListener());
						adapter.setOnItemLongClickListener(new ListViewListener());
						new AlertDialog.Builder(context).setView(l).show();
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
							urls.add(media.getMediaURL());
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
				new IntentActivity().showStatus(Long.parseLong(state.group(2)), context, false);
				return;
			}else{
				new ChromeIntent(context, Uri.parse(clickedText));
				return;
			}
			context.startActivity(intent);
		}else if(clickedText.equals("ブラウザで開く")){
			Status orig = status.isRetweet() ? status.getRetweetedStatus() : status;
			String tweet_sn = orig.getUser().getScreenName();
			String tweet_id = String.valueOf(orig.getId());
			String url = "https://twitter.com/" + tweet_sn + "/status/" + tweet_id;
			context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
		}else if(clickedText.startsWith("@")){ // UserPage
			Intent intent = new Intent(context, UserPage.class);
			String usersn = clickedText.substring(1);
			User user = status.getUser().getScreenName().equals(usersn) ? status.getUser() : null;
			if(user != null)
				intent.putExtra(UserPage.INTENT_EXTRA_KEY_USER_OBJECT, user);
			else
				intent.putExtra(UserPage.INTENT_EXTRA_KEY_USER_SCREEN_NAME, usersn);
			context.startActivity(intent);
		}
	}

	public void regEditPlus(EditText edit, String text){
		edit.setText(edit.getText().toString() + text);
	}
}