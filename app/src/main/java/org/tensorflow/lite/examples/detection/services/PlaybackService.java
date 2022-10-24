package org.tensorflow.lite.examples.detection.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import org.tensorflow.lite.examples.aidl.IPlaybackService;

public class PlaybackService extends Service {

    private final IBinder mNinder = new ServiceStub(this);

    private MediaPlayback mediaPlayback;

    private AudioManager mAudioManager;

    private MediaPlaybackHandler mPlayerHandler;

    private AudioManager.OnAudioFocusChangeListener mAudioFocusListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(final int focusChange) {
            mPlayerHandler.obtainMessage(FOCUS_CHANGE, focusChange, 0).sendToTarget();
        }
    };

    private static final int FOCUS_CHANGE = 10;
    private static final int FADE_DOWN = 11;
    private static final int FADE_UP = 12;
    private static final int SERVER_DIED = 13;

    private boolean mPausedByTransientLossOfFocus = false;
    private boolean mIsSupposedToBePlaying = false;

    @Override
    public void onCreate() {
        super.onCreate();

        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work will not disrupt the UI.
        final HandlerThread thread = new HandlerThread("MediaPlaybackHandler",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Initialize the handlers
        mPlayerHandler = new MediaPlaybackHandler(this, thread.getLooper());

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        Log.i("PlabackService", "MediaPlayback class instantiated");
        mediaPlayback = new MediaPlayback();
        mediaPlayback.setHandler(mPlayerHandler);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.w("PlaybackService", "Destroying service");
        mediaPlayback.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mNinder;
    }


    /**
     * Provides an interface for dealing with playback of audio files
     */
    private class MediaPlayback implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

        private MediaPlayer mPlayer = new MediaPlayer();

        private Handler mHandler;

        private float mVolume;

        private boolean mIsPlayerInitialised = false;

        public void setHandler(Handler handler) {
            mHandler = handler;
        }

        public void start() {
            mPlayer.start();
        }

        public void stop() {
            mPlayer.reset();
            mIsSupposedToBePlaying = false;
        }

        /**
         * You CANNOT use this player after calling release
         */
        public void release() {
            stop();
            mPlayer.release();
        }

        public void pause() {
            mPlayer.pause();
        }

        public long getDuration() {
            if (mPlayer != null && mIsPlayerInitialised) {
                return mPlayer.getDuration();
            }
            return -1;
        }

        public long getPosition() {
            if (mPlayer != null && mIsPlayerInitialised) {
                return mPlayer.getCurrentPosition();
            }
            return 0;
        }

        public void seek(long whereTo) {
            mPlayer.seekTo((int) whereTo);
        }

        public void setVolume(float volume)
        {
            mPlayer.setVolume(volume, volume);
            this.mVolume = volume;
        }

        public float getVolume()
        {
            return mVolume;
        }

        public void setDataSource(String path) {
            Log.e("Patha", path);

            mIsPlayerInitialised = setDataSource(mPlayer, path);
        }

        private boolean setDataSource(MediaPlayer mediaPlayer, String path) {
            Log.e("Patha", path);

            try {
                AssetFileDescriptor afd = getAssets().openFd(path);
                mediaPlayer.reset();
                mediaPlayer.setOnPreparedListener(null);
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.prepare();
                mediaPlayer.setOnCompletionListener(this);
                mediaPlayer.setOnErrorListener(this);

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
                return false;
            } catch (SecurityException ex) {
                ex.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            mPlayer.release();
            mPlayer = null;
        }

        @Override
        public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {

            Log.e("PlaybackService", "Error: " + what);

            switch (what) {
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED: {

                    mIsPlayerInitialised = false;
                    mPlayer.release();
                    mPlayer = new MediaPlayer();

                    mHandler.sendMessageDelayed(mHandler.obtainMessage(SERVER_DIED), 2000);
                    return true;
                }
            }

            return false;
        }
    }

    public void stop() {

        //Todo: Fade down nicely

        synchronized (this) {
            if (mediaPlayback != null && mediaPlayback.mIsPlayerInitialised) {
                mediaPlayback.stop();
            }
        }
    }

    public void play() {
        synchronized (this) {
            mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (mediaPlayback.mIsPlayerInitialised) {
                mediaPlayback.start();
                mPlayerHandler.removeMessages(FADE_DOWN);
                mPlayerHandler.sendEmptyMessage(FADE_UP);
                mIsSupposedToBePlaying = true;
            }
        }
    }

    public void pause() {

        //Todo: Fade down nicely

        synchronized (this) {
            mPlayerHandler.removeMessages(FADE_UP);
            if (mIsSupposedToBePlaying) {
                mediaPlayback.pause();
                mIsSupposedToBePlaying = false;
                mPausedByTransientLossOfFocus = false;
            }
        }
    }

    public boolean openFile(String path) {
        synchronized (this) {
            if (path == null) {
                return false;
            }
            mediaPlayback.setDataSource(path);
            if (mediaPlayback.mIsPlayerInitialised) {
                return true;
            }
            stop();
            return false;
        }
    }

    public long getDuration() {
        synchronized (this) {
            if (mediaPlayback != null && mediaPlayback.mIsPlayerInitialised) {
                return mediaPlayback.getDuration();
            }
        }
        return -1;
    }

    public long getPosition() {
        synchronized (this) {
            if (mediaPlayback != null && mediaPlayback.mIsPlayerInitialised) {
                return mediaPlayback.getPosition();
            }
        }
        return 0;
    }

    public void seek(long pos) {
        synchronized (this) {
            if (mediaPlayback != null && mediaPlayback.mIsPlayerInitialised) {
                if (pos < 0) {
                    pos = 0;
                } else if (pos > mediaPlayback.getDuration()) {
                    pos = mediaPlayback.getDuration();
                }
                mediaPlayback.seek(pos);
            }
        }
    }

    private final class MediaPlaybackHandler extends android.os.Handler {

        private final WeakReference<PlaybackService> _service;
        private float mCurrentVolume = 0.8f;

        public MediaPlaybackHandler(final PlaybackService service, final Looper looper) {
            super(looper);
            _service = new WeakReference<>(service);
            MediaPlayback mediaPlayback = _service.get().mediaPlayback;
            if (mediaPlayback != null) {
                mCurrentVolume = mediaPlayback.getVolume();
            }
        }

        @Override
        public void handleMessage(Message msg) {
            final PlaybackService service = _service.get();
            if (service == null) {
                return;
            }

            switch (msg.what) {
                case FOCUS_CHANGE: {
                    switch (msg.arg1) {
                        case FADE_DOWN:
                            mCurrentVolume -= .05f;
                            if (mCurrentVolume > .2f) {
                                sendEmptyMessageDelayed(FADE_DOWN, 10);
                            } else {
                                mCurrentVolume = .2f;
                            }
                            service.mediaPlayback.setVolume(mCurrentVolume);
                            break;
                        case FADE_UP:
                            //Todo: Only fade up to original volume
                            mCurrentVolume += .01f;
                            if (mCurrentVolume < 1.0f) {
                                sendEmptyMessageDelayed(FADE_UP, 10);
                            } else {
                                mCurrentVolume = 1.0f;
                            }
                            service.mediaPlayback.setVolume(mCurrentVolume);
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS:
                            if (service.mIsSupposedToBePlaying) {
                                service.mPausedByTransientLossOfFocus = false;
                            }
                            service.pause();
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            removeMessages(FADE_UP);
                            sendEmptyMessage(FADE_DOWN);
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                            if (service.mIsSupposedToBePlaying) {
                                service.mPausedByTransientLossOfFocus = true;
                            }
                            service.pause();
                            break;
                        case AudioManager.AUDIOFOCUS_GAIN:
                            if (!service.mIsSupposedToBePlaying
                                    && service.mPausedByTransientLossOfFocus) {
                                service.mPausedByTransientLossOfFocus = false;
                                mCurrentVolume = 0f;
                                service.mediaPlayback.setVolume(mCurrentVolume);
                                service.play(); // also queues a fade-in
                            } else {
                                removeMessages(FADE_DOWN);
                                sendEmptyMessage(FADE_UP);
                            }
                            break;
                        default:
                            //App.log("PlaybackService: " +  "Unknown audio focus change code");
                    }
                    break;
                }
            }
        }
    }

    private static class ServiceStub extends IPlaybackService.Stub {

        private final WeakReference<PlaybackService> _service;

        private ServiceStub(final PlaybackService service) {
            _service = new WeakReference<>(service);
        }

        @Override
        public void stop() throws RemoteException {
            _service.get().stop();
        }

        @Override
        public void play() throws RemoteException {
            _service.get().play();
        }

        @Override
        public void pause() throws RemoteException {
            _service.get().pause();
        }

        @Override
        public boolean openFile(String path) throws RemoteException {
            return _service.get().openFile(path);
        }

        @Override
        public long getDuration() throws RemoteException {
            return _service.get().getDuration();
        }


        @Override
        public long getPosition() throws RemoteException {
            return _service.get().getPosition();
        }

        @Override
        public void seek(long pos) throws RemoteException {
            _service.get().seek(pos);
        }

        @Override
        public boolean isPlaying() {
            return _service.get().mIsSupposedToBePlaying;
        }
    }

}