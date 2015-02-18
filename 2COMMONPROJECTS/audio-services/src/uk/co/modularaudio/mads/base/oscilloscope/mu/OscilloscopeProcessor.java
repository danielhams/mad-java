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

package uk.co.modularaudio.mads.base.oscilloscope.mu;

import java.util.ArrayList;

import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeWriteableScopeData.FloatType;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;

public class OscilloscopeProcessor
{
//	private static Log log = LogFactory.getLog( OscilloscopeProcessor.class.getName() );

	private final OscilloscopeMadInstance instance;

	protected ArrayList<OscilloscopeWriteableScopeData> bufferedScopeData;

	private OscilloscopeWriteableScopeData scopeData;

	private boolean currentlyCapturing;
	private float previousTriggerVal;

	public OscilloscopeProcessor( final OscilloscopeMadInstance instance,
			final ArrayList<OscilloscopeWriteableScopeData> bufferedScopeData )
	{
		this.instance = instance;
		this.bufferedScopeData = bufferedScopeData;
	}

	public void processPeriod(
			final OscilloscopeCaptureTriggerEnum triggerEnum,
			final int frameOffset,
			final int numFrames,
			final ThreadSpecificTemporaryEventStorage tses,
			final long timingInfo,
			final boolean triggerConnected, final float[] triggerFloats,
			final boolean audio0Connected, final float[] audio0Floats,
			final boolean cv0Connected, final float[] cv0Floats,
			final boolean audio1Connected, final float[] audio1Floats,
			final boolean cv1Connected, final float[] cv1Floats )
	{
		int triggerIndex = -1;
		if( currentlyCapturing && scopeData.currentWriteIndex < scopeData.desiredDataLength )
		{
			triggerIndex = 0;
		}
		else
		{
			// Not currently capturing
			if( triggerConnected )
			{
				switch( triggerEnum )
				{
					case NONE:
					{
						// No trigger defined, capture everything we can
						triggerIndex = 0;
						break;
					}
					case ON_RISE:
					{
						// Looking for < 0.0 to > 0.0
						float thisTrigVal = previousTriggerVal;
						for( int s = 0 ; s < numFrames ; s++ )
						{
							final float testVal = triggerFloats[ frameOffset + s ];
							if( thisTrigVal <= 0.0 && testVal > 0.0 )
							{
								triggerIndex = s;
								break;
							}
							else
							{
								thisTrigVal = testVal;
							}
						}
						break;
					}
					case ON_FALL:
					{
						// Looking for > 0.0 to < 0.0
						float thisTrigVal = previousTriggerVal;
						for( int s = 0 ; s < numFrames ; s++ )
						{
							final float testVal = triggerFloats[ frameOffset + s ];
							if( thisTrigVal >= 0.0 && testVal < 0.0 )
							{
								triggerIndex = s;
								break;
							}
							else
							{
								thisTrigVal = testVal;
							}
						}
						break;
					}
				}
			}
			else
			{
				// Trigger not connected, assume no trigger
				triggerIndex = 0;
			}
		}

		if( triggerIndex != -1 )
		{
			// Have hit a trigger (or are still processing a previously triggered capture)

			// If we haven't yet obtained a scope data, go and get one.
			if( bufferedScopeData.size() > 0 )
			{
				if( scopeData == null )
				{
					scopeData = bufferedScopeData.remove( 0 );
					scopeData.currentWriteIndex = 0;
//					scopeData.desiredDataLength = desiredCaptureSamples;
					currentlyCapturing = true;
				}

				scopeData.float0Written = true;
				if( audio0Connected )
				{
					scopeData.floatBuffer0Type = FloatType.AUDIO;
				}
				else if( cv0Connected )
				{
					scopeData.floatBuffer0Type = FloatType.CV;
				}
				else
				{
					scopeData.float0Written = false;
				}

				scopeData.float1Written = true;
				if( audio1Connected )
				{
					scopeData.floatBuffer1Type = FloatType.AUDIO;
				}
				else if( cv1Connected )
				{
					scopeData.floatBuffer1Type = FloatType.CV;
				}
				else
				{
					scopeData.float1Written = false;
				}
			}

			if( scopeData != null && currentlyCapturing )
			{
				// Pipe what we can from the buffers into the scope data
				final int numAvailable = numFrames - triggerIndex;
				final int scopeSpace = scopeData.desiredDataLength - scopeData.currentWriteIndex;
				final int numToWrite = (numAvailable < scopeSpace ? numAvailable : scopeSpace );
				for( int s = 0 ; s < numToWrite ; s++ )
				{
					final int inIndex = frameOffset + triggerIndex + s;
					final int outIndex = scopeData.currentWriteIndex + s;
					if( audio0Connected )
					{
						scopeData.floatBuffer0[ outIndex ] = audio0Floats[ inIndex ];
					}
					else if( cv0Connected )
					{
						scopeData.floatBuffer0[ outIndex ] = cv0Floats[ inIndex ];
					}

					if( audio1Connected )
					{
						scopeData.floatBuffer1[ outIndex ] = audio1Floats[ inIndex ];
					}
					else if( cv1Connected )
					{
						scopeData.floatBuffer1[ outIndex ] = cv1Floats[ inIndex ];
					}
				}
				scopeData.currentWriteIndex += numToWrite;

				if( scopeData.currentWriteIndex >= scopeData.desiredDataLength )
				{
//					log.debug("Emitted scope data to ui");
					instance.emitScopeDataToUi( tses, timingInfo, scopeData );
					scopeData = null;
					currentlyCapturing = false;
				}
			}
		}

		if( triggerConnected )
		{
			previousTriggerVal = triggerFloats[ frameOffset + numFrames - 1 ];
		}
	}

}
