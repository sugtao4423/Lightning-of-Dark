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

public class Fragment_home extends Fragment {
	
	public static ListView home;
	
	@Override
	  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_home, null);
		home = (ListView)v.findViewById(R.id.HomeLine);
		home.setOnItemClickListener(new ListViewListener());
		home.setOnItemLongClickListener(new ListViewListener());
	    return v;
	  }
	
	public void setHome(CustomAdapter adapter){
		home.setAdapter(adapter);
	}
}
