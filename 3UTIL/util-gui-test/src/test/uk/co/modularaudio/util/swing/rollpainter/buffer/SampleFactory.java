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

package test.uk.co.modularaudio.util.swing.rollpainter.buffer;

import java.awt.Color;
import java.awt.Component;
import java.awt.image.BufferedImage;

import test.uk.co.modularaudio.util.swing.rollpainter.RPConstants;
import uk.co.modularaudio.util.audio.gui.mad.rollpainter.RollPaintDefaultUpdateStructure;
import uk.co.modularaudio.util.audio.gui.mad.rollpainter.RollPaintDirection;
import uk.co.modularaudio.util.audio.gui.mad.rollpainter.RollPaintUpdate;
import uk.co.modularaudio.util.audio.gui.mad.rollpainter.RollPaintUpdateType;
import uk.co.modularaudio.util.audio.gui.mad.rollpainter.RollPainterSampleFactory;
import uk.co.modularaudio.util.exception.DatastoreException;

public class SampleFactory implements RollPainterSampleFactory<Buffer, BufferClearer>
{
//	private static Log log = LogFactory.getLog( SampleFactory.class.getName() );
	
//	private final Component sourceComponent;
	
	private final BufferClearer bufferClearer = new BufferClearer();
	
	private final RollPaintDefaultUpdateStructure updateType = new RollPaintDefaultUpdateStructure();

	private int curSpeed = 0;
	
	public SampleFactory( Component sourceComponent )
	{
//		this.sourceComponent = sourceComponent;
	}

	private int getNumSamplesAvailable()
	{
		return curSpeed;
	}

	public RollPaintUpdateType getPaintUpdateType()
	{
		return RollPaintUpdateType.DELTA;
	}

	@Override
	public BufferClearer getBufferClearer()
	{
		return bufferClearer;
	}

	@Override
	public Buffer createBuffer( int bufNum ) throws DatastoreException
	{
		Buffer retVal = new Buffer();
		retVal.bufNum = bufNum;
		retVal.image = new BufferedImage( RPConstants.RP_CANVAS_WIDTH, RPConstants.RP_CANVAS_HEIGHT, BufferedImage.TYPE_INT_RGB );
		retVal.graphics = retVal.image.createGraphics();
		return retVal;
	}

	@Override
	public void freeBuffer( Buffer bufferToFree ) throws DatastoreException
	{
	}

	public void setSpeedValue( int newSpeed )
	{
		curSpeed = newSpeed;
	}

	@Override
	public RollPaintUpdate getPaintUpdate()
	{
		int numSamplesAvailable = getNumSamplesAvailable();

		updateType.setUpdateValues( RollPaintUpdateType.DELTA,
				(numSamplesAvailable < 0 ? RollPaintDirection.BACKWARDS : RollPaintDirection.FORWARDS ),
				numSamplesAvailable );
		return updateType;
	}

	@Override
	public void fullFillSamples( RollPaintUpdate update, Buffer buffer )
	{
	}

	@Override
	public void deltaFillSamples( RollPaintUpdate update,
			int displayOffset,
			Buffer buffer,
			int bufferSampleOffset,
			int numSamples,
			Buffer otherBuffer )
	{
//		log.debug("deltaFillSamples(" + numSamples + ") at sampleOffset(" + bufferSampleOffset + ") with displayOffset(" + displayOffset + ")");
		bufferClearer.clearBuffer( buffer.bufNum, buffer );
		bufferClearer.clearBuffer( otherBuffer.bufNum, otherBuffer );
		if( update.getDirection() == RollPaintDirection.FORWARDS )
		{
			buffer.graphics.setColor( Color.yellow );
		}
		else
		{
			buffer.graphics.setColor( Color.magenta );
		}

		buffer.graphics.fillRect( bufferSampleOffset, 10, numSamples, RPConstants.RP_CANVAS_HEIGHT - 20 );
	}

}
