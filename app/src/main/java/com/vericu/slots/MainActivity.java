package com.vericu.slots;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity{

    private int comboNumber = 7;
    private int coef1 = 72;
    private int coef2 = 142;
    private int coef3 = 212;
    private int position1 = 5;
    private int position2 = 5;
    private int position3 = 5;
    private int[] slot = {1, 2, 3, 4, 5, 6, 7};

    private RecyclerView rv1;
    private RecyclerView rv2;
    private RecyclerView rv3;
    private SpinnerAdapter adapter;
    private CustomLayoutManager layoutManager1;
    private CustomLayoutManager layoutManager2;
    private CustomLayoutManager layoutManager3;

    private ImageButton spinButton;
    private ImageView plusButton;
    private ImageView settingsButton;
    private TextView jackpot;
    private TextView myCoins;
    private TextView lines;
    private TextView bet;
    int myCoins_val;
    int bet_val;
    int jackpot_val;

    private boolean firstRun;

    private Game_Logic gameLogic;

    private boolean soundOn, soundOn1;
    private SharedPreferences pref;
    public MediaPlayer mp,win,bgsound;
    private SoundPool mSoundPool;
    private int mStreamId;
    int soundclick,soundspinbg,spinstop;
    private int countads;
    public static final String PREFS_NAME = "FirstRun";

    private InterstitialAd mInterstitialAd;

    private int playmusic, playsound;
    private ImageView music_off, music_on , soundon, soundoff;
    private AdView mAdView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        int densityDpi = metrics.densityDpi;

        if (densityDpi == 320) {
            setContentView(R.layout.activity_main);
        } if (densityDpi == 480) {
            setContentView(R.layout.activity_main);
        }if (densityDpi == 640) {
            setContentView(R.layout.activity_main);
        }else {
            setContentView(R.layout.activity_main);
        }


        mSoundPool = new SoundPool (5, AudioManager.STREAM_MUSIC, 100);
        soundclick = mSoundPool.load(this, R.raw.click, 1);
        soundspinbg = mSoundPool.load(this, R.raw.spin_bg, 1);
        spinstop = mSoundPool.load(this, R.raw.spin_stop, 1);
        final Typeface typeface = ResourcesCompat.getFont(this, R.font.font);

        MobileAds.initialize(this, "ca-app-pub-4764434047050617~4488275898");
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-4764434047050617/7086519075");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        bgsound = MediaPlayer.create(this,R.raw.bg_music);
        bgsound.setLooping(true);
        mp = MediaPlayer.create(this, R.raw.spin);
        win = MediaPlayer.create(this, R.raw.win);

        pref = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
       firstRun = pref.getBoolean("firstRun", true);



        if (firstRun) {
            playmusic = 1;
            playsound = 1;
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("firstRun", false);
            editor.apply();
        } else {
            playmusic= pref.getInt("music", 1);
            playsound = pref.getInt("sound", 1);
            checkmusic();

        }

        Log.d("MUSIC",String.valueOf(playmusic));



        //Initializations
        gameLogic = new Game_Logic();
        settingsButton = findViewById(R.id.settings);
        spinButton = findViewById(R.id.spinButton);
        plusButton = findViewById(R.id.plusButton);
        jackpot = findViewById(R.id.jackpot);
        jackpot.setTypeface(typeface);
        myCoins = findViewById(R.id.myCoins);
        myCoins.setTypeface(typeface);
        bet = findViewById(R.id.bet);
        bet.setTypeface(typeface);
        adapter = new SpinnerAdapter();


        //RecyclerView settings
        rv1 = findViewById(R.id.spinner1);
        rv2 = findViewById(R.id.spinner2);
        rv3 = findViewById(R.id.spinner3);
        rv1.setHasFixedSize(true);
        rv2.setHasFixedSize(true);
        rv3.setHasFixedSize(true);


        layoutManager1 = new CustomLayoutManager(this);
        layoutManager1.setScrollEnabled(false);
        rv1.setLayoutManager(layoutManager1);
        layoutManager2 = new CustomLayoutManager(this);
        layoutManager2.setScrollEnabled(false);
        rv2.setLayoutManager(layoutManager2);
        layoutManager3 = new CustomLayoutManager(this);
        layoutManager3.setScrollEnabled(false);
        rv3.setLayoutManager(layoutManager3);

        rv1.setAdapter(adapter);
        rv2.setAdapter(adapter);
        rv3.setAdapter(adapter);
        rv1.scrollToPosition(position1);
        rv2.scrollToPosition(position2);
        rv3.scrollToPosition(position3);

        setText();
        updateText();

        //RecyclerView listeners
        rv1.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        rv1.scrollToPosition(gameLogic.getPosition(0));
                        layoutManager1.setScrollEnabled(false);
                        if(playsound == 1) {
                            mSoundPool.play(spinstop, 1, 1, 0, 0, 1);
                        }
                }
            }
        });

        rv2.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        rv2.scrollToPosition(gameLogic.getPosition(1));
                        layoutManager2.setScrollEnabled(false);
                        if(playsound == 1) {
                            mSoundPool.play(spinstop, 1, 1, 0, 0, 1);
                        }
                }
            }
        });

        rv3.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        rv3.scrollToPosition(gameLogic.getPosition(2));
                        layoutManager3.setScrollEnabled(false);
                        if(playsound == 1) {
                            mSoundPool.play(spinstop, 1, 1, 0, 0, 1);
                        }
                        updateText();
                        if (gameLogic.getHasWon()) {
                            if(playsound == 1){
                                win.start();
                            }
                            countads ++;
                            Log.d("ADS",String.valueOf(countads));
                            if (countads == 2){

                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mInterstitialAd.isLoaded()) {
                                            mInterstitialAd.show();
                                        }
                                    }
                                }, 2000);


                            countads = 0;
                            }

                            LayoutInflater inflater = getLayoutInflater();
                            View layout = inflater.inflate(R.layout.win_splash,
                                    (ViewGroup) findViewById(R.id.win_splash));
                            TextView winCoins = layout.findViewById(R.id.win_coins);
                            TextView win_text = layout.findViewById(R.id.win_text);
                            win_text.setTypeface(typeface);
                            winCoins.setTypeface(typeface);
                            winCoins.setText(gameLogic.getPrize());
                            Toast toast = new Toast(MainActivity.this);
                            toast.setDuration(Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                            toast.setView(layout);
                            toast.show();
                            gameLogic.setHasWon(false);

                        }
                }
            }
        });

        //Button listeners
        spinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playsound == 1){
                mp.start();
                    mSoundPool.play(soundspinbg, 1, 1, 0, 0, 1);
                }
                layoutManager1.setScrollEnabled(true);
                layoutManager2.setScrollEnabled(true);
                layoutManager3.setScrollEnabled(true);
                gameLogic.getSpinResults();
                position1 = gameLogic.getPosition(0) + coef1;
                position2 = gameLogic.getPosition(1) + coef2;
                position3 = gameLogic.getPosition(2) + coef3;
                rv1.smoothScrollToPosition(position1);
                rv2.smoothScrollToPosition(position2);
                rv3.smoothScrollToPosition(position3);

            }
        });

        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playsound == 1){
                    mSoundPool.play(soundclick, 1, 1, 0, 0, 1);
                }
                gameLogic.betUp();
                updateText();
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playsound == 1){
                    mSoundPool.play(soundclick, 1, 1, 0, 0, 1);
                }
                ShowSettingsDialog();
            }
        });
    }


    private void setText(){
        if(firstRun){
         gameLogic.setMyCoins(1000);
         gameLogic.setBet(5);
         gameLogic.setJackpot(100000);

            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("firstRun", false);
            editor.apply();

        }else {
         String coins = pref.getString("coins","");
            String bet = pref.getString("bet","");
            String jackpot = pref.getString("jackpot","");
            Log.d("COINS",coins);
            myCoins_val = Integer.valueOf(coins);
            bet_val = Integer.valueOf(bet);
            jackpot_val = Integer.valueOf(jackpot);
            gameLogic.setMyCoins(myCoins_val);
            gameLogic.setBet(bet_val);
            gameLogic.setJackpot(jackpot_val);
        }
    }


    private void updateText() {
            jackpot.setText(gameLogic.getJackpot());
            myCoins.setText(gameLogic.getMyCoins());
            bet.setText(gameLogic.getBet());

        SharedPreferences.Editor editor = pref.edit();
        editor.putString("coins",gameLogic.getMyCoins());
        editor.putString("bet",gameLogic.getBet());
        editor.putString("jackpot",gameLogic.getJackpot());
        editor.apply();
    }


    private class ItemViewHolder extends RecyclerView.ViewHolder {

        ImageView pic;

        public ItemViewHolder(View itemView) {
            super(itemView);
            pic = itemView.findViewById(R.id.spinner_item);
        }
    }

    private class SpinnerAdapter extends RecyclerView.Adapter<ItemViewHolder> {

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
            View view = layoutInflater.inflate(R.layout.spinner_item, parent, false);
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
            int i = position < 7 ? position : position % comboNumber;
            switch (slot[i]) {
                case 1:
                    holder.pic.setImageResource(R.drawable.combination_1);
                    break;
                case 2:
                    holder.pic.setImageResource(R.drawable.combination_2);
                    break;
                case 3:
                    holder.pic.setImageResource(R.drawable.combination_3);
                    break;
                case 4:
                    holder.pic.setImageResource(R.drawable.combination_4);
                    break;
                case 5:
                    holder.pic.setImageResource(R.drawable.combination_5);
                    break;
                case 6:
                    holder.pic.setImageResource(R.drawable.combination_6);
                    break;
                case 7:
                    holder.pic.setImageResource(R.drawable.combination_7);
                    break;
            }

        }

        @Override
        public int getItemCount() {
            return Integer.MAX_VALUE;
        }
    }

    private void ShowSettingsDialog() {
        final Dialog dialog;


        dialog = new Dialog(this, R.style.WinDialog);
        dialog.getWindow().setContentView(R.layout.settings);

        dialog.getWindow().setGravity(Gravity.CENTER_HORIZONTAL);
        dialog.getWindow().setBackgroundDrawableResource(R.color.transparent);



        ImageView close = (ImageView) dialog.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
            }
        });

       music_on = (ImageView)dialog.findViewById(R.id.music_on);
        music_on.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                playmusic = 0;
                checkmusic();
                music_on.setVisibility(View.INVISIBLE);
                music_off.setVisibility(View.VISIBLE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("music", playmusic);
                editor.apply();
            }
        });

        music_off = (ImageView)dialog.findViewById(R.id.music_off);
        music_off.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                playmusic = 1;
                bgsound.start();
               recreate();
                dialog.show();
                music_on.setVisibility(View.VISIBLE);
                music_off.setVisibility(View.INVISIBLE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("music", playmusic);
                editor.apply();
            }
        });

         soundon = (ImageView)dialog.findViewById(R.id.sounds_on);
         soundon.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                playsound = 0;
                soundon.setVisibility(View.INVISIBLE);
                soundoff.setVisibility(View.VISIBLE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("sound", playsound);
                editor.apply();
            }
        });

        soundoff = (ImageView)dialog.findViewById(R.id.sounds_off);
        soundoff.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                playsound = 1;
                recreate();
                dialog.show();
                soundon.setVisibility(View.INVISIBLE);
                soundoff.setVisibility(View.VISIBLE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("sound", playsound);
                editor.apply();
            }
        });

        checkmusicdraw();
        checksounddraw();

        dialog.show();
    }

    @Override
    public void onPause() {
        super.onPause();
        bgsound.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkmusic();

    }


    private void checkmusic(){
        if (playmusic == 1){
            bgsound.start();
        }
        else {
            bgsound.pause();
        }

    }

    private void checkmusicdraw(){
        if (playmusic == 1){
            music_on.setVisibility(View.VISIBLE);
            music_off.setVisibility(View.INVISIBLE);
        }
        else {
            music_on.setVisibility(View.INVISIBLE);
            music_off.setVisibility(View.VISIBLE);
        }
    }

    private void checksounddraw(){
        if (playsound == 1){
            soundon.setVisibility(View.VISIBLE);
            soundoff.setVisibility(View.INVISIBLE);
        }
        else {
            soundon.setVisibility(View.INVISIBLE);
            soundoff.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
       finish();
        System.exit(0);

    }



}

