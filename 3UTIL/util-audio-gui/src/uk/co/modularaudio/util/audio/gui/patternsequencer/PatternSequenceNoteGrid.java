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
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.gui.patternsequencer.model.PatternSequenceModel;
import uk.co.modularaudio.util.audio.gui.patternsequencer.model.PatternSequenceModelListener;
import uk.co.modularaudio.util.audio.gui.patternsequencer.model.PatternSequenceStep;
import uk.co.modularaudio.util.audio.midi.MidiNote;
import uk.co.modularaudio.util.audio.midi.MidiUtils;

public class PatternSequenceNoteGrid extends JPanel implements PatternSequenceModelListener
{
	private static Log log = LogFactory.getLog( PatternSequenceNoteGrid.class.getName() );
	private static final long serialVersionUID = 3554792918850929516L;
	
	private PatternSequenceModel dataModel = null;
	private int numMidiNotes = -1;
	private int numCols = -1;
	private Dimension cellDimensions = null;
	private Dimension size = null;
	private Color backgroundColour = null;
	private Color foregroundColour = null;
	private Color blockColour = null;
	
	private Rectangle curClipBounds = new Rectangle();
	private Point minPointToPaint = new Point(Integer.MIN_VALUE, Integer.MAX_VALUE);
	private Point maxPointToPaint = new Point(Integer.MIN_VALUE, Integer.MAX_VALUE);
	
	private PatternSequenceNoteGridMouseListener mouseListener = null;
	
	private Point lastPoint = new Point( 0,0 );

	public PatternSequenceNoteGrid( PatternSequenceModel dataModel,
			Dimension blockDimensions,
			Color backgroundColour,
			Color gridColour,
			Color blockColour )
	{
		this.dataModel = dataModel;
		this.cellDimensions = blockDimensions;
		log.debug("Table cell dimensions are " + blockDimensions.toString());
		this.numCols = dataModel.getNumSteps();
		this.numMidiNotes = MidiUtils.getNumMidiNotes();
		this.backgroundColour = backgroundColour;
		this.foregroundColour = gridColour;
		this.blockColour = blockColour;
		
		size = new Dimension( (numCols * blockDimensions.width) + 1, (numMidiNotes * blockDimensions.height) + 1 );
		this.setMinimumSize( size );
		this.setSize( size );
		this.setPreferredSize( size );
		this.setMaximumSize( size );
		
		this.setBackground( backgroundColour );
		
		// Make sure results are cached
		this.setOpaque( true );
		this.setDoubleBuffered( true );
		
		dataModel.addListener( this );
		
		fullRefreshFromModel();
		
		mouseListener = new PatternSequenceNoteGridMouseListener( this, dataModel );
		this.addMouseListener( mouseListener );
		this.addMouseMotionListener( mouseListener );
		
		SwingUtilities.invokeLater( new Runnable()
		{
			
			@Override
			public void run()
			{
				scrollRectToVisible( new Rectangle( 0, (int)size.getHeight() - 1, (int)size.getWidth(), cellDimensions.height ) );
			}
		} );
	}
	
	private void fullRefreshFromModel()
	{
		this.invalidate();
//		this.repaint();
	}

	@Override
	public void paint( Graphics g )
	{
//		log.debug("Paint called");
		getBounds( curClipBounds );
//		log.debug( "Curclipbounds after get bounds are " + curClipBounds.toString() );
		g.getClipBounds( curClipBounds );
		g.setColor( backgroundColour );
//		log.debug( "Curclipbounds after get clip bounds are " + curClipBounds.toString() );
		g.fillRect( curClipBounds.x, curClipBounds.y, curClipBounds.width, curClipBounds.height );
		// Work out our boundings values +/- the individual cell width so we paint the edge cases too
		minPointToPaint.x = curClipBounds.x - cellDimensions.width;
		minPointToPaint.y = curClipBounds.y - cellDimensions.height;
		maxPointToPaint.x = curClipBounds.x + curClipBounds.width + (cellDimensions.width);
		maxPointToPaint.y = curClipBounds.y + curClipBounds.height + (cellDimensions.height);
		int startCol = (minPointToPaint.x < 0 ? 0 : (minPointToPaint.x / cellDimensions.width) );
		int endCol = (maxPointToPaint.x >= size.width ? numCols : (maxPointToPaint.x / cellDimensions.width ) );
		int startRow = (minPointToPaint.y < 0 ? 0 : (minPointToPaint.y / cellDimensions.height) );
		int endRow = (maxPointToPaint.y >= size.height ? numMidiNotes : (maxPointToPaint.y / cellDimensions.height ) );
//		log.debug( "So for minpoint(" + minPointToPaint.toString() + ") and maxpoint(" + maxPointToPaint.toString() + ")");
//		log.debug( "We get startCol(" + startCol + ") and endCol(" + endCol +")");
//		log.debug( "We get startRow(" + startRow + ") and endRow(" + endRow +")");

		paintGrid( g, startCol, endCol, startRow, endRow );
		paintCells( g, startCol, endCol, startRow, endRow );
		
//		g.setColor( Color.RED );
//		g.drawLine( lastPoint.x-2, lastPoint.y, lastPoint.x+2, lastPoint.y );
//		g.drawLine( lastPoint.x, lastPoint.y-2, lastPoint.x, lastPoint.y+2 );
	}
	
	private void paintGrid( Graphics g, int startCol, int endCol, int startRow, int endRow )
	{
		g.setColor( foregroundColour );
		for( int i = startCol ; i <= endCol ; i++ )
		{
			int lineStartY = startRow * cellDimensions.height;
			int lineX = i * cellDimensions.width;
			int lineEndY = endRow * cellDimensions.height;
			g.drawLine(lineX, lineStartY, lineX, lineEndY );
		}
		for( int j = startRow ; j <= endRow ; j++ )
		{
			int lineStartX = startCol * cellDimensions.width;
			int lineY = j * cellDimensions.height;
			int lineEndX = endCol * cellDimensions.width;
			g.drawLine(lineStartX, lineY, lineEndX, lineY );
		}
	}
	
	private void paintCells( Graphics g, int startCol, int endCol, int startRow, int endRow )
	{
		g.setColor( blockColour );
		// Now fill in any spots that have something currently set
		for( int i = startCol ; i < endCol ; i++ )
		{
			PatternSequenceStep psn = dataModel.getNoteAtStep( i );
			MidiNote mn = psn.note;
			if( mn != null )
			{
				int row = (numMidiNotes - 1 ) - mn.getMidiNumber();
				int startx = (i * cellDimensions.width) + 2;
				int starty = (row * cellDimensions.height) + 2;
				g.fillRect( startx, starty, cellDimensions.width - 3, cellDimensions.height - 3 );
			}
		}
	}

	public Dimension getCellDimensions()
	{
		return cellDimensions;
	}
	
	public Dimension getTableSize()
	{
		return size;
	}

	public void setLastClickedPoint( Point clickPoint )
	{
		Point previousPoint = lastPoint;
		lastPoint = clickPoint;
		this.repaint( previousPoint.x - 3, previousPoint.y - 3, 6, 6 );
		this.repaint( clickPoint.x -3, clickPoint.y - 3, 6, 6 );
	}

	@Override
	public void receiveStepNoteAndAmpChange( int firstStep, int lastStep )
	{
//		log.debug("Received table changed");
		repaint();
	}

	@Override
	public void receiveStepnoteChange( int firstStep, int lastStep )
	{
		repaint();
	}

	@Override
	public void receiveStepAmpChange( int firstStep, int lastStep )
	{
	}

}
