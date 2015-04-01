package UserPageFragment;

import twitter4j.Paging;
import twitter4j.ResponseList;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class _1_Tweet extends Fragment {
	
	static ListView userTweet;
	SwipeRefreshLayout PulltoRefresh;
	CustomAdapter adapter;
	ResponseList<twitter4j.Status> timeline;;
	int num = 1;
	
	@Override
	  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.user_1, null);
		//通常のListViewSet
		userTweet = (ListView)v.findViewById(R.id.UserTweet);
		userTweet.setOnItemClickListener(new ListViewListener());
		userTweet.setOnItemLongClickListener(new ListViewListener());
		//ここまで
		adapter = new CustomAdapter(getActivity());
		//フッター生成〜設定まで
		ListView foot = new ListView(getActivity());
		foot.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, new String[]{"ReadMore"}));
		foot.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				setTweetList();
			}
		});
		userTweet.addFooterView(foot);
		//ここまで
		
		setTweetList();
		
		//PulltoRefresh
		PulltoRefresh = (SwipeRefreshLayout)v.findViewById(R.id.UserTweetPull);
		PulltoRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, 
	            android.R.color.holo_green_light, 
	            android.R.color.holo_orange_light, 
	            android.R.color.holo_red_light);
		PulltoRefresh.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				adapter.clear();
				num = 1;
				setTweetList();
			}
		});
		
		return v;
	}
	public void setTweetList(){
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Void... params) {
				try {
					timeline = MainActivity.twitter.getUserTimeline(UserPage.target.getId(), new Paging(num, 50));
					return true;
				} catch (TwitterException e) {
					return false;
				}
			}
			protected void onPostExecute(Boolean result){
				if(result){
					for(twitter4j.Status status : timeline)
						adapter.add(status);
					if(num == 1)
						userTweet.setAdapter(adapter);
					num++;
				}else
					new UserPage().showToast("タイムラインを取得できませんでした");
				PulltoRefresh.setRefreshing(false);
				PulltoRefresh.setEnabled(true);
			}
		};
		task.execute();
	}
}
