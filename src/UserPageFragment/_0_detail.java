package UserPageFragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tao.lightning_of_dark.R;
import com.tao.lightning_of_dark.UserPage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
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
		String bio = UserPage.target.getDescription();
		Matcher at_user = Pattern.compile("@\\w*", Pattern.DOTALL).matcher(bio);
		Matcher url = Pattern.compile("(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+", Pattern.DOTALL).matcher(bio);
		if(at_user.find() || url.find()){
			at_user.reset();
			url.reset();
			if(at_user.find()){
				at_user.reset();
				while(at_user.find())
					bio = bio.replace(at_user.group(), "<a href=\"https://twitter.com/" + at_user.group().substring(1) + "\">" + at_user.group() + "</a>");
			}
			if(url.find()){
				url.reset();
				while(url.find())
					bio = bio.replace(url.group(), "<a href=\"" + url.group() + "\">" + url.group() + "</a>");
			}
			UserBio.setMovementMethod(LinkMovementMethod.getInstance());
			UserBio.setText(Html.fromHtml(bio));
		}else
			UserBio.setText(bio);
		location.setText(UserPage.target.getLocation());
		Link.setText(UserPage.target.getURL());
	}

}
