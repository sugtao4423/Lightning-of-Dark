package dialog_onClick;

import java.util.regex.Pattern;

import twitter4j.Status;

import com.tao.lightning_of_dark.ApplicationClass;
import com.tao.lightning_of_dark.CustomAdapter;
import com.tao.lightning_of_dark.ListViewListener;
import com.tao.lightning_of_dark.Show_Image;
import com.tao.lightning_of_dark.UserPage;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class Dialog_ListClick implements OnItemClickListener {

	private Status status;
	private AdapterView<?> baseParent;
	
	private boolean tweet_do_back;
	
	public Dialog_ListClick(Status status, AdapterView<?> baseParent, boolean tweet_do_back) {
		this.status = status;
		this.baseParent = baseParent;
		this.tweet_do_back = tweet_do_back;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		((ApplicationClass)baseParent.getContext().getApplicationContext()).getDialog().dismiss();
		String clickedText = (String)parent.getItemAtPosition(position);
		
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
					CustomAdapter content = new CustomAdapter(baseParent.getContext());
					for(int i = 0; baseParent.getCount() - 1 > i; i++){
						Status status = ((Status) baseParent.getAdapter().getItem(i));
						if(pattern.matcher(status.getText()).find())
							content.add(status);
					}
					AlertDialog.Builder b = new AlertDialog.Builder(baseParent.getContext());
					ListView l = new ListView(baseParent.getContext());
					if(content.isEmpty())
						l.setAdapter(new ArrayAdapter<String>(baseParent.getContext(),android.R.layout.simple_list_item_1, new String[]{"なし"}));
					else{
						l.setAdapter(content);
						l.setOnItemClickListener(new ListViewListener(tweet_do_back));
						l.setOnItemLongClickListener(new ListViewListener(tweet_do_back));
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
			Intent web;
			if(clickedText.startsWith("http://pbs.twimg.com/media/") || clickedText.startsWith("https://pbs.twimg.com/media/")){
				web = new Intent(parent.getContext(), Show_Image.class);
				web.putExtra("URL", clickedText);
			}else
				web = new Intent(Intent.ACTION_VIEW, Uri.parse(clickedText));
			parent.getContext().startActivity(web);
		}
		if(clickedText.equals("ブラウザで開く")){
			String url, SN, Id;
			if(status.isRetweet()){
				SN = status.getRetweetedStatus().getUser().getScreenName();
				Id = String.valueOf(status.getRetweetedStatus().getId());
			}else{
				SN = status.getUser().getScreenName();
				Id = String.valueOf(status.getId());
			}
			url = "https://twitter.com/" + SN + "/status/" + Id;
			parent.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
		}
		if(clickedText.startsWith("@")){ //UserPage
			Intent intent = new Intent(parent.getContext(), UserPage.class);
			intent.putExtra("userScreenName", clickedText.substring(1));
			parent.getContext().startActivity(intent);
		}
	}
}