/**
 * Excerpted from the book, "Pragmatic Unit Testing"
 * ISBN 0-9745140-1-2
 * Copyright 2003 The Pragmatic Programmers, LLC.  All Rights Reserved.
 * Visit www.PragmaticProgrammer.com
 */

import java.util.ArrayList;

public class MockMp3Player implements Mp3Player{
    
    /**
     * Begin playing the filename at the top of the
     * play list, or do nothing if playlist
     * is empty.
     */
    
    boolean flag;
    double position;
    ArrayList<String> playlist;
    
    public MockMp3Player(){
        flag=false;
        position=0.0;
        playlist=new ArrayList<String>();
    }
    
    public void play(){
        if(playlist.size()==0){
            return;
        }
        if(!flag){
            flag=true;
            position+=0.1;
        }
    }
    
    /**
     * Pause playing. Play will resume at this spot.
     */
    public void pause(){
        flag=false;
    }
    
    /**
     * Stop playing. The current song remains at the
     * top of the playlist, but rewinds to the
     * beginning of the song.
     */
    public void stop(){
        flag=false;
        position=0.0;
    }
    
    /** Returns the number of seconds into
     * the current song.
     */
    public double currentPosition(){
        return position;
    }
    
    
    
    /**
     * Returns the currently playing file name.
     */
    public String currentSong(){
        return playlist.get((int)position);
        
    }
    
    /**
     * Advance to the next song in the playlist
     * and begin playing it.
     */
    public void next(){
        if(playlist.size()==0){
            return;
        }
        position=(int) position+1;
        position=position>playlist.size()-1?playlist.size()-1:position;
        if(flag){
            position+=0.1;
        }
    }
    
    /**
     * Go back to the previous song in the playlist
     * and begin playing it.
     */
    public void prev(){
        if(playlist.size()==0){
            return;
        }
        position=(int) position-1;
        position=position>0?position:0;
        if(flag){
            position+=0.1;
        }
    }
    
    /**
     * Returns true if a song is currently
     * being played.
     */
    public boolean isPlaying(){
        return flag;
    }
    
    /**
     * Load filenames into the playlist.
     */
    public void loadSongs(ArrayList names){
        playlist=names;
    }
}