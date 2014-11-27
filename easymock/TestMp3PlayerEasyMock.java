/**
 * Excerpted from the book, "Pragmatic Unit Testing"
 * ISBN 0-9745140-1-2
 * Copyright 2003 The Pragmatic Programmers, LLC.  All Rights Reserved.
 * Visit www.PragmaticProgrammer.com
 */

import junit.framework.*;
import java.util.ArrayList;
import static org.easymock.EasyMock.*;
//import org.easymock.MockControl;

public class TestMp3PlayerEasyMock extends TestCase {
    
    protected Mp3Player mp3;
    protected ArrayList list = new ArrayList();
    
    public void setUp() {
        
        mp3=createMock(Mp3Player.class);
        
        list = new ArrayList();
        list.add("Bill Chase -- Open Up Wide");
        list.add("Jethro Tull -- Locomotive Breath");
        list.add("The Boomtown Rats -- Monday");
        list.add("Carl Orff -- O Fortuna");
        
    }
    
    public void testPlay() {
        System.out.println("Easymock testPlay.");
        mp3.loadSongs(list);
        expectLastCall();
        expect(mp3.isPlaying()).andReturn(false);
        mp3.play();
        expectLastCall();
        expect(mp3.isPlaying()).andReturn(true);
        expect(mp3.currentPosition()).andReturn(0.1);
        mp3.pause();
        expectLastCall();
        expect(mp3.currentPosition()).andReturn(0.1);
        mp3.stop();
        expectLastCall();
        expect(mp3.currentPosition()).andReturn(0.0);
        replay(mp3);
        
        mp3.loadSongs(list);
        assertFalse(mp3.isPlaying());
        mp3.play();
        assertTrue(mp3.isPlaying());
        assertTrue(mp3.currentPosition() != 0.0);
        mp3.pause();
        assertTrue(mp3.currentPosition() != 0.0);
        mp3.stop();
        assertEquals(mp3.currentPosition(), 0.0, 0.1);
        
        
        verify(mp3); //verify the number of times that the functions are called
    }
    
    public void testPlayNoList() {
        System.out.println("Easymock testPlayNoList.");
        expect(mp3.isPlaying()).andReturn(false);
        mp3.play();
        expectLastCall();
        expect(mp3.isPlaying()).andReturn(false);
        expect(mp3.currentPosition()).andReturn(0.0);
        mp3.pause();
        expectLastCall();
        expect(mp3.currentPosition()).andReturn(0.0);
        expect(mp3.isPlaying()).andReturn(false);
        mp3.stop();
        expectLastCall();
        expect(mp3.currentPosition()).andReturn(0.0);
        expect(mp3.isPlaying()).andReturn(false);
        replay(mp3);
        
        // Don't set the list up
        assertFalse(mp3.isPlaying());
        mp3.play();
        assertFalse(mp3.isPlaying());
        assertEquals(mp3.currentPosition(), 0.0, 0.1);
        mp3.pause();
        assertEquals(mp3.currentPosition(), 0.0, 0.1);
        assertFalse(mp3.isPlaying());
        mp3.stop();
        assertEquals(mp3.currentPosition(), 0.0, 0.1);
        assertFalse(mp3.isPlaying());
    }
    

    
    public void testAdvance() {
        System.out.println("Easymock testAdvance.");
        mp3.loadSongs(list);
        expectLastCall();
        mp3.play();
        expectLastCall();
        expect(mp3.isPlaying()).andReturn(true);
        mp3.prev();
        expectLastCall();
        expect(mp3.currentSong()).andReturn("Bill Chase -- Open Up Wide");
        expect(mp3.isPlaying()).andReturn(true);
        mp3.next();
        expectLastCall();
        expect(mp3.currentSong()).andReturn("Jethro Tull -- Locomotive Breath");
        mp3.next();
        expectLastCall();
        expect(mp3.currentSong()).andReturn("The Boomtown Rats -- Monday");
        mp3.prev();
        expectLastCall();
        expect(mp3.currentSong()).andReturn("Jethro Tull -- Locomotive Breath");
        mp3.next();
        expectLastCall();
        expect(mp3.currentSong()).andReturn("The Boomtown Rats -- Monday");
        mp3.next();
        expectLastCall();
        expect(mp3.currentSong()).andReturn("Carl Orff -- O Fortuna");
        mp3.next();
        expectLastCall();
        expect(mp3.currentSong()).andReturn("Carl Orff -- O Fortuna");
        expect(mp3.isPlaying()).andReturn(true);
        replay(mp3);
        
        
        mp3.loadSongs(list);
        
        mp3.play();
        
        assertTrue(mp3.isPlaying());
        
        mp3.prev();
        assertEquals(mp3.currentSong(), list.get(0));
        assertTrue(mp3.isPlaying());
        
        mp3.next();
        assertEquals(mp3.currentSong(), list.get(1));
        mp3.next();
        assertEquals(mp3.currentSong(), list.get(2));
        mp3.prev();
        
        assertEquals(mp3.currentSong(), list.get(1));
        mp3.next();
        assertEquals(mp3.currentSong(), list.get(2));
        mp3.next();
        assertEquals(mp3.currentSong(), list.get(3));
        mp3.next();
        assertEquals(mp3.currentSong(), list.get(3));
        assertTrue(mp3.isPlaying());
    }
    
}