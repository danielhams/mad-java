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
import uk.co.modularaudio.util.audio.midi.MidiUtils;

public class PatternSequenceNoteGridMouseListener implements MouseListener, MouseMotionListener
{
	private static Log log = LogFactory.getLog( PatternSequenceNoteGridMouseListener.class.getName() );

	private final PatternSequenceNoteGrid table;
	private final PatternSequenceModel dataModel;

	private final Dimension tableCellDimensions;
	private final Dimension tableSize;
	private final int numMidiNotes;

	private boolean lastNoteChangeWasSet = true;

	private final Point cellPoint = new Point();
	private final Point previouslySetCellPoint = new Point(-1,-1);

	public PatternSequenceNoteGridMouseListener( final PatternSequenceNoteGrid patternSequencerTable, final PatternSequenceModel dataModel )
	{
		this.table = patternSequencerTable;
		this.dataModel = dataModel;
		this.tableCellDimensions = table.getCellDimensions();
		this.tableSize = table.getTableSize();
		numMidiNotes = MidiUtils.getNumMidiNotes();
	}

	@Override
	public void mouseDragged( final MouseEvent e )
	{
//		log.debug("Got a drag event " + e.toString() );
		final Point dragPoint = e.getPoint();
		calculateCellIndexesFromPoint( dragPoint, cellPoint );
		if( !cellPoint.equals( previouslySetCellPoint ) )
		{
//			log.debug("Was for a new cell, will call set note for point");
			setNoteForPoint( dragPoint, true );
			previouslySetCellPoint.x = cellPoint.x;
			previouslySetCellPoint.y = cellPoint.y;
		}
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
		calculateCellIndexesFromPoint( clickPoint, cellPoint );
		if( !cellPoint.equals( previouslySetCellPoint ) )
		{
			lastNoteChangeWasSet = setNoteForPoint( clickPoint, false );
			previouslySetCellPoint.x = cellPoint.x;
			previouslySetCellPoint.y = cellPoint.y;
		}
	}

	private void calculateCellIndexesFromPoint( final Point clickPoint, final Point outputPoint )
	{
		outputPoint.x = (clickPoint.x / tableCellDimensions.width);
		outputPoint.y = (clickPoint.y / tableCellDimensions.height);
	}
	private boolean setNoteForPoint( final Point clickPoint, final boolean isDrag )
	{
		boolean wasASet = false;
		if( clickPoint.x >= 0 && clickPoint.x <= (tableSize.width - 1) &&
				clickPoint.y >= 0 && clickPoint.y <= (tableSize.height - 1) )
		{
//			log.debug("Within bounds. Will calculate which cell");
			final Point cellPoint = new Point();
			calculateCellIndexesFromPoint( clickPoint, cellPoint );
			final int cellCol = cellPoint.x;
			final int cellRow = cellPoint.y;
//			log.debug("Is (" + cellCol + ", " + cellRow + ")");
			if( cellCol < 0 || cellRow < 0 || cellCol > (dataModel.getNumSteps() - 1) || cellRow > (numMidiNotes - 1) )
			{
				return false;
			}

			try
			{
				final MidiNote relatedNote = MidiUtils.getMidiNoteFromNumberReturnNull( (numMidiNotes - 1) - cellRow );
				if( relatedNote == null )
				{
					return false;
				}
//				log.debug("Toggling note " + relatedNote.toString() );

				// If it's already set, unset it
				final PatternSequenceStep psn = dataModel.getNoteAtStep( cellCol );
				final MidiNote noteFound = psn.note;

				final boolean continuation = ( psn.note == null ? dataModel.getContinuationState() : psn.isContinuation );
				final float amp = ( psn.note == null ? dataModel.getDefaultAmplitude() : psn.amp );

				if( (!isDrag && noteFound == relatedNote ) ||
						(isDrag && lastNoteChangeWasSet == false && noteFound == relatedNote ) )
				{
					dataModel.unsetNoteAtStep( cellCol );
					wasASet = false;
				}
				else
				{
					if( !isDrag || (isDrag && lastNoteChangeWasSet) )
					{
						if( psn.note != null )
						{
							// Re-use the existing amp/continuation
							dataModel.setNoteAtStep( cellCol, relatedNote );
						}
						else
						{
							dataModel.setNoteAtStepWithAmp( cellCol, relatedNote, continuation, amp );
						}
						wasASet = true;
					}
				}
			}
			catch (final Exception e1)
			{
				final String msg = "Exception caught adding contents to table: " + e1.toString();
				log.error( msg, e1 );
			}
		}
		return wasASet;
	}

	@Override
	public void mouseReleased( final MouseEvent e )
	{
//		log.debug("Received release event.");
		previouslySetCellPoint.x = -1;
		previouslySetCellPoint.y = -1;
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
