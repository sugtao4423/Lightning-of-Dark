package UserPageFragment;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

import com.tao.lightning_of_dark.CustomAdapter;
import com.tao.lightning_of_dark.ListViewListener;
import com.tao.lightning_of_dark.MainActivity;
import com.tao.lightning_of_dark.R;
import com.tao.lightning_of_dark.UserPage;

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

public class _2_favorites extends Fragment {
	
	static ListView UserFav;
	SwipeRefreshLayout PulltoRefresh;
	CustomAdapter adapter;
	ResponseList<twitter4j.Status> FavLine;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.user_2, null);
		UserFav = (ListView)v.findViewById(R.id.Userfav);
		UserFav.setOnItemClickListener(new ListViewListener());
		UserFav.setOnItemLongClickListener(new ListViewListener());
		
		adapter = new CustomAdapter(getActivity());
		
		//フッター生成
		addFooter();
		
		setFavList();
		
		PulltoRefresh = (SwipeRefreshLayout)v.findViewById(R.id.UserFavPull);
		PulltoRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, 
	            android.R.color.holo_green_light, 
	            android.R.color.holo_orange_light, 
	            android.R.color.holo_red_light);
		PulltoRefresh.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				adapter.clear();
				setFavList();
			}
		});
		
		return v;
	}
	
	public void setFavList(){
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Void... params) {
				try {
					FavLine = MainActivity.twitter.getFavorites(UserPage.target.getId(), new Paging(1, 50));
					return true;
				} catch (TwitterException e) {
					return false;
				}
			}
			protected void onPostExecute(Boolean result){
				if(result){
					for(twitter4j.Status status : FavLine)
						adapter.add(status);
					UserFav.setAdapter(adapter);
				}else
					new UserPage().showToast("お気に入りを取得できませんでした");
				PulltoRefresh.setRefreshing(false);
				PulltoRefresh.setEnabled(true);
			}
		};
		task.execute();
	}
	
	//フッター生成
	public void addFooter(){
		ListView foot = new ListView(getActivity());
		foot.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, new String[]{"ReadMore"}));
		foot.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Status s = (Status)UserFav.getItemAtPosition(UserFav.getAdapter().getCount() - 2);
				final long tweetId = s.getId();
				AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>(){
					@Override
					protected Boolean doInBackground(Void... params) {
						try{
							FavLine = MainActivity.twitter.getFavorites(UserPage.target.getId(), new Paging(1, 50).maxId(tweetId));
							return true;
						}catch(Exception e){
							return false;
						}
					}
					protected void onPostExecute(Boolean result){
						if(result){
							boolean i = false;
							for(twitter4j.Status status : FavLine){
								if(i)
									adapter.add(status);
								else
									i = true;
							}
						}else
							new MainActivity().showToast("ふぁぼ取得エラー", getActivity());
					}
				};
				task.execute();
			}
		});
		UserFav.addFooterView(foot);
	}
}
