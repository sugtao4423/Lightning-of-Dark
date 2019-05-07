package sugtao4423.lod;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public abstract class LoDBaseActivity extends AppCompatActivity{

    protected App app;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        app = (App)getApplicationContext();
    }

    @Override
    protected void onResume(){
        super.onResume();
        app.getUseTime().start();
    }

    @Override
    protected void onPause(){
        super.onPause();
        app.getUseTime().stop();
    }

}
