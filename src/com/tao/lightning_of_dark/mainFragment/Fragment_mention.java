package com.tao.lightning_of_dark.mainFragment;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

import com.tao.lightning_of_dark.ApplicationClass;
import com.tao.lightning_of_dark.CustomAdapter;
import com.tao.lightning_of_dark.ListViewListener;
import com.tao.lightning_of_dark.R;
import com.tao.lightning_of_dark.ShowToast;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Fragment_mention extends Fragment implements OnRefreshListener{

	private ListView list;
	private SwipeRefreshLayout pulltoRefresh;
	private CustomAdapter adapter;
	private ApplicationClass appClass;
	private Context context;

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		context = container.getContext();
		View v = inflater.inflate(R.layout.fragment_list, null);
		list = (ListView)v.findViewById(R.id.ListLine);
		list.setOnItemClickListener(new ListViewListener());
		list.setOnItemLongClickListener(new ListViewListener());

		appClass = (ApplicationClass)context.getApplicationContext();
		adapter = new CustomAdapter(context);
		adapter.registerDataSetObserver(new DataSetObserver(){
			@Override
			public void onChanged(){
				super.onChanged();
				moreMention();
				adapter.unregisterDataSetObserver(this);
			}
		});
		list.setAdapter(adapter);

		pulltoRefresh = (SwipeRefreshLayout)v.findViewById(R.id.ListPull);
		pulltoRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
				android.R.color.holo_orange_light, android.R.color.holo_red_light);
		pulltoRefresh.setOnRefreshListener(this);
		onRefresh();
		return v;
	}

	@Override
	public void onRefresh(){
		adapter.clear();
		new AsyncTask<Void, Void, ResponseList<Status>>(){
			@Override
			protected ResponseList<twitter4j.Status> doInBackground(Void... params){
				try{
					return appClass.getTwitter().getMentionsTimeline(new Paging(1, 50));
				}catch(TwitterException e){
					return null;
				}
			}

			@Override
			protected void onPostExecute(ResponseList<twitter4j.Status> result){
				if(result == null){
					new ShowToast("メンション取得エラー", context, 0);
				}else{
					for(twitter4j.Status status : result)
						add(status);
				}
				pulltoRefresh.setRefreshing(false);
				pulltoRefresh.setEnabled(true);
			}
		}.execute();
	}

	public void moreMention(){
		ListView foot = new ListView(getActivity());
		foot.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, new String[]{"ReadMore"}));
		foot.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				final long tweetId = adapter.getItem(adapter.getCount() - 1).getId();
				new AsyncTask<Void, Void, ResponseList<twitter4j.Status>>(){
					@Override
					protected ResponseList<twitter4j.Status> doInBackground(Void... params){
						try{
							return appClass.getTwitter().getMentionsTimeline(new Paging(1, 50).maxId(tweetId - 1));
						}catch(Exception e){
							return null;
						}
					}

					@Override
					protected void onPostExecute(ResponseList<twitter4j.Status> result){
						if(result != null)
							adapter.addAll(result);
						else
							new ShowToast("メンション取得エラー", getActivity(), 0);
					}
				}.execute();
			}
		});
		list.addFooterView(foot);
	}

	public void insert(final Status status){
		new AsyncTask<Void, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Void... params){
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result){
				if(list.getChildCount() != 0){
					int pos = list.getFirstVisiblePosition();
					int top = list.getChildAt(0).getTop();
					adapter.insert(status, 0);
					if(pos == 0 && top == 0)
						list.setSelectionFromTop(pos, 0);
					else
						list.setSelectionFromTop(pos + 1, top);
				}
			}
		}.execute();
	}

	public void add(Status status){
		adapter.add(status);
	}
}