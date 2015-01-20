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
	
	private OscilloscopeMadInstance instance = null;
	
	private OscilloscopeCaptureTriggerEnum triggerEnum = OscilloscopeCaptureTriggerEnum.NONE;
	
//	private int desiredCaptureSamples = 1;
	private int periodLength = -1;
	
	protected ArrayList<OscilloscopeWriteableScopeData> bufferedScopeData = null;
	
	private OscilloscopeWriteableScopeData scopeData = null;
	
	private boolean currentlyCapturing = false;
	private float previousTriggerVal = 0.0f;
	
	public OscilloscopeProcessor( OscilloscopeMadInstance instance, 
			ArrayList<OscilloscopeWriteableScopeData> bufferedScopeData )
	{
		this.instance = instance;
		this.bufferedScopeData = bufferedScopeData;
	}
	
	public void setPeriodData( OscilloscopeCaptureTriggerEnum triggerEnum,
			int desiredCaptureSamples,
			int periodLength )
	{
		this.periodLength = periodLength;
//		this.desiredCaptureSamples = desiredCaptureSamples;
		this.triggerEnum = triggerEnum;
	}
	
	public void processPeriod( ThreadSpecificTemporaryEventStorage tses,
			long timingInfo,
			boolean triggerConnected, float[] triggerFloats,
			boolean audio0Connected, float[] audio0Floats,
			boolean cv0Connected, float[] cv0Floats,
			boolean audio1Connected, float[] audio1Floats,
			boolean cv1Connected, float[] cv1Floats )
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
						for( int s = 0 ; s < periodLength ; s++ )
						{
							float testVal = triggerFloats[ s ];
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
						for( int s = 0 ; s < periodLength ; s++ )
						{
							float testVal = triggerFloats[ s ];
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
				int numAvailable = periodLength - triggerIndex;
				int scopeSpace = scopeData.desiredDataLength - scopeData.currentWriteIndex;
				int numToWrite = (numAvailable < scopeSpace ? numAvailable : scopeSpace );
				for( int s = 0 ; s < numToWrite ; s++ )
				{
					int inIndex = triggerIndex + s;
					int outIndex = scopeData.currentWriteIndex + s;
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
			previousTriggerVal = triggerFloats[ periodLength - 1 ];
		}
	}

}
