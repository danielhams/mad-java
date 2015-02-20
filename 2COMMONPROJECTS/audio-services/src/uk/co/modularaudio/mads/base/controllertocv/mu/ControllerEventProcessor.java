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

package uk.co.modularaudio.mads.base.controllertocv.mu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.mad.MadChannelNoteEvent;
import uk.co.modularaudio.util.math.NormalisedValuesMapper;

public class ControllerEventProcessor
{
	private static Log log = LogFactory.getLog( ControllerEventProcessor.class.getName() );

	private final ControllerEvent[] events;
	private int numEvents;

	private float previousCvValue = 0.0f;

	private float curValueRatio = 1.0f;
	private float newValueRatio = 0.0f;

	private float curDesiredValue = 0.0f;

	private ControllerEventMapping mapping = ControllerEventMapping.LINEAR;

	public ControllerEventProcessor( final int notePeriodLength )
	{
		events = new ControllerEvent[ notePeriodLength ];
		for( int e = 0 ; e < notePeriodLength ; e++ )
		{
			events[ e ] = new ControllerEvent();
		}
		numEvents = 0;
	}

	public void setNewRatios( final float newCurValueRatio, final float newNewValueRatio )
	{
//		this.curValueRatio = newCurValueRatio;
//		this.newValueRatio = newNewValueRatio;
		this.curValueRatio = 0.0f;
		this.newValueRatio = 1.0f;
	}

	public void processEvent( final MadChannelNoteEvent ne )
	{
		final int sampleIndex = ne.getEventSampleIndex();
		events[ numEvents ].sampleIndex = sampleIndex;
		final float valToMap = (ne.getParamTwo() / 127.0f);
		events[ numEvents ].desiredValue = mapValue( valToMap );
		numEvents++;
//		log.debug("Processed event: " + ne.toString() );
	}

	private float mapValue( final float valToMap )
	{
		switch( mapping )
		{
			case LINEAR:
			{
				return valToMap;
			}
			case LOG:
			{
				return NormalisedValuesMapper.logMapF( valToMap );
			}
			case LOG_FREQUENCY:
			{
				return NormalisedValuesMapper.logMinMaxMapF( valToMap, 0.0f, 22050.0f );
			}
			case EXP:
			{
				return NormalisedValuesMapper.expMapF( valToMap );
			}
			case EXP_FREQUENCY:
			{
				return NormalisedValuesMapper.expMinMaxMapF( valToMap, 0.0f, 22050.0f );
			}
			case CIRC_Q1:
			{
				return NormalisedValuesMapper.circleQuadOneF( valToMap );
			}
			case CIRC_Q2:
			{
				return NormalisedValuesMapper.circleQuadTwoF( valToMap );
			}
			case CIRC_Q3:
			{
				return NormalisedValuesMapper.circleQuadThreeF( valToMap );
			}
			case CIRC_Q4:
			{
				return NormalisedValuesMapper.circleQuadFourF( valToMap );
			}
			default:
			{
				if( log.isErrorEnabled() )
				{
					log.error("Unknown mapping: " + mapping.toString() );
				}
			}
		}
		return valToMap;
	}

	public void emptyPeriod( final int numFrames )
	{
	}

	public void outputCv( final int numFrames, final float[] outCvFloats )
	{
		boolean loopDone = false;
		int previousSampleIndex = 0;
		for( int e = 0 ; !loopDone && e < numEvents ; e++ )
		{
			final ControllerEvent event = events[ e ];
			final int sampleIndex = event.sampleIndex;
			if( sampleIndex == -1 )
			{
				loopDone = true;
			}
			curDesiredValue = event.desiredValue;

			for( int s = previousSampleIndex ; s < sampleIndex ; s++ )
			{
				previousCvValue = (previousCvValue * curValueRatio) + (curDesiredValue * newValueRatio);
				outCvFloats[ s ] = previousCvValue;
			}
			previousSampleIndex = sampleIndex;
		}

		for( int s = previousSampleIndex ; s < numFrames ; s++ )
		{
			if( previousCvValue != curDesiredValue && (previousCvValue - curDesiredValue) < Float.MIN_VALUE * 2000 )
			{
				previousCvValue = curDesiredValue;
			}
			else
			{
				previousCvValue = (previousCvValue * curValueRatio) + (curDesiredValue * newValueRatio);
			}
			outCvFloats[ s ] = previousCvValue;
		}
	}

	public void done()
	{
		numEvents = 0;
	}

	public void setDesiredMapping( final ControllerEventMapping desiredMapping )
	{
		this.mapping  = desiredMapping;
	}
}
