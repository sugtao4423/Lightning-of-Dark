package sugtao4423.lod.swipe_image_viewer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.tenthbit.view.ZoomImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import sugtao4423.lod.R;

public class ImageFragment extends Fragment{

    private FrameLayout parentLayout;
    private ZoomImageView image;
    private ProgressBar progressBar;
    private String url;
    private byte[] nonOrigImage;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        url = bundle.getString(ImagePagerAdapter.BUNDLE_KEY_URL);

        parentLayout = new FrameLayout(getContext());
        image = new ZoomImageView(getContext());

        progressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setIndeterminate(true);
        progressBar.setScaleY(1.5f);
        progressBar.setVisibility(View.VISIBLE);

        FrameLayout.LayoutParams imageLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        parentLayout.addView(image, imageLayoutParams);

        FrameLayout.LayoutParams barLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        barLayoutParams.setMargins(64, 0, 64, 0);
        parentLayout.addView(progressBar, barLayoutParams);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        new AsyncTask<String, Void, Bitmap>(){

            @Override
            protected Bitmap doInBackground(String... params){
                try{
                    URL url = new URL(params[0]);
                    HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream is = connection.getInputStream();
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while((len = is.read(buffer)) > 0){
                        bout.write(buffer, 0, len);
                    }
                    nonOrigImage = bout.toByteArray();
                    Bitmap myBitmap = BitmapFactory.decodeByteArray(nonOrigImage, 0, nonOrigImage.length);
                    is.close();
                    bout.close();
                    connection.disconnect();
                    return myBitmap;
                }catch(IOException e){
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Bitmap result){
                if(result != null){
                    progressBar.setVisibility(View.GONE);
                    image.setImageBitmap(result);
                }else{
                    Toast.makeText(getActivity(), R.string.error_get_image, Toast.LENGTH_LONG).show();
                }
            }
        }.execute(url);

        return parentLayout;
    }

    public byte[] getNonOrigImage(){
        return nonOrigImage;
    }

}
