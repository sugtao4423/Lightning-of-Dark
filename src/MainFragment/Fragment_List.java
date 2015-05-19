package MainFragment;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

import com.tao.lightning_of_dark.ApplicationClass;
import com.tao.lightning_of_dark.CustomAdapter;
import com.tao.lightning_of_dark.ListViewListener;
import com.tao.lightning_of_dark.Preference;
import com.tao.lightning_of_dark.R;
import com.tao.lightning_of_dark.ShowToast;

import android.app.AlertDialog;
import android.content.Context;
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
	private long tweetId;
	
	@Override
	public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_list, null);
		final ApplicationClass appClass = (ApplicationClass)container.getContext().getApplicationContext();
		ListLine = (ListView)v.findViewById(R.id.ListLine);
		ListLine.setOnItemClickListener(new ListViewListener());
		ListLine.setOnItemLongClickListener(new ListViewListener());
		
		adapter = appClass.getListAdapter();
		
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
				appClass.setList_AlreadyLoad(false);
				getList(container.getContext());
			}
		});
		
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
				getList(parent.getContext());
			}
		});
		ListLine.addFooterView(foot);
	}
	
	public void getList(Context context){
		final ApplicationClass appClass = (ApplicationClass)context.getApplicationContext();
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		if(pref.getBoolean("showList", false)){
			final long ListId = pref.getLong("SelectListId", -1);
			if(ListId != -1){
				if(appClass.getList_AlreadyLoad()){
					tweetId = ((Status)ListLine.getItemAtPosition(ListLine.getAdapter().getCount() - 2)).getId();
				}
				AsyncTask<Void, Void, ResponseList<twitter4j.Status>> task = new AsyncTask<Void, Void, ResponseList<twitter4j.Status>>(){
					@Override
					protected ResponseList<twitter4j.Status> doInBackground(Void... params) {
						try {
							if(appClass.getList_AlreadyLoad())
								return appClass.getTwitter().getUserListStatuses(ListId, new Paging(1, 50).maxId(tweetId - 1));
							else
								return appClass.getTwitter().getUserListStatuses(ListId, new Paging(1, 50));
							} catch (TwitterException e) {
								return null;
							}
					}
					protected void onPostExecute(ResponseList<twitter4j.Status> result){
						if(result != null){
							for(twitter4j.Status status : result)
								adapter.add(status);
							if(!appClass.getList_AlreadyLoad())
								appClass.setList_AlreadyLoad(true);
						}else
							new ShowToast("リストを取得できませんでした", getActivity());
						PulltoRefresh.setRefreshing(false);
						PulltoRefresh.setEnabled(true);
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
