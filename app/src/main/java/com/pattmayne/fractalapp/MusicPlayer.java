/**
 * Copyright 2014 Matthew Payne

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0
 or in the assets folder of this application.

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.pattmayne.fractalapp;

import android.content.Context;
import android.media.MediaPlayer;

import java.util.Random;

/**
 * Created by Matt on 2014-11-25.
 */
public class MusicPlayer {

    //Variables

    private Random randomizer;
    private Context context;

    private MediaPlayer musicPlayer;

    private int[] drumTrackList = {0,0,0,0,0,0,0,0,0};
    private int[] noiseTrackList = {0,0,0,0,0,0,0,0};
    private int[] currentTrackList;

    private int currentDrumTrack = 0;
    private int currentNoiseTrack = 0;

    private int nextTrackCountdown = 1;
    private boolean trackSwitcher = true;
    private boolean playMusic = false;


    /**
     * Constructor
     */
    public MusicPlayer(Context context)
    {
        this.context = context;
        randomizer = new Random();
        prepareTracks();
    }



    //The next few methods control the music.
    //There are rhythm tracks and noise tracks, and they should be played in an alternating sequence.

    /**
     * This method fills values in the integer arrays for drumTrackList and noiseTrackList.
     * The integer values refer to audio resource files.
     * The MediaPlayer object can now access those audio files through these integer arrays.
     */
    public void prepareTracks()
    {
        drumTrackList[0] = R.raw.drums_1;
        drumTrackList[1] = R.raw.drums_2;
        drumTrackList[2] = R.raw.drums_3;
        drumTrackList[3] = R.raw.drums_4;
        drumTrackList[4] = R.raw.drums_5;
        drumTrackList[5] = R.raw.drums_6;
        drumTrackList[6] = R.raw.drums_7;
        drumTrackList[7] = R.raw.drums_8;
        drumTrackList[8] = R.raw.drums_9;

        noiseTrackList[0] = R.raw.noise_1;
        noiseTrackList[1] = R.raw.noise_2;
        noiseTrackList[2] = R.raw.noise_3;
        noiseTrackList[3] = R.raw.noise_4;
        noiseTrackList[4] = R.raw.noise_5;
        noiseTrackList[5] = R.raw.noise_6;
        noiseTrackList[6] = R.raw.noise_7;
        noiseTrackList[7] = R.raw.noise_8;

        //create a dummy MediaPlayer object, just so the variable is not null.
        musicPlayer = MediaPlayer.create(context, drumTrackList[0]);
    }

    /**
     * When an object begins playing music, it calls this method first to randomize the playlists.
     */
    public void shuffleTracks()
    {
        musicPlayer.release();

        currentDrumTrack = 0;
        currentNoiseTrack = 0;

        shuffleArray(noiseTrackList);
        shuffleArray(drumTrackList);
    }

    /**
     * This method uses the "Fisher-Yates Shuffle" to randomize any integer array fed into it.
     * This lets us randomize our playlist.
     * @param arrayToShuffle
     */
    private void shuffleArray(int[] arrayToShuffle)
    {
        for (int i = arrayToShuffle.length - 1; i > 0; i--)
        {
            int randomIndex = randomizer.nextInt(i + 1);

            int swapper = arrayToShuffle[randomIndex];
            arrayToShuffle[randomIndex] = arrayToShuffle[i];
            arrayToShuffle[i] = swapper;
        }
    }


    /**
     * This method chooses a random drumTrack, begins playing the track,
     * and sets up a listener to perform certain functions when the track is finished playing.
     *
     * The OnCompletionListener should tell the next loop to play another track.
     */
    public void playTrack()
    {
        if (trackSwitcher)
        {
            musicPlayer = MediaPlayer.create(context, drumTrackList[currentDrumTrack]);

            currentDrumTrack++;
            if (currentDrumTrack >= drumTrackList.length)
            {
                currentDrumTrack = 0;
            }
        }
        else
        {
            musicPlayer = MediaPlayer.create(context, noiseTrackList[currentNoiseTrack]);

            currentNoiseTrack++;
            if (currentNoiseTrack >= noiseTrackList.length)
            {
                currentNoiseTrack = 0;
            }
        }

        trackSwitcher = !trackSwitcher;

        musicPlayer.setVolume(0.047f, 0.047f);
        musicPlayer.setLooping(false);
        musicPlayer.start();

        //When the track is over I want the OnCompletionListener to tell the Thread to play another track.
        musicPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {

                musicPlayer.release();
                playTrack();
            };
        });
    }

    public void skipTrack()
    {
        musicPlayer.release();
        playTrack();
    }

    public void stopMusic()
    {
        playMusic = false;
        musicPlayer.release();
    }
}
