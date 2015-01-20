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

package uk.co.modularaudio.util.audio.envelope;


public class FixedTransitionAdsrEnvelope
{
//	private static Log log = LogFactory.getLog( FixedTransitionAdsrEnvelope.class.getName() );
	
	private enum Segment
	{
		ATTACK,
		DECAY,
		SUSTAIN,
		RELEASE
	};
	
	// Internal vars
	private Segment currentState = Segment.RELEASE;
	private int currentSegmentPosition = 0;
	
	// Things manipulated from outside
	private int numAttackSamples = 0;
	private int numDecaySamples = 0;
	private int numReleaseSamples = 0;
	private float startAttackAmplitude = 0.0f;
	private float targetAttackAmplitude = 0.0f;
	private float startReleaseAmplitude = 0.0f;
	private float targetSustainAmplitude = 0.0f;
	
	public FixedTransitionAdsrEnvelope()
	{
	}
	
	public void start( boolean startFromZero,
			int numAttackSamples,
			int numDecaySamples,
			int numReleaseSamples,
			float attackAmplitude,
			float sustainAmplitude )
	{
		this.numAttackSamples = numAttackSamples;
		this.numDecaySamples = numDecaySamples;
		this.numReleaseSamples = numReleaseSamples;
		this.targetAttackAmplitude = attackAmplitude;
		this.targetSustainAmplitude = sustainAmplitude;
		
		if( startFromZero )
		{
			startAttackAmplitude = 0.0f;
		}
		else
		{
			switch( currentState )
			{
				case ATTACK:
				{
					startAttackAmplitude = startReleaseAmplitude;
					break;
				}
				case DECAY:
				{
					break;
				}
				case SUSTAIN:
				{
					startAttackAmplitude = startReleaseAmplitude;
					break;
				}
				case RELEASE:
				{
					break;
				}
			}
		}
		
		currentSegmentPosition = 0;
		currentState = Segment.ATTACK;
	}
	
	public void release()
	{
		if( targetSustainAmplitude == 0.0f )
		{
			switch( currentState )
			{
				case ATTACK:
				{
					currentState = Segment.DECAY;
					targetAttackAmplitude = startReleaseAmplitude;
					currentSegmentPosition = 0;
					break;
				}
				case DECAY:
				{
					break;
				}
				default:
				{
				}
			}
			// Carry on
		}
		else
		{
			switch( currentState )
			{
				case DECAY:
				{
					startReleaseAmplitude = startAttackAmplitude;
//					log.debug("Switching to decay with release amplitude equal to attack amp (" + startReleaseAmplitude + ")");
				}
				default:
				{
				}
			}
			currentState =  Segment.RELEASE;
			currentSegmentPosition = 0;
		}
	}
	
	public float outputEnvelope( float[] output, int outputIndex, int length )
	{
		for( int pos = 0 ; pos < length ; pos++ )
		{
			float newVal = nextValue();
			output[ outputIndex + pos ] = newVal;
		}

		return output[ outputIndex + (length - 1) ];
	}

	public final float nextValue()
	{
		checkForSegmentTransition();
		
		// Now calculate the amplitude at this position
		float newVal = 0.0f;
		switch( currentState )
		{
			case ATTACK:
			{
				// Goes from 0 -> 1
				float attackDistance = (float)currentSegmentPosition / numAttackSamples;
				float normSegIndex = ((1.0f / numAttackSamples) * currentSegmentPosition );
				// And we want any previous decaying value to dissapear as soon as possible
				float decayingVal = 1.0f - attackDistance;
//					float decayingVal = 1.0f - (normSegIndex * 1);
				decayingVal = ( decayingVal < 0 ? 0 : decayingVal);
				float startVal = startAttackAmplitude;
				float endVal = targetAttackAmplitude ;
				newVal = ((startVal * decayingVal) + (endVal * normSegIndex));
				startReleaseAmplitude = newVal;
//					if( startAttackAmplitude > 0.0f )
//					{
//						log.debug("NZ attack start amplitude: " + startAttackAmplitude + " newVal is " + newVal  + " and normseg is " +
//								normSegIndex + " and decayingval is " + decayingVal );
//					}
//					log.debug("Outputting sample with attack amp: " + newVal );
				break;
			}
			case DECAY:
			{
				// From 0 -> 1
				float normSegIndex = ((1.0f / numDecaySamples) * currentSegmentPosition);
				// From 1 -> 0
				float decayingVal = 1.0f - normSegIndex;
				float startVal = startReleaseAmplitude;
				float endVal = targetSustainAmplitude;
				newVal = ((startVal * decayingVal) + (endVal * normSegIndex));
				startAttackAmplitude = newVal;
//					log.debug("Outputting sample with decay amp: " + newVal  + " and normSegIndex: " + normSegIndex );
				break;
			}
			case SUSTAIN:
			{
				newVal = targetSustainAmplitude;
				startReleaseAmplitude = newVal;
				startAttackAmplitude = newVal;
				break;
			}
			case RELEASE:
			{
				if( currentSegmentPosition < numReleaseSamples )
				{
					// From 0 -> 1
					float normSegIndex = ((1.0f / (numReleaseSamples-1)) * currentSegmentPosition);
					// From 1-> 0
					float decayingVal = 1.0f - normSegIndex;
					float startVal = startReleaseAmplitude;
					float endVal = 0.0f;
					newVal = (startVal * decayingVal) + (endVal * normSegIndex );
					startAttackAmplitude = newVal;
//						log.debug("Outputting sample with release amp: " + newVal  + " and normSegIndex: " + normSegIndex );
				}
				else
				{
					newVal = 0.0f;
					startAttackAmplitude = 0.0f;
//						log.debug("Outputting sample with release amp 0.0f and resetting start attack amp to zero");
				}
				break;
			}
		}
		currentSegmentPosition++;
		return newVal;
	}

	private final void checkForSegmentTransition()
	{
		if( currentState == Segment.SUSTAIN || currentState == Segment.RELEASE )
		{
			return;
		}
		boolean transition = true;
		do
		{
			if( currentState == Segment.ATTACK )
			{
				if( currentSegmentPosition >= numAttackSamples )
				{
					currentState = Segment.DECAY;
					currentSegmentPosition = 0;
					transition = true;
					if( numAttackSamples == 0 )
					{
						startReleaseAmplitude = 1.0f;
					}
				}
				else
				{
					transition = false;
				}
			}
			else if( currentState == Segment.DECAY )
			{
				if( currentSegmentPosition >= numDecaySamples )
				{
					if( targetSustainAmplitude == 0.0f )
					{
						currentState = Segment.RELEASE;
						currentSegmentPosition = numReleaseSamples;
						startAttackAmplitude = 0.0f;
						transition = true;
					}
					else
					{
						currentState = Segment.SUSTAIN;
						currentSegmentPosition = 0;
						transition = true;
					}
				}
				else
				{
					transition = false;
				}
			}
			else
			{
				transition = false;
			}
		}
		while( transition );
	}
}
