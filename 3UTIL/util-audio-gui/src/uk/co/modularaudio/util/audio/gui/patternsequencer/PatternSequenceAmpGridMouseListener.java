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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.gui.patternsequencer.model.PatternSequenceModel;
import uk.co.modularaudio.util.audio.gui.patternsequencer.model.PatternSequenceStep;
import uk.co.modularaudio.util.audio.midi.MidiNote;

public class PatternSequenceAmpGridMouseListener implements MouseListener, MouseMotionListener
{
	private static Log log = LogFactory.getLog( PatternSequenceAmpGridMouseListener.class.getName() );

	private final PatternSequenceAmpGrid ampGrid;
	private final PatternSequenceModel dataModel;

	private final Dimension tableCellDimensions;
	private final Dimension tableSize;

	public PatternSequenceAmpGridMouseListener( final PatternSequenceAmpGrid ampGrid, final PatternSequenceModel dataModel )
	{
		this.ampGrid = ampGrid;
		this.dataModel = dataModel;
		this.tableCellDimensions = ampGrid.getCellDimensions();
		this.tableSize = ampGrid.getTableSize();
	}

	@Override
	public void mouseDragged( final MouseEvent e )
	{
		processMouseClickAtPosition( e.getPoint() );
	}

	@Override
	public void mouseMoved( final MouseEvent e )
	{
	}

	@Override
	public void mouseClicked( final MouseEvent e )
	{
	}

	@Override
	public void mousePressed( final MouseEvent e )
	{
//		log.debug("Mouse pressed");
		final Point clickPoint = e.getPoint();
		processMouseClickAtPosition( clickPoint );
	}

	private void processMouseClickAtPosition( final Point clickPoint )
	{
		if( clickPoint.x >= 0 && clickPoint.x <= (tableSize.width ) &&
				clickPoint.y >= 0 && clickPoint.y <= (tableSize.height ) )
		{
			final int cellCol = (clickPoint.x / tableCellDimensions.width);
			if( cellCol < 0 || cellCol > (dataModel.getNumSteps() - 1 ) )
			{
				return;
			}
//			log.debug("Within bounds. Will calculate new amp from y " + clickPoint.y );
			final int clickPointGridOffset = (ampGrid.getSize().height + 2) - clickPoint.y;
//			log.debug("Clickpointgridoffset " + clickPointGridOffset );
			final float realY = clickPointGridOffset;
//			log.debug("Calculate real Y of " + realY );
			float newAmp = (realY / PatternSequenceAmpGrid.AMP_BOX_HEIGHT );
			if( newAmp < 0.2f ) newAmp = 0.2f;
			if( newAmp > 1.0f ) newAmp = 1.0f;
//			log.debug("New amp will be " + newAmp );

			try
			{
				// Only change the amp if it's already set
				final PatternSequenceStep noteFound = dataModel.getNoteAtStep( cellCol );
				final MidiNote mn = noteFound.note;
				final boolean isContinuation = dataModel.getContinuationState();
				if( noteFound != null && mn != null )
				{
//					log.debug("Setting note at step " + cellCol + " " + mn + " " + isContinuation + " " + newAmp );
					dataModel.setAmpAndContinuationAtStep( cellCol, isContinuation, newAmp );
				}
			}
			catch (final Exception e1)
			{
				final String msg = "Exception caught adding contents to table: " + e1.toString();
				log.error( msg, e1 );
			}
		}
	}

	@Override
	public void mouseReleased( final MouseEvent e )
	{
	}

	@Override
	public void mouseEntered( final MouseEvent e )
	{
	}

	@Override
	public void mouseExited( final MouseEvent e )
	{
	}

}
