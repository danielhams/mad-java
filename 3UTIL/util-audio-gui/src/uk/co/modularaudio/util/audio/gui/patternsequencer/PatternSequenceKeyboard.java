/**
 *
 * Copyright (C) 2015 - Daniel Hams, Modular Audio Limited
 *                      daniel.hams@gmail.com
 *
 * Mad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mad.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.co.modularaudio.util.audio.gui.patternsequencer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.JPanel;

import uk.co.modularaudio.util.audio.gui.patternsequencer.model.PatternSequenceModel;
import uk.co.modularaudio.util.audio.midi.MidiNote;
import uk.co.modularaudio.util.audio.midi.MidiUtils;

public class PatternSequenceKeyboard extends JPanel
{
	private static final long serialVersionUID = -1573972550711034355L;
	
//	private static Log log = LogFactory.getLog( PatternSequenceKeyboard.class.getName() );
	
	private Dimension blockDimensions = null;
	
	public final static int KEYBOARD_WIDTH = 42;
	
	private int numMidiNotes = -1;
	private Dimension size = null;
	
	private Color blockColour = null;
	
	private Rectangle curClipBounds = new Rectangle( -1, -1, -1, -1);

	public PatternSequenceKeyboard( PatternSequenceModel dataModel,
			Dimension blockDimensions,
			Color backgroundColour,
			Color gridColour,
			Color blockColour )
	{
		this.setOpaque( true );
		
		this.blockDimensions = blockDimensions;
		this.blockColour = blockColour;
		
		numMidiNotes = MidiUtils.getNumMidiNotes();
		
		size = new Dimension( KEYBOARD_WIDTH, (numMidiNotes * blockDimensions.height) + 1 );
		this.setMinimumSize( size );
		this.setSize( size );
		this.setPreferredSize( size );
		this.setMaximumSize( size );
		
		this.setBackground( backgroundColour );
	}

	@Override
	public void paint( Graphics g )
	{
		super.paint( g );
		getBounds( curClipBounds );
		g.getClipBounds( curClipBounds );
//		log.debug("Cur clip bounds are " + curClipBounds.toString() );

		int screenRes = Toolkit.getDefaultToolkit().getScreenResolution();
		float fontSize = Math.round( 8.0 * screenRes / 72.0 );
		
		Font origFont = g.getFont();
		Font replacementFont = origFont.deriveFont( fontSize );
		g.setFont( replacementFont );
		
		paintKeys( g );
	}
	
	private void paintKeys( Graphics g )
	{
		int pixelsPerKey = blockDimensions.height;
		int startKey = curClipBounds.y / pixelsPerKey;
		int endKey = (curClipBounds.y + curClipBounds.height) / pixelsPerKey;
//		log.debug("So will paint from key " + startKey + " to " + endKey );
		
		// First pass, white keys
		for( int curKey = startKey -1 ; curKey <= endKey + 1 ; curKey++ )
		{
			if( curKey < 0 || curKey > (numMidiNotes - 1) )
			{
				continue;
			}
			int graphicsYCorrectedKey = (numMidiNotes - 1) - curKey;

			MidiNote mn = MidiUtils.getMidiNoteFromNumberReturnNull( graphicsYCorrectedKey );
			if( mn == null )
			{
				break;
			}
			boolean isWhite = mn.isWhiteKey();
			
			int previousKey = graphicsYCorrectedKey - 1;
			boolean previousIsWhite = true;
			if( previousKey >= 0 )
			{
				MidiNote pmn = MidiUtils.getMidiNoteFromNumberReturnNull( previousKey );
				if( pmn == null )
				{
					break;
				}
				previousIsWhite = pmn.isWhiteKey();
			}
			int nextKey = graphicsYCorrectedKey + 1;
			boolean nextIsWhite = true;
			if( nextKey < numMidiNotes )
			{
				MidiNote nmn = MidiUtils.getMidiNoteFromNumberReturnNull( nextKey );
				if( nmn == null )
				{
					break;
				}
				nextIsWhite = nmn.isWhiteKey();
			}
			
			if( isWhite )
			{
				paintPianoKey( g, mn, curKey, isWhite, previousIsWhite, nextIsWhite );
			}
		}

		// Second pass, black keys
		for( int curKey = startKey -1 ; curKey <= endKey + 1 ; curKey++ )
		{
			if( curKey < 0 || curKey > (numMidiNotes - 1) )
			{
				continue;
			}

			int graphicsYCorrectedKey = (numMidiNotes - 1) - curKey;

			MidiNote mn = MidiUtils.getMidiNoteFromNumberReturnNull( graphicsYCorrectedKey );
			if( mn == null )
			{
				break;
			}
			boolean isWhite = mn.isWhiteKey();
			
			int previousKey = graphicsYCorrectedKey - 1;
			boolean previousIsWhite = true;
			if( previousKey >= 0 )
			{
				MidiNote pmn = MidiUtils.getMidiNoteFromNumberReturnNull( previousKey );
				if( pmn == null )
				{
					break;
				}
				previousIsWhite = pmn.isWhiteKey();
			}
			int nextKey = graphicsYCorrectedKey + 1;
			boolean nextIsWhite = true;
			if( nextKey < numMidiNotes )
			{
				MidiNote nmn = MidiUtils.getMidiNoteFromNumberReturnNull( nextKey );
				if( nmn == null )
				{
					break;
				}
				nextIsWhite = nmn.isWhiteKey();
			}
			
			if( !isWhite )
			{
				paintPianoKey( g, mn, curKey, isWhite, previousIsWhite, nextIsWhite );
			}
		}
	}

	private void paintPianoKey( Graphics g,
			MidiNote mn,
			int curKey,
			boolean isWhite,
			boolean previousIsWhite,
			boolean nextIsWhite )
	{
		int startX = 1;
		int endX = size.width - 1;
		
		int startY = (curKey * blockDimensions.height) + 1;
		int endY = ((curKey + 1) * blockDimensions.height) - 1;

		char noteChar = mn.getNoteChar();
		boolean shouldPaintKeyNum = (noteChar == 'C');
		
		if( isWhite )
		{
			g.setColor( Color.WHITE );
			if( !nextIsWhite )
			{
				startY -= blockDimensions.height / 2;
			}
			if( !previousIsWhite )
			{
				endY += blockDimensions.height / 2;
			}
		}
		else
		{
			endX = size.width / 2;
			g.setColor( Color.BLACK );
//			startY -= 2;
//			endY += 2;
		}
		
		g.fillRect( startX,  startY, endX - startX, endY - startY );
		
		if( shouldPaintKeyNum )
		{
			g.setColor( blockColour );
			String noteName = mn.getNoteName();
//			FontMetrics fm = g.getFontMetrics();
			// Do zero, and plus and minus maximum
//			int fontAscent = fm.getAscent();
			g.drawString( noteName, endX - 18, endY - 1 );
		}
		
	}
}
