package org.tensorflow.lite.examples.aidl;


interface IPlaybackService {

    void stop();

    void play();

    void pause();

    boolean openFile(String path);

    long getDuration();

    long getPosition();

    void seek(long pos);

    boolean isPlaying();
}
