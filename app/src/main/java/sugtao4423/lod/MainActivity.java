package sugtao4423.lod;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.View;

import java.io.File;

import sugtao4423.icondialog.IconDialog;
import sugtao4423.icondialog.IconItem;
import sugtao4423.lod.AutoLoadTLService.AutoLoadTLListener;
import sugtao4423.lod.main_fragment.Fragment_home;
import sugtao4423.lod.main_fragment.Fragment_mention;
import sugtao4423.lod.main_fragment.MainFragmentPagerAdapter;
import twitter4j.ResponseList;
import twitter4j.Status;

public class MainActivity extends LoDBaseActivity{

    private boolean resetFlag;

    private Fragment_mention fragmentMention;
    private Fragment_home fragmentHome;

    private Builder iconDialog;

    private MusicReceiver musicReceiver;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        MainFragmentPagerAdapter pagerAdapter = new MainFragmentPagerAdapter(getSupportFragmentManager(), this);

        ViewPager viewPager = (ViewPager)findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(1);
        viewPager.setOffscreenPageLimit(app.getCurrentAccount().getSelectListIds().length + 1);

        PagerTabStrip strip = (PagerTabStrip)findViewById(R.id.mainPagerTabStrip);
        strip.setTabIndicatorColor(Color.parseColor(getString(R.color.pagerTabText)));
        strip.setDrawFullUnderline(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        fragmentHome = pagerAdapter.getFragmentHome();
        fragmentMention = pagerAdapter.getFragmentMention();
        logIn();
        setMusicReceiver();
    }

    public void logIn(){
        if(!app.haveAccount()){
            startActivity(new Intent(this, StartOAuth.class));
            finish();
        }else{
            autoLoadTL();
        }
    }

    public void autoLoadTL(){
        if(app.getCurrentAccount().getAutoLoadTLInterval() == 0){
            return;
        }
        AutoLoadTLListener listener = new AutoLoadTLListener(){

            @Override
            public void onStatus(ResponseList<Status> statuses){
                for(Status s : statuses){
                    fragmentHome.insert(s);
                    if(app.getMentionPattern().matcher(s.getText()).find() && !s.isRetweet()){
                        fragmentMention.insert(s);
                    }
                }
            }
        };
        app.setAutoLoadTLListener(listener);
        Intent intent = new Intent(this, AutoLoadTLService.class);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            startForegroundService(intent);
        }else{
            startService(intent);
        }
    }

    public void setMusicReceiver(){
        musicReceiver = new MusicReceiver();
        IntentFilter filter = new IntentFilter();
        for(String s : MusicReceiver.ACTIONS_GOOGLEPLAY){
            filter.addAction(s);
        }
        for(String s : MusicReceiver.ACTIONS_SONYMUSIC){
            filter.addAction(s);
        }
        registerReceiver(musicReceiver, filter);
    }

    public void newTweet(View v){
        Intent intent = new Intent(MainActivity.this, TweetActivity.class);
        startActivity(intent);
    }

    public void option(View v){
        if(iconDialog == null){
            int black = Color.parseColor(getString(R.color.icon));
            IconItem[] items = new IconItem[6];
            items[0] = new IconItem(getString(R.string.icon_bomb).charAt(0), black, getString(R.string.tweet_bomb));
            items[1] = new IconItem(getString(R.string.icon_search).charAt(0), black, getString(R.string.search_user));
            items[2] = new IconItem(getString(R.string.icon_user).charAt(0), black, getString(R.string.account));
            items[3] = new IconItem(getString(R.string.icon_experience).charAt(0), black, getString(R.string.level_info));
            items[4] = new IconItem(getString(R.string.icon_clock).charAt(0), black, getString(R.string.use_info));
            items[5] = new IconItem(getString(R.string.icon_cog).charAt(0), black, getString(R.string.settings));
            iconDialog = new IconDialog(this).setItems(items, new OptionClickListener(this));
        }
        iconDialog.show();
    }

    public void restart(){
        resetFlag = true;
        finish();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(musicReceiver);
        stopService(new Intent(this, AutoLoadTLService.class));
        app.resetAccount();
        app.closeAccountDB();
        app.closeUseTimeDB();
        if(resetFlag){
            resetFlag = false;
            startActivity(new Intent(this, MainActivity.class));
        }
        clearThumbnailCache();
    }

    public void clearThumbnailCache(){
        File cache = new File(getCacheDir().getAbsolutePath() + "/web_image_cache/");
        if(!cache.exists()){
            return;
        }
        for(File f : cache.listFiles()){
            if(f.getName().startsWith("https+pbs+twimg+com+media+")){
                f.delete();
            }
        }
    }

}