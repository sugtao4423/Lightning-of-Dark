package MainFragment;

import com.tao.lightning_of_dark.CustomAdapter;
import com.tao.lightning_of_dark.ListViewListener;
import com.tao.lightning_of_dark.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class Fragment_mention extends Fragment {
	
	public static ListView mention;
	
	@Override
	  public View onCreateView(LayoutInflater inflater,
	    ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_mention, null);
		mention = (ListView)v.findViewById(R.id.MentionLine);
		mention.setOnItemClickListener(new ListViewListener());
		mention.setOnItemLongClickListener(new ListViewListener());
	    return v;
	}
	public void setMention(CustomAdapter adapter){
		mention.setAdapter(adapter);
	}
}