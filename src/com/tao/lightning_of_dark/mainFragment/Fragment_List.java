package com.tao.lightning_of_dark.mainFragment;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

import com.tao.lightning_of_dark.ApplicationClass;
import com.tao.lightning_of_dark.CustomAdapter;
import com.tao.lightning_of_dark.ListViewListener;
import com.tao.lightning_of_dark.Settings;
import com.tao.lightning_of_dark.R;
import com.tao.lightning_of_dark.ShowToast;

import android.annotation.SuppressLint;
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

public class Fragment_List extends Fragment{

	private ListView listLine, foot;
	private SwipeRefreshLayout pulltoRefresh;
	private CustomAdapter adapter;
	private long tweetId;
	private int listIndex;

	public Fragment_List(int index){
		listIndex = index;
	}

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.fragment_list, null);
		final ApplicationClass appClass = (ApplicationClass)container.getContext().getApplicationContext();
		listLine = (ListView)v.findViewById(R.id.ListLine);
		listLine.setOnItemClickListener(new ListViewListener(true));
		listLine.setOnItemLongClickListener(new ListViewListener(true));

		adapter = appClass.getListAdapters()[listIndex];

		addFooter();

		listLine.setAdapter(adapter);

		pulltoRefresh = (SwipeRefreshLayout)v.findViewById(R.id.ListPull);
		pulltoRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
				android.R.color.holo_orange_light, android.R.color.holo_red_light);
		pulltoRefresh.setOnRefreshListener(new OnRefreshListener(){
			@Override
			public void onRefresh(){
				adapter.clear();
				boolean[] tmp = appClass.getList_AlreadyLoad();
				tmp[listIndex] = false;
				appClass.setList_AlreadyLoad(tmp);
				getList(container.getContext());
			}
		});

		return v;
	}

	public void addFooter(){
		foot = new ListView(getActivity());
		foot.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, new String[]{"ReadMore"}));
		foot.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				foot.setEnabled(false);
				getList(parent.getContext());
			}
		});
		listLine.addFooterView(foot);
	}

	public void getList(Context context){
		final ApplicationClass appClass = (ApplicationClass)context.getApplicationContext();
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		if(pref.getBoolean("showList", false)) {
			String[] ListId_str = pref.getString("SelectListIds", null).split(",", 0);
			final long[] ListId = new long[ListId_str.length];
			for(int i = 0; i < ListId_str.length; i++)
				ListId[i] = Long.parseLong(ListId_str[i]);
			if(ListId[listIndex] != -1) {
				if(appClass.getList_AlreadyLoad()[listIndex]) {
					tweetId = ((Status)listLine.getItemAtPosition(listLine.getAdapter().getCount() - 2)).getId();
				}
				new AsyncTask<Void, Void, ResponseList<twitter4j.Status>>(){
					@Override
					protected ResponseList<twitter4j.Status> doInBackground(Void... params){
						try{
							if(appClass.getList_AlreadyLoad()[listIndex])
								return appClass.getTwitter().getUserListStatuses(ListId[listIndex],
										new Paging(1, 50).maxId(tweetId - 1));
							else
								return appClass.getTwitter().getUserListStatuses(ListId[listIndex], new Paging(1, 50));
						}catch(TwitterException e){
							return null;
						}
					}

					@Override
					protected void onPostExecute(ResponseList<twitter4j.Status> result){
						if(result != null) {
							for(twitter4j.Status status : result)
								adapter.add(status);
							if(!appClass.getList_AlreadyLoad()[listIndex]) {
								boolean[] tmp = appClass.getList_AlreadyLoad();
								tmp[listIndex] = true;
								appClass.setList_AlreadyLoad(tmp);
							}
						}else
							new ShowToast("リストを取得できませんでした", getActivity(), 0);
						pulltoRefresh.setRefreshing(false);
						pulltoRefresh.setEnabled(true);
						foot.setEnabled(true);
					}
				}.execute();
			}else{
				pulltoRefresh.setRefreshing(false);
				pulltoRefresh.setEnabled(true);
				new AlertDialog.Builder(getActivity())
				.setTitle("リストを選択してください")
				.setMessage("リストが未選択です。\n設定ページを開きますか？")
				.setPositiveButton("開く", new OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which){
						startActivity(new Intent(getActivity(), Settings.class));
					}
				})
				.setNegativeButton("キャンセル", new OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which){
					}
				}).show();
			}
		}
	}
}