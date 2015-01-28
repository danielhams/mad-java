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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.gui.patternsequencer.model.PatternSequenceModel;
import uk.co.modularaudio.util.audio.gui.patternsequencer.model.PatternSequenceModelListener;
import uk.co.modularaudio.util.audio.gui.patternsequencer.model.PatternSequenceStep;
import uk.co.modularaudio.util.audio.midi.MidiNote;

public class PatternSequenceAmpGrid extends JPanel implements PatternSequenceModelListener
{
	private static Log log = LogFactory.getLog( PatternSequenceAmpGrid.class.getName() );

	private static final long serialVersionUID = -65213973113401428L;

	private final PatternSequenceModel dataModel;
	private final int numCols;
	private final Dimension cellDimensions;
	private final Dimension size;
	private final Color backgroundColour;
	private final Color foregroundColour;
	private final Color blockColour;

	private final Rectangle curClipBounds = new Rectangle();
	private final Point minPointToPaint = new Point(Integer.MIN_VALUE, Integer.MAX_VALUE);
	private final Point maxPointToPaint = new Point(Integer.MIN_VALUE, Integer.MAX_VALUE);

	private final PatternSequenceAmpGridMouseListener mouseListener;

	public final static int AMP_GRID_HEIGHT = 50;
	public final static int AMP_BOX_HEIGHT = 40;
	public final static int AMP_BOX_DELTA = AMP_GRID_HEIGHT - AMP_BOX_HEIGHT;

	public PatternSequenceAmpGrid( final PatternSequenceModel dataModel,
			final Dimension blockDimensions,
			final Color backgroundColour,
			final Color gridColour,
			final Color blockColour )
	{
		this.dataModel = dataModel;
		this.cellDimensions = blockDimensions;
		if( log.isDebugEnabled() )
		{
			log.debug("Table cell dimensions are " + blockDimensions.toString());
		}
		this.numCols = dataModel.getNumSteps();
		this.backgroundColour = backgroundColour;
		this.foregroundColour = gridColour;
		this.blockColour = blockColour;

		size = new Dimension( (numCols * blockDimensions.width) + 1, AMP_GRID_HEIGHT );
		this.setMinimumSize( size );
		this.setSize( size );
		this.setPreferredSize( size );
		this.setMaximumSize( size );

//		this.setBackground( backgroundColour );
		this.setBackground( Color.BLUE );

		// Make sure results are cached
		this.setOpaque( true );
		this.setDoubleBuffered( true );

		dataModel.addListener( this );

//		fullRefreshFromModel();

		mouseListener = new PatternSequenceAmpGridMouseListener( this, dataModel );
		this.addMouseListener( mouseListener );
		this.addMouseMotionListener( mouseListener );
		log.debug("Done with pattern sequence amp grid initialisation");
	}

	@Override
	public void paint( final Graphics g )
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
		final int startCol = (minPointToPaint.x < 0 ? 0 : (minPointToPaint.x / cellDimensions.width) );
		final int endCol = (maxPointToPaint.x >= size.width ? numCols : (maxPointToPaint.x / cellDimensions.width ) );

//		log.debug( "So for minpoint(" + minPointToPaint.toString() + ") and maxpoint(" + maxPointToPaint.toString() + ")");
//		log.debug( "We get startCol(" + startCol + ") and endCol(" + endCol +")");

		paintGrid( g, startCol, endCol );
		paintCells( g, startCol, endCol );
	}

	private void paintGrid( final Graphics g,
			final int startCol,
			final int endCol )
	{
		g.setColor( foregroundColour );
		for( int i = startCol ; i <= endCol ; i++ )
		{
			final int lineStartY = AMP_BOX_DELTA;
			final int lineX = i * cellDimensions.width;
			final int lineEndY = size.height - 1;
			g.drawLine(lineX, lineStartY, lineX, lineEndY );
		}
		g.drawLine( minPointToPaint.x, AMP_BOX_DELTA, maxPointToPaint.x, AMP_BOX_DELTA );
		g.drawLine( minPointToPaint.x, size.height - 1, maxPointToPaint.x, size.height - 1 );
	}

	private void paintCells( final Graphics g,
			final int startCol,
			final int endCol )
	{
		g.setColor( blockColour );
		for( int i = startCol ; i <= endCol ; i++ )
		{
			if( i < 0 || i > numCols - 1 )
			{
				continue;
			}
			final PatternSequenceStep psn = dataModel.getNoteAtStep( i );
			final MidiNote mn = psn.note;
			if( mn != null )
			{
				final float amp = psn.amp;
				final boolean isContinuation = psn.isContinuation;
				final int scaledAmp  = (int)(amp * AMP_BOX_HEIGHT) - 6;
				final int startX = (i * cellDimensions.width) + 2;
				final int endX = ( isContinuation ? startX + cellDimensions.width - 4 : startX + (cellDimensions.width / 2 ) );
				final int endY = size.height - 3;
				final int startY = endY - scaledAmp;

				g.fillRect( startX, startY, endX - startX, endY - startY );
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

	@Override
	public void receiveStepAmpChange( final int firstStep, final int lastStep )
	{
		this.repaint();
	}

	@Override
	public void receiveStepnoteChange( final int firstStep, final int lastStep )
	{
	}

	@Override
	public void receiveStepNoteAndAmpChange( final int firstStep, final int lastStep )
	{
		this.repaint();
	}
}
