package com.kabouzeid.gramophone.ui.activities.base;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.misc.SmallOnGestureListener;
import com.kabouzeid.gramophone.model.MusicRemoteEvent;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.squareup.otto.Subscribe;

/**
 * Created by karim on 22.01.15.
 */
public abstract class AbsFabActivity extends AbsBaseActivity {
    public static final String TAG = AbsFabActivity.class.getSimpleName();
    private FloatingActionButton fab;
    private Object busEventListener = new Object() {
        @Subscribe
        public void onBusEvent(MusicRemoteEvent event) {
            onMusicRemoteEvent(event);
        }
    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        try {
            App.bus.register(busEventListener);
        } catch (Exception ignored) {
        }
        setUpFab();
    }

    private void setUpFab() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            getFab().setImageResource(R.drawable.ic_pause_resume);
        }
        updateFabState();
        final GestureDetector gestureDetector = new GestureDetector(this, new SmallOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                NavigationUtil.openCurrentPlayingIfPossible(AbsFabActivity.this, getSharedViewsWithFab(null));
                return true;
            }
        });

        getFab().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MusicPlayerRemote.getPosition() != -1) {
                    if (MusicPlayerRemote.isPlaying()) {
                        MusicPlayerRemote.pauseSong();
                    } else {
                        MusicPlayerRemote.resumePlaying();
                    }
                } else {
                    Toast.makeText(AbsFabActivity.this, getResources().getString(R.string.nothing_playing), Toast.LENGTH_SHORT).show();
                }
            }
        });

        getFab().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return false;
            }
        });
    }

    private void updateFabState() {
        if (MusicPlayerRemote.isPlaying()) {
            setFabPause();
        } else {
            setFabPlay();
        }
    }

    protected FloatingActionButton getFab() {
        if (fab == null) {
            fab = (FloatingActionButton) findViewById(R.id.fab);
            if (fab == null) {
                fab = new FloatingActionButton(this);
                Log.e(getTag(), "No FAB found created default FAB.");
            }
        }
        return fab;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateControllerState();
    }

    @Override
    public void enableViews() {
        super.enableViews();
        getFab().setEnabled(true);
    }

    @Override
    public void disableViews() {
        super.disableViews();
        getFab().setEnabled(false);
    }

    public Pair[] getSharedViewsWithFab(Pair[] sharedViews) {
        Pair[] sharedViewsWithFab;
        if (sharedViews != null) {
            sharedViewsWithFab = new Pair[sharedViews.length + 1];
            for (int i = 0; i < sharedViews.length; i++) {
                sharedViewsWithFab[i] = sharedViews[i];
            }
        } else {
            sharedViewsWithFab = new Pair[1];
        }
        sharedViewsWithFab[sharedViewsWithFab.length - 1] = Pair.create((View) getFab(), getString(R.string.transition_fab));
        return sharedViewsWithFab;
    }

    protected void updateControllerState() {
        updateFabState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            App.bus.unregister(busEventListener);
        } catch (Exception ignored) {
        }
    }

    public void onMusicRemoteEvent(MusicRemoteEvent event) {
        switch (event.getAction()) {
            case MusicRemoteEvent.PLAY:
                setFabPause();
                break;
            case MusicRemoteEvent.PAUSE:
                setFabPlay();
                break;
            case MusicRemoteEvent.RESUME:
                setFabPause();
                break;
            case MusicRemoteEvent.STOP:
                setFabPlay();
                break;
            case MusicRemoteEvent.QUEUE_COMPLETED:
                setFabPlay();
                break;
        }
    }

    private void setFabPlay(){
        getFab().setSelected(true);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            getFab().setImageResource(R.drawable.ic_play_arrow_white_24dp);
        }
    }

    private void setFabPause(){
        getFab().setSelected(false);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            getFab().setImageResource(R.drawable.ic_pause_white_24dp);
        }
    }
}
