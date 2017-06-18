package com.tao.lightning_of_dark.dialog_onClick;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.MediaEntity;
import twitter4j.Status;

import com.tao.lightning_of_dark.IntentActivity;
import com.tao.lightning_of_dark.ListViewListener;
import com.tao.lightning_of_dark.R;
import com.tao.lightning_of_dark.ShowToast;
import com.tao.lightning_of_dark.Show_Video;
import com.tao.lightning_of_dark.swipeImageViewer.ImageFragmentActivity;
import com.tao.lightning_of_dark.tweetlistview.TweetListAdapter;
import com.tao.lightning_of_dark.tweetlistview.TweetListView;
import com.tao.lightning_of_dark.userPageFragment.UserPage;

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

		if(clickedText.equals("正規表現で抽出")) {
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

			final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(parent.getContext());
			regEdit.setText(pref.getString("regularExpression", ""));
			new AlertDialog.Builder(parent.getContext())
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
		}

		if(clickedText.startsWith("http") || clickedText.startsWith("ftp")) {
			Matcher image = Pattern.compile("http(s)?://pbs.twimg.com/media/").matcher(clickedText);
			Matcher video = Pattern.compile("http(s)?://video.twimg.com/ext_tw_video/[0-9]+/(pu|pr)/vid/.+/.+(.mp4|.webm)").matcher(clickedText);
			Matcher gif = Pattern.compile("http(s)?://pbs.twimg.com/tweet_video/").matcher(clickedText);
			Matcher state = Pattern.compile("http(s)?://twitter.com/[0-9a-zA-Z_]+/status/([0-9]+)").matcher(clickedText);
			Intent web;
			if(image.find()) {
				ArrayList<String> urls = new ArrayList<String>();
				MediaEntity[] mentitys = status.getMediaEntities();
				if(mentitys != null && mentitys.length > 0) {
					for(MediaEntity media : mentitys){
						if(!media.getType().equals("video") || !media.getType().equals("animated_gif"))
							urls.add(media.getMediaURL());
					}
				}
				int pos = urls.indexOf(clickedText);
				String[] arr = (String[])urls.toArray(new String[0]);
				web = new Intent(parent.getContext(), ImageFragmentActivity.class);
				web.putExtra("urls", arr);
				web.putExtra("position", pos);
			}else if(video.find()) {
				web = new Intent(parent.getContext(), Show_Video.class);
				web.putExtra("URL", clickedText);
				web.putExtra("type", Show_Video.TYPE_VIDEO);
			}else if(gif.find()) {
				web = new Intent(parent.getContext(), Show_Video.class);
				web.putExtra("URL", clickedText);
				web.putExtra("type", Show_Video.TYPE_GIF);
			}else if(state.find()) {
				new IntentActivity().showStatus(Long.parseLong(state.group(2)), context, false);
				return;
			}else{
				web = new Intent(Intent.ACTION_VIEW, Uri.parse(clickedText));
			}
			parent.getContext().startActivity(web);
		}
		if(clickedText.equals("ブラウザで開く")) {
			String url, SN, Id;
			if(status.isRetweet()) {
				SN = status.getRetweetedStatus().getUser().getScreenName();
				Id = String.valueOf(status.getRetweetedStatus().getId());
			}else{
				SN = status.getUser().getScreenName();
				Id = String.valueOf(status.getId());
			}
			url = "https://twitter.com/" + SN + "/status/" + Id;
			parent.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
		}
		if(clickedText.startsWith("@")) { // UserPage
			Intent intent = new Intent(parent.getContext(), UserPage.class);
			intent.putExtra("userScreenName", clickedText.substring(1));
			parent.getContext().startActivity(intent);
		}
	}

	public void regEditPlus(EditText edit, String text){
		edit.setText(edit.getText().toString() + text);
	}
}