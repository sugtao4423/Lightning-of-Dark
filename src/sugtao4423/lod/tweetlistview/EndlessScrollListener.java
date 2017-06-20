package sugtao4423.lod.tweetlistview;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class EndlessScrollListener extends RecyclerView.OnScrollListener{

	int visibleThreshold = 5;
	int visibleItemCount, totalItemCount;
	private int previousTotal = 0;
	private boolean loading = true;
	private int current_page = 0;

	private LinearLayoutManager mLinearLayoutManager;

	public EndlessScrollListener(LinearLayoutManager linearLayoutManager){
		this.mLinearLayoutManager = linearLayoutManager;
	}

	public void setVisibleThreshold(int visibleThreshold){
		this.visibleThreshold = visibleThreshold;
	}

	@Override
	public void onScrolled(RecyclerView recyclerView, int dx, int dy){
		super.onScrolled(recyclerView, dx, dy);

		visibleItemCount = recyclerView.getChildCount();
		totalItemCount = mLinearLayoutManager.getItemCount();
		int lastVisibleItem = mLinearLayoutManager.findLastVisibleItemPosition();

		if(loading && totalItemCount > previousTotal){
			loading = false;
			previousTotal = totalItemCount;
		}

		if(!loading && (lastVisibleItem + visibleThreshold) > totalItemCount){
			current_page++;
			onLoadMore(current_page);
			loading = true;
		}
	}

	public void resetState() {
        this.current_page = 0;
        this.previousTotal = 0;
        this.loading = true;
    }

	public abstract void onLoadMore(int current_page);
}