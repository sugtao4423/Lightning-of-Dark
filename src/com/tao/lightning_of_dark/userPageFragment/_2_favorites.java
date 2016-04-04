package com.tao.lightning_of_dark.userPageFragment;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;

import com.tao.lightning_of_dark.ApplicationClass;
import com.tao.lightning_of_dark.CustomAdapter;
import com.tao.lightning_of_dark.ListViewListener;
import com.tao.lightning_of_dark.R;
import com.tao.lightning_of_dark.ShowToast;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
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

public class _2_favorites extends Fragment{

	private ListView userFavorite, foot;
	private SwipeRefreshLayout pulltoRefresh;
	private CustomAdapter adapter;
	private boolean alreadyLoad;
	private long tweetId;
	private ApplicationClass appClass;

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.user_1, null);
		appClass = (ApplicationClass)container.getContext().getApplicationContext();
		alreadyLoad = false;

		userFavorite = (ListView)v.findViewById(R.id.UserPageList);
		userFavorite.setOnItemClickListener(new ListViewListener(false));
		userFavorite.setOnItemLongClickListener(new ListViewListener(false));

		adapter = new CustomAdapter(getActivity());
		// フッター生成
		addFooter();
		userFavorite.setAdapter(adapter);

		pulltoRefresh = (SwipeRefreshLayout)v.findViewById(R.id.UserPagePull);
		pulltoRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
				android.R.color.holo_orange_light, android.R.color.holo_red_light);
		pulltoRefresh.setOnRefreshListener(new OnRefreshListener(){
			@Override
			public void onRefresh(){
				adapter.clear();
				alreadyLoad = false;
				MentionLine();
			}
		});
		return v;
	}

	// フッター生成
	public void addFooter(){
		foot = new ListView(getActivity());
		foot.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, new String[]{"ReadMore"}));
		foot.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				foot.setEnabled(false);
				MentionLine();
			}
		});
		userFavorite.addFooterView(foot);
	}

	public void MentionLine(){
		if(alreadyLoad)
			tweetId = ((Status)userFavorite.getItemAtPosition(userFavorite.getAdapter().getCount() - 2)).getId();
		((UserPage)_2_favorites.this.getActivity()).resetUser();
		new AsyncTask<Void, Void, ResponseList<Status>>(){
			@Override
			protected ResponseList<twitter4j.Status> doInBackground(Void... params){
				try{
					if(alreadyLoad)
						return appClass.getTwitter().getFavorites(appClass.getTarget().getScreenName(),
								new Paging(1, 50).maxId(tweetId - 1));
					else
						return appClass.getTwitter().getFavorites(appClass.getTarget().getScreenName(), new Paging(1, 50));
				}catch(Exception e){
					return null;
				}
			}

			protected void onPostExecute(ResponseList<twitter4j.Status> result){
				if(result != null) {
					for(twitter4j.Status status : result)
						adapter.add(status);
					if(!alreadyLoad)
						alreadyLoad = true;
				}else
					new ShowToast("ふぁぼ取得エラー", getActivity(), 0);
				pulltoRefresh.setRefreshing(false);
				pulltoRefresh.setEnabled(true);
				foot.setEnabled(true);
			}
		}.execute();
	}
}