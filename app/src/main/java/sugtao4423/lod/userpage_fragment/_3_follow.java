package sugtao4423.lod.userpage_fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import sugtao4423.lod.App;
import sugtao4423.lod.R;
import sugtao4423.lod.ShowToast;
import sugtao4423.lod.tweetlistview.EndlessScrollListener;
import sugtao4423.lod.tweetlistview.TweetListUserAdapter;
import sugtao4423.lod.tweetlistview.TweetListView;
import twitter4j.PagableResponseList;
import twitter4j.TwitterException;
import twitter4j.User;

public class _3_follow extends Fragment{

    private SwipeRefreshLayout pulltoRefresh;
    private TweetListUserAdapter adapter;
    private long cursor;
    private boolean isAllLoaded;
    private App app;
    private User targetUser;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.user_1, container, false);
        app = (App)container.getContext().getApplicationContext();
        cursor = -1L;

        TweetListView userFollow = (TweetListView)v.findViewById(R.id.UserPageList);

        adapter = new TweetListUserAdapter(container.getContext());
        userFollow.setAdapter(adapter);

        final EndlessScrollListener scrollListener = getLoadMoreListener(userFollow.getLinearLayoutManager());
        userFollow.addOnScrollListener(scrollListener);

        pulltoRefresh = (SwipeRefreshLayout)v.findViewById(R.id.UserPagePull);
        pulltoRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
        pulltoRefresh.setOnRefreshListener(new OnRefreshListener(){
            @Override
            public void onRefresh(){
                adapter.clear();
                cursor = -1L;
                isAllLoaded = false;
                loadFollowLine();
                scrollListener.resetState();
            }
        });
        return v;
    }

    public void setTargetUser(User targetUser){
        this.targetUser = targetUser;
    }

    public EndlessScrollListener getLoadMoreListener(LinearLayoutManager llm){
        return new EndlessScrollListener(llm){

            @Override
            public void onLoadMore(int current_page){
                if(!isAllLoaded){
                    loadFollowLine();
                }
            }
        };
    }

    public void loadFollowLine(){
        new AsyncTask<Void, Void, PagableResponseList<User>>(){
            @Override
            protected PagableResponseList<User> doInBackground(Void... params){
                try{
                    return app.getTwitter().getFriendsList(targetUser.getScreenName(), cursor);
                }catch(TwitterException e){
                    return null;
                }
            }

            @Override
            public void onPostExecute(PagableResponseList<User> result){
                if(result != null){
                    adapter.addAll(result);
                    cursor = result.getNextCursor();
                    if(targetUser != null && targetUser.getFriendsCount() <= adapter.getItemCount()){
                        isAllLoaded = true;
                    }
                }else{
                    new ShowToast(getContext().getApplicationContext(), R.string.error_getFollow);
                }
                pulltoRefresh.setRefreshing(false);
                pulltoRefresh.setEnabled(true);
            }
        }.execute();
    }

}