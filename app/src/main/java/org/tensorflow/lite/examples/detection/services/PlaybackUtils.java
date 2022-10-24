package org.tensorflow.lite.examples.detection.services;
import android.os.RemoteException;

import org.tensorflow.lite.examples.aidl.IPlaybackService;
import org.tensorflow.lite.examples.detection.env.Logger;

public class PlaybackUtils {
    private static final Logger LOGGER = new Logger();

    public static void openFile(String path) {
        if (ServiceUtils.sService == null) {
            return;
        }
        try {
            ServiceUtils.sService.openFile(path);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void play() {
        LOGGER.d("Entro a play ");

        if (ServiceUtils.sService == null) {
            LOGGER.d("Entro a null");

            return;
        }
        try {
            LOGGER.d("Entro a try ");

            ServiceUtils.sService.play();

        } catch (RemoteException e) {
            LOGGER.d("Entro a error ");

            e.printStackTrace();
        }
    }

    public static void pause() {
        if (ServiceUtils.sService == null) {
            return;
        }
        try {
            ServiceUtils.sService.pause();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        if (ServiceUtils.sService == null) {
            return;
        }
        try {
            ServiceUtils.sService.stop();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static long getDuration() {

        if (ServiceUtils.sService != null) {
            try {
                return ServiceUtils.sService.getDuration();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public static long getPosition() {

        if (ServiceUtils.sService != null) {
            try {
                return ServiceUtils.sService.getPosition();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public static boolean isPlaying() {
        if (ServiceUtils.sService != null) {
            try {
                return ServiceUtils.sService.isPlaying();
            } catch (final RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void seek(long position) {
        if (ServiceUtils.sService != null) {
            try {
                ServiceUtils.sService.seek(position);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
