package MainFragment;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

import com.tao.lightning_of_dark.CustomAdapter;
import com.tao.lightning_of_dark.ListViewListener;
import com.tao.lightning_of_dark.MainActivity;
import com.tao.lightning_of_dark.Preference;
import com.tao.lightning_of_dark.R;
import com.tao.lightning_of_dark.UserPage;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class Fragment_List extends Fragment {
	
	private ListView ListLine, foot;
	private SwipeRefreshLayout PulltoRefresh;
	private CustomAdapter adapter;
	private SharedPreferences pref;
	private boolean AlreadyLoad;
	private long tweetId;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_list, null);
		pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		ListLine = (ListView)v.findViewById(R.id.ListLine);
		ListLine.setOnItemClickListener(new ListViewListener());
		ListLine.setOnItemLongClickListener(new ListViewListener());
		
		adapter = new CustomAdapter(getActivity());
		
		addFooter();
		
		ListLine.setAdapter(adapter);
		
		PulltoRefresh = (SwipeRefreshLayout)v.findViewById(R.id.ListPull);
		PulltoRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, 
	            android.R.color.holo_green_light, 
	            android.R.color.holo_orange_light, 
	            android.R.color.holo_red_light);
		PulltoRefresh.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				adapter.clear();
				AlreadyLoad = false;
				getList();
			}
		});
		
		if(pref.getBoolean("startApp_showList", false))
			getList();
		
		return v;
	}
	
	public void addFooter(){
		foot = new ListView(getActivity());
		foot.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, new String[]{"ReadMore"}));
		foot.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				foot.setEnabled(false);
				getList();
			}
		});
		ListLine.addFooterView(foot);
	}
	
	public void getList(){
		if(pref.getBoolean("showList", false)){
			final long ListId = pref.getLong("SelectListId", -1);
			if(ListId != -1){
				if(AlreadyLoad){
					Status s = (Status)ListLine.getItemAtPosition(ListLine.getAdapter().getCount() - 2);
					tweetId = s.getId();
				}
				AsyncTask<Void, Void, ResponseList<twitter4j.Status>> task = new AsyncTask<Void, Void, ResponseList<twitter4j.Status>>(){
					@Override
					protected ResponseList<twitter4j.Status> doInBackground(Void... params) {
						try {
							if(AlreadyLoad)
								return MainActivity.twitter.getUserListStatuses(ListId, new Paging(1, 50).maxId(tweetId - 1));
							else
								return MainActivity.twitter.getUserListStatuses(ListId, new Paging(1, 50));
							} catch (TwitterException e) {
								return null;
							}
					}
					protected void onPostExecute(ResponseList<twitter4j.Status> result){
						if(result != null){
							for(twitter4j.Status status : result)
								adapter.add(status);
							if(!AlreadyLoad)
								AlreadyLoad = true;
							PulltoRefresh.setRefreshing(false);
							PulltoRefresh.setEnabled(true);
						}else
							new UserPage().showToast("リストを取得できませんでした");
						foot.setEnabled(true);
					}
				};
				task.execute();
			}else{
				PulltoRefresh.setRefreshing(false);
				PulltoRefresh.setEnabled(true);
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("リストを選択してください")
				.setMessage("リストが未選択です。\n設定ページを開きますか？")
				.setPositiveButton("開く", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startActivity(new Intent(getActivity(), Preference.class));
					}
				});
				builder.setNegativeButton("キャンセル", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
				builder.create().show();
			}
		}
	}
}
