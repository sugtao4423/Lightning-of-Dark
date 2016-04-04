package com.tao.lightning_of_dark.userPageFragment;

import twitter4j.PagableResponseList;
import twitter4j.TwitterException;
import twitter4j.User;

import com.tao.lightning_of_dark.ApplicationClass;
import com.tao.lightning_of_dark.CustomAdapter_User;
import com.tao.lightning_of_dark.R;
import com.tao.lightning_of_dark.ShowToast;

import android.annotation.SuppressLint;
import android.content.Intent;
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

public class _3_follow extends Fragment{

	private ListView userFollow, foot;
	private SwipeRefreshLayout pulltoRefresh;
	private CustomAdapter_User adapter;
	private long cursor;
	private ApplicationClass appClass;

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.user_1, null);
		appClass = (ApplicationClass)container.getContext().getApplicationContext();
		cursor = -1L;

		userFollow = (ListView)v.findViewById(R.id.UserPageList);
		adapter = new CustomAdapter_User(getActivity());

		addFooter();
		userFollow.setAdapter(adapter);
		userFollow.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				String screen = ((User)userFollow.getItemAtPosition(position)).getScreenName();
				Intent user = new Intent(container.getContext(), UserPage.class);
				user.putExtra("userScreenName", screen);
				startActivity(user);
			}
		});

		// PulltoRefresh
		pulltoRefresh = (SwipeRefreshLayout)v.findViewById(R.id.UserPagePull);
		pulltoRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
				android.R.color.holo_orange_light, android.R.color.holo_red_light);
		pulltoRefresh.setOnRefreshListener(new OnRefreshListener(){
			@Override
			public void onRefresh(){
				adapter.clear();
				FollowLine();
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
				FollowLine();
			}
		});
		userFollow.addFooterView(foot);
	}

	public void FollowLine(){
		((UserPage)_3_follow.this.getActivity()).resetUser();
		new AsyncTask<Void, Void, PagableResponseList<User>>(){
			@Override
			protected PagableResponseList<User> doInBackground(Void... params){
				try{
					return appClass.getTwitter().getFriendsList(appClass.getTarget().getScreenName(), cursor);
				}catch(TwitterException e){
					return null;
				}
			}

			@Override
			public void onPostExecute(PagableResponseList<User> result){
				if(result != null) {
					for(User user : result)
						adapter.add(user);
					cursor = result.getNextCursor();
				}else
					new ShowToast("フォローを取得できませんでした", getActivity(), 0);
				pulltoRefresh.setRefreshing(false);
				pulltoRefresh.setEnabled(true);
				foot.setEnabled(true);
			}
		}.execute();
	}
}