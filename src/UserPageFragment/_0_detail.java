package UserPageFragment;

import com.tao.lightning_of_dark.R;
import com.tao.lightning_of_dark.UserPage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class _0_detail extends Fragment {
	
	static TextView UserBio, location, Link;
	
	@Override
	  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.user_0, null);
		UserBio = (TextView)v.findViewById(R.id.UserBio);
		location = (TextView)v.findViewById(R.id.location);
		Link = (TextView)v.findViewById(R.id.link);
		return v;
	}
	public void setText(){
		UserBio.setText(UserPage.target.getDescription());
		location.setText(UserPage.target.getLocation());
		Link.setText(UserPage.target.getURL());
	}

}
