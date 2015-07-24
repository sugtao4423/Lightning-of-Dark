package dialog_onClick;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.Status;
import com.tao.lightning_of_dark.ApplicationClass;
import com.tao.lightning_of_dark.CustomAdapter;
import com.tao.lightning_of_dark.IntentActivity;
import com.tao.lightning_of_dark.ListViewListener;
import com.tao.lightning_of_dark.R;
import com.tao.lightning_of_dark.Show_Image;
import com.tao.lightning_of_dark.UserPage;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
		((ApplicationClass)baseParent.getContext().getApplicationContext()).getListViewDialog().dismiss();
		String clickedText = (String)parent.getItemAtPosition(position);
		
		if(clickedText.equals("正規表現で抽出")){
			View regView = LayoutInflater.from(parent.getContext()).inflate(R.layout.reg_dialog, null);
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
			
			
			AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());
			final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(parent.getContext());
			regEdit.setText(pref.getString("regularExpression", ""));
			builder.setTitle("正規表現を入力してください")
			.setView(regView)
			.setPositiveButton("OK", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String editReg = regEdit.getText().toString();
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
		
		Matcher image = Pattern.compile("(http://|https://){1}pbs.twimg.com/media/").matcher(clickedText);
		Matcher state = Pattern.compile("https://twitter.com/[0-9a-zA-Z_]+/status/([0-9]+)").matcher(clickedText);
		if(clickedText.startsWith("http") || clickedText.startsWith("ftp")){
			Intent web;
			if(image.find()){
				web = new Intent(parent.getContext(), Show_Image.class);
				web.putExtra("URL", clickedText);
			}
			else if(state.find()){
				new IntentActivity().showStatus(Long.parseLong(state.group(1)), baseParent.getContext(), false);
				return;
			}
			else{
				web = new Intent(Intent.ACTION_VIEW, Uri.parse(clickedText));
			}
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
	
	public void regEditPlus(EditText edit, String text){
		edit.setText(edit.getText().toString() + text);
	}
}