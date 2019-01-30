package sugtao4423.lod.main_fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import sugtao4423.lod.App;
import sugtao4423.lod.ListViewListener;
import sugtao4423.lod.R;
import sugtao4423.lod.ShowToast;
import sugtao4423.lod.tweetlistview.EndlessScrollListener;
import sugtao4423.lod.tweetlistview.TweetListAdapter;
import sugtao4423.lod.tweetlistview.TweetListView;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

public class Fragment_home extends Fragment{

    private TweetListView list;
    private TweetListAdapter adapter;
    private Handler handler;
    private SwipeRefreshLayout pulltoRefresh;
    private long listAsTL;
    private App app;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        app = (App)container.getContext().getApplicationContext();
        handler = new Handler();
        View v = inflater.inflate(R.layout.fragment_list, container, false);
        list = (TweetListView)v.findViewById(R.id.listLine);
        listAsTL = app.getCurrentAccount().getListAsTL();

        adapter = new TweetListAdapter(container.getContext());
        adapter.setOnItemClickListener(new ListViewListener());
        adapter.setOnItemLongClickListener(new ListViewListener());
        list.setAdapter(adapter);
        final EndlessScrollListener scrollListener = getLoadMoreListener();
        list.addOnScrollListener(scrollListener);

        pulltoRefresh = (SwipeRefreshLayout)v.findViewById(R.id.ListPull);
        pulltoRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
        OnRefreshListener onRefreshListener = new OnRefreshListener(){

            @Override
            public void onRefresh(){
                adapter.clear();
                loadTimeLine();
                scrollListener.resetState();
            }
        };
        pulltoRefresh.setOnRefreshListener(onRefreshListener);
        onRefreshListener.onRefresh();
        return v;
    }

    public void loadTimeLine(){
        new AsyncTask<Void, Void, ResponseList<Status>>(){
            @Override
            protected ResponseList<twitter4j.Status> doInBackground(Void... params){
                Paging paging;
                if(adapter.getItemCount() > 0){
                    long tweetId = adapter.getItem(adapter.getItemCount() - 1).getId();
                    paging = new Paging(1, 50).maxId(tweetId - 1);
                }else{
                    paging = new Paging(1, 50);
                }

                try{
                    if(listAsTL > 0){
                        return app.getTwitter().getUserListStatuses(listAsTL, paging);
                    }else{
                        return app.getTwitter().getHomeTimeline(paging);
                    }
                }catch(TwitterException e){
                    return null;
                }
            }

            @Override
            protected void onPostExecute(ResponseList<twitter4j.Status> result){
                if(result != null){
                    if(adapter.getItemCount() <= 0){
                        app.setLatestTweetId(result.get(0).getId());
                    }
                    adapter.addAll(result);
                }else{
                    new ShowToast(getContext().getApplicationContext(), R.string.error_getTimeLine);
                }
                pulltoRefresh.setRefreshing(false);
                pulltoRefresh.setEnabled(true);
            }
        }.execute();
    }

    public void insert(Status status){
        adapter.insertTop(status);
        app.setLatestTweetId(status.getId());
        handler.post(new Runnable(){

            @Override
            public void run(){
                if(list.getLinearLayoutManager().findFirstVisibleItemPosition() <= 1){
                    list.smoothScrollToPosition(0);
                }
            }
        });
    }

    public void clear(){
        if(adapter != null){
            adapter.clear();
        }
    }

    public EndlessScrollListener getLoadMoreListener(){
        return new EndlessScrollListener(list.getLinearLayoutManager()){

            @Override
            public void onLoadMore(int current_page){
                if(adapter.getItemCount() > 30){
                    loadTimeLine();
                }
            }
        };
    }

}