package UserPageFragment;

import twitter4j.PagableResponseList;
import twitter4j.TwitterException;
import twitter4j.User;

import com.tao.lightning_of_dark.ApplicationClass;
import com.tao.lightning_of_dark.CustomAdapter_User;
import com.tao.lightning_of_dark.R;
import com.tao.lightning_of_dark.ShowToast;
import com.tao.lightning_of_dark.UserPage;
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

public class _4_follower extends Fragment{

	private ListView userFollower, foot;
	private SwipeRefreshLayout PulltoRefresh;
	private CustomAdapter_User adapter;
	private long cursor;
	private ApplicationClass appClass;

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.user_1, null);
		appClass = (ApplicationClass)container.getContext().getApplicationContext();
		cursor = -1L;

		userFollower = (ListView)v.findViewById(R.id.UserPageList);
		adapter = new CustomAdapter_User(getActivity());

		addFooter();
		userFollower.setAdapter(adapter);
		userFollower.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				String screen = ((User)userFollower.getItemAtPosition(position)).getScreenName();
				Intent user = new Intent(container.getContext(), UserPage.class);
				user.putExtra("userScreenName", screen);
				startActivity(user);
			}
		});

		// PulltoRefresh
		PulltoRefresh = (SwipeRefreshLayout)v.findViewById(R.id.UserPagePull);
		PulltoRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
				android.R.color.holo_orange_light, android.R.color.holo_red_light);
		PulltoRefresh.setOnRefreshListener(new OnRefreshListener(){
			@Override
			public void onRefresh(){
				adapter.clear();
				FollowerLine();
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
				FollowerLine();
			}
		});
		userFollower.addFooterView(foot);
	}

	public void FollowerLine(){
		AsyncTask<Void, Void, PagableResponseList<User>> task = new AsyncTask<Void, Void, PagableResponseList<User>>(){
			@Override
			protected PagableResponseList<User> doInBackground(Void... params){
				try{
					return appClass.getTwitter().getFollowersList(appClass.getTargetScreenName(), cursor);
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
					new ShowToast("フォロワーを取得できませんでした", getActivity(), 0);
				PulltoRefresh.setRefreshing(false);
				PulltoRefresh.setEnabled(true);
				foot.setEnabled(true);
			}
		};
		((UserPage)_4_follower.this.getActivity()).resetUser();
		task.execute();
	}
}