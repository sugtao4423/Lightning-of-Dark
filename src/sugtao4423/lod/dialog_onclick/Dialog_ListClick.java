package sugtao4423.lod.dialog_onclick;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.MediaEntity;
import twitter4j.Status;

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
import android.widget.AdapterView.OnItemClickListener;
import sugtao4423.lod.ChromeIntent;
import sugtao4423.lod.IntentActivity;
import sugtao4423.lod.ListViewListener;
import sugtao4423.lod.R;
import sugtao4423.lod.ShowToast;
import sugtao4423.lod.Show_Video;
import sugtao4423.lod.swipe_image_viewer.ImageFragmentActivity;
import sugtao4423.lod.tweetlistview.TweetListAdapter;
import sugtao4423.lod.tweetlistview.TweetListView;
import sugtao4423.lod.userpage_fragment.UserPage;
import sugtao4423.lod.utils.Regex;

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
			Button dot = (Button)regView.findViewById(R.id.regDialog_dot);
			Button kome = (Button)regView.findViewById(R.id.regDialog_kome);
			Button or = (Button)regView.findViewById(R.id.regDialog_or);
			Button plus = (Button)regView.findViewById(R.id.regDialog_plus);
			Button que = (Button)regView.findViewById(R.id.regDialog_q);
			Button backslash = (Button)regView.findViewById(R.id.regDialog_backslash);
			Button kakko1_0 = (Button)regView.findViewById(R.id.regDialog_kakko1_0);
			Button kakko1_1 = (Button)regView.findViewById(R.id.regDialog_kakko1_1);
			Button kakko2_0 = (Button)regView.findViewById(R.id.regDialog_kakko2_0);
			Button kakko2_1 = (Button)regView.findViewById(R.id.regDialog_kakko2_1);

			dot.setOnClickListener(new Dialog_regButtonClick(regEdit, "."));
			kome.setOnClickListener(new Dialog_regButtonClick(regEdit, "*"));
			or.setOnClickListener(new Dialog_regButtonClick(regEdit, "|"));
			plus.setOnClickListener(new Dialog_regButtonClick(regEdit, "+"));
			que.setOnClickListener(new Dialog_regButtonClick(regEdit, "?"));
			backslash.setOnClickListener(new Dialog_regButtonClick(regEdit, "\\"));
			kakko1_0.setOnClickListener(new Dialog_regButtonClick(regEdit, "("));
			kakko1_1.setOnClickListener(new Dialog_regButtonClick(regEdit, ")"));
			kakko2_0.setOnClickListener(new Dialog_regButtonClick(regEdit, "{"));
			kakko2_1.setOnClickListener(new Dialog_regButtonClick(regEdit, "}"));

			final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
			regEdit.setText(pref.getString("regularExpression", ""));
			new AlertDialog.Builder(context)
			.setTitle("正規表現を入力してください")
			.setView(regView)
			.setNegativeButton("キャンセル", null)
			.setPositiveButton("OK", new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which){
					String editReg = regEdit.getText().toString();
					pref.edit().putString("regularExpression", editReg).commit();
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
						new ShowToast("ありませんでした", context, 0);
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
						if(!media.getType().equals("video") || !media.getType().equals("animated_gif"))
							urls.add(media.getMediaURL());
					}
				}
				int pos = urls.indexOf(clickedText);
				String[] arr = (String[])urls.toArray(new String[0]);
				intent = new Intent(context, ImageFragmentActivity.class);
				intent.putExtra("urls", arr);
				intent.putExtra("position", pos);
			}else if(video.find()){
				intent = new Intent(context, Show_Video.class);
				intent.putExtra("URL", clickedText);
				intent.putExtra("type", Show_Video.TYPE_VIDEO);
			}else if(gif.find()){
				intent = new Intent(context, Show_Video.class);
				intent.putExtra("URL", clickedText);
				intent.putExtra("type", Show_Video.TYPE_GIF);
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
			intent.putExtra("userScreenName", clickedText.substring(1));
			context.startActivity(intent);
		}
	}

	public void regEditPlus(EditText edit, String text){
		edit.setText(edit.getText().toString() + text);
	}
}