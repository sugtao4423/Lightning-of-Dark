package sugtao4423.lod.main_fragment;

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
import sugtao4423.lod.ListViewListener;
import sugtao4423.lod.R;
import sugtao4423.lod.ShowToast;
import sugtao4423.lod.dataclass.TwitterList;
import sugtao4423.lod.tweetlistview.EndlessScrollListener;
import sugtao4423.lod.tweetlistview.TweetListAdapter;
import sugtao4423.lod.tweetlistview.TweetListView;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

public class Fragment_List extends Fragment{

    public static final String LIST_INDEX = "listIndex";

    private App app;
    private TwitterList thisList;
    private SwipeRefreshLayout pulltoRefresh;
    private int listIndex;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        listIndex = getArguments().getInt(LIST_INDEX);
        View v = inflater.inflate(R.layout.fragment_list, container, false);
        app = (App)container.getContext().getApplicationContext();
        thisList = app.getLists(container.getContext())[listIndex];
        TweetListView list = (TweetListView)v.findViewById(R.id.listLine);
        final TweetListAdapter adapter = thisList.getTweetListAdapter();
        adapter.setOnItemClickListener(new ListViewListener());
        adapter.setOnItemLongClickListener(new ListViewListener());
        list.setAdapter(adapter);

        final EndlessScrollListener scrollListener = getLoadMoreListener(list.getLinearLayoutManager());
        list.addOnScrollListener(scrollListener);

        pulltoRefresh = (SwipeRefreshLayout)v.findViewById(R.id.ListPull);
        pulltoRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
        pulltoRefresh.setOnRefreshListener(new OnRefreshListener(){
            @Override
            public void onRefresh(){
                adapter.clear();
                thisList.setIsAlreadyLoad(false);
                getList();
                scrollListener.resetState();
            }
        });
        return v;
    }

    public EndlessScrollListener getLoadMoreListener(LinearLayoutManager llm){
        return new EndlessScrollListener(llm){

            @Override
            public void onLoadMore(int current_page){
                if(thisList.getTweetListAdapter().getItemCount() > 30){
                    getList();
                }
            }
        };
    }

    public void getList(){
        new AsyncTask<Void, Void, ResponseList<Status>>(){
            @Override
            protected ResponseList<twitter4j.Status> doInBackground(Void... params){
                try{
                    if(thisList.getIsAlreadyLoad()){
                        long lastTweetId = thisList.getTweetListAdapter().getItem(thisList.getTweetListAdapter().getItemCount() - 1).getId();
                        return app.getTwitter().getUserListStatuses(thisList.getListId(), new Paging(1, 50).maxId(lastTweetId - 1));
                    }else{
                        return app.getTwitter().getUserListStatuses(thisList.getListId(), new Paging(1, 50));
                    }
                }catch(TwitterException e){
                    return null;
                }
            }

            @Override
            protected void onPostExecute(ResponseList<twitter4j.Status> result){
                if(result != null){
                    thisList.getTweetListAdapter().addAll(result);
                    thisList.setIsAlreadyLoad(true);
                }else{
                    new ShowToast(getContext().getApplicationContext(), R.string.error_getList);
                }
                pulltoRefresh.setRefreshing(false);
                pulltoRefresh.setEnabled(true);
            }
        }.execute();
    }

}