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

package uk.co.modularaudio.util.audio.gui.mad.rollpainter;

import uk.co.modularaudio.util.exception.DatastoreException;

public class RollPainter<RPBT, RPBTClearer extends RollPainterBufferClearer<RPBT>>
{
//	private static Log log = LogFactory.getLog( RollPainter2.class.getName() );

	private final int numViewingSamples;
	private final int numViewingSamplesNeg;
	private final RollPainterSampleFactory<RPBT, RPBTClearer> sampleFactory;

	// The buffers that will be populated
	public RPBT buffer0;
	public RPBT buffer1;
	public int buffer0XOffset;
	public int buffer1XOffset;

	private final RPBTClearer bufferClearer;

	public RollPainter( final int numViewingSamples, final RollPainterSampleFactory<RPBT, RPBTClearer> sampleFactory )
		throws DatastoreException
	{
		this.numViewingSamples = numViewingSamples;
		this.numViewingSamplesNeg = -numViewingSamples;
		this.sampleFactory = sampleFactory;
		bufferClearer = sampleFactory.getBufferClearer();

		buffer0 = sampleFactory.createBuffer( 0 );
		buffer1 = sampleFactory.createBuffer( 1 );

		buffer0XOffset = 0;
		buffer1XOffset = numViewingSamples;
	}

	public void cleanup() throws DatastoreException
	{
		sampleFactory.freeBuffer( buffer0 );
		sampleFactory.freeBuffer( buffer1 );
		buffer0 = null;
		buffer1 = null;
	}

	public boolean checkAndUpdate()
	{
		final RollPaintUpdate update = sampleFactory.getPaintUpdate();
		final RollPaintUpdateType paintUpdateType = update.getUpdateType();
		switch( paintUpdateType )
		{
			case FULL:
			{
//				log.debug("Doing full update");
				doFullUpdate( update );
				return true;
			}
			case DELTA:
			{
//				log.debug("Doing delta update");
				doDeltaUpdate( update );
				return true;
			}
			case NONE:
			default:
			{
				return false;
			}
		}
	}

	public boolean buffer0Visible()
	{
		return( buffer0XOffset > numViewingSamplesNeg && buffer0XOffset < numViewingSamples );
	}

	public boolean buffer1Visible()
	{
		return( buffer1XOffset > numViewingSamplesNeg && buffer1XOffset < numViewingSamples );
	}

	private void doFullUpdate( final RollPaintUpdate update )
	{
		// Full repaint, reset positions and clear current buffer
		buffer0XOffset = 0;
		bufferClearer.clearBuffer( 0, buffer0 );

		buffer1XOffset = numViewingSamples;
		bufferClearer.clearBuffer( 1, buffer1 );

		sampleFactory.fullFillSamples( update, buffer0 );
	}

	private void doDeltaUpdate( final RollPaintUpdate update )
	{
		int numSamplesAvailable = update.getNumSamplesInUpdate();
		assert( numSamplesAvailable != 0 );
		final RollPaintDirection direction = update.getDirection();

		// Delta repaint - potentially paint into both
		updateBufferPositions( update );

		// Handle forward case, I'll work out reverse later when I have something that uses it :-)
		if( direction == RollPaintDirection.FORWARDS )
		{
			// Moving forwards
			RPBT lb, rb;
			int rightOffset;
			if( buffer0XOffset < buffer1XOffset )
			{
				lb = buffer0;
				rb = buffer1;
				rightOffset = buffer1XOffset;
			}
			else
			{
				lb = buffer1;
				rb = buffer0;
				rightOffset = buffer0XOffset;
			}
			final int numVisibleInRight = (numViewingSamples - rightOffset);
			final int numInRight = (numSamplesAvailable > numVisibleInRight ? numVisibleInRight : numSamplesAvailable );
			final int numInLeft = (numSamplesAvailable - numInRight);

			final int rightDisplayOffset = numViewingSamples - numInRight;
			final int leftDisplayOffset = rightDisplayOffset - numInLeft;

			if( numInLeft > 0 )
			{
				final int leftBegin = numViewingSamples - numInLeft;
//				log.debug("Left painting " + numInLeft + " at offset " + leftBegin );
				sampleFactory.deltaFillSamples( update, leftDisplayOffset, lb, leftBegin, numInLeft, rb );
			}
			if( numInRight > 0 )
			{
				final int rightBegin = numVisibleInRight - numInRight;
//				log.debug("Right painting " + numInRight + " at offset " + rightBegin );
				sampleFactory.deltaFillSamples( update, rightDisplayOffset, rb, rightBegin, numInRight, lb );
			}
		}
		else
		{
			numSamplesAvailable = -numSamplesAvailable;
			// Moving backwards
			RPBT lb, rb;
			int rightOffset;
			if( buffer0XOffset < buffer1XOffset )
			{
				lb = buffer0;
				rb = buffer1;
				rightOffset = buffer1XOffset;
			}
			else
			{
				lb = buffer1;
				rb = buffer0;
				rightOffset = buffer0XOffset;
			}
			final int numVisibleInLeft = rightOffset;
			final int numInLeft = (numSamplesAvailable > numVisibleInLeft ? numVisibleInLeft : numSamplesAvailable );
			final int numInRight = numSamplesAvailable - numInLeft;

			final int leftDisplayOffset = 0;
			final int rightDisplayOffset = leftDisplayOffset + numInLeft;

			// Need to paint the ones in the right first
			if( numInRight > 0 )
			{
				final int rightBegin = 0;
//				log.debug("Reverse right painting " + numInRight + " at offset " + rightBegin );
				sampleFactory.deltaFillSamples( update, rightDisplayOffset, rb, rightBegin, numInRight, lb );
			}
			if( numInLeft > 0 )
			{
				final int leftBegin = numViewingSamples - numVisibleInLeft;
//				log.debug("Reverse left painting " + numInLeft + " at offset " + leftBegin );
				sampleFactory.deltaFillSamples( update, leftDisplayOffset, lb, leftBegin, numInLeft, rb );
			}
		}
	}

	private void updateBufferPositions( final RollPaintUpdate update )
	{
		final RollPaintDirection direction = update.getDirection();
		final int numSamplesToUpdate = update.getNumSamplesInUpdate();

		buffer0XOffset -= numSamplesToUpdate;
		buffer1XOffset -= numSamplesToUpdate;

		if( direction == RollPaintDirection.FORWARDS )
		{
			if( buffer0XOffset <= numViewingSamplesNeg )
			{
				buffer0XOffset += numViewingSamples * 2;
				bufferClearer.clearBuffer( 0, buffer0 );
			}
			if( buffer1XOffset <= numViewingSamplesNeg )
			{
				buffer1XOffset += numViewingSamples * 2;
				bufferClearer.clearBuffer( 1, buffer1 );
			}
		}
		else
		{
			if( buffer0XOffset >= numViewingSamples )
			{
				buffer0XOffset -= numViewingSamples * 2;
				bufferClearer.clearBuffer( 0, buffer0 );
			}
			else if( buffer1XOffset >= numViewingSamples )
			{
				buffer1XOffset -= numViewingSamples * 2;
				bufferClearer.clearBuffer( 1, buffer1 );
			}
		}
//		log.debug("Updated by " + numSamplesToUpdate + " positions to " + buffer0XOffset + " and " + buffer1XOffset );
	}

}
