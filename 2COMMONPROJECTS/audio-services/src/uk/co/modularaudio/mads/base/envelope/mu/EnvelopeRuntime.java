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

package uk.co.modularaudio.mads.base.envelope.mu;

import java.util.Arrays;

import uk.co.modularaudio.util.audio.lookuptable.valuemapping.ValueMappingWaveTable;

public class EnvelopeRuntime
{
//	private static Log log = LogFactory.getLog( EnvelopeRuntime.class.getName() );

	enum EnvelopeSegment
	{
		ATTACK,
		DECAY,
		SUSTAIN,
		RELEASE,
		OFF
	};

	private EnvelopeSegment currentSegment = EnvelopeSegment.OFF;
	private int indexInCurrentSegment;

	private float startAttackAmp;
	private float startReleaseAmp;

	private float lastOutputAttackAmp;
	private float lastOutputDecayAmp;
	private float lastOutputSustainAmp;
	private float lastOutputReleaseAmp;

	public EnvelopeRuntime()
	{
	}

	public EnvelopeSegment getCurrentSegment()
	{
		return currentSegment;
	}

	public void trigger( final Envelope envelope )
	{
//		log.debug("Trigger");
		if( envelope.getAttackFromZero() )
		{
			startAttackAmp = 0.0f;
		}
		else
		{
			switch( currentSegment )
			{
				case ATTACK:
				{
					startAttackAmp = lastOutputAttackAmp;
					break;
				}
				case DECAY:
				{
					startAttackAmp = lastOutputDecayAmp;
					break;
				}
				case SUSTAIN:
				{
					startAttackAmp = lastOutputSustainAmp;
					break;
				}
				case RELEASE:
				{
					startAttackAmp = lastOutputReleaseAmp;
					break;
				}
				case OFF:
				{
					startAttackAmp = 0.0f;
					break;
				}
			}
		}
		indexInCurrentSegment = 0;
		currentSegment = EnvelopeSegment.ATTACK;
	}

	public void release( final Envelope envelope )
	{
//		log.debug("Release");
		if( envelope.getReleaseSamples() == 0 )
		{
			currentSegment = EnvelopeSegment.OFF;
		}
		else
		{
			switch( currentSegment )
			{
				case ATTACK:
				{
					startReleaseAmp = lastOutputAttackAmp;
					break;
				}
				case DECAY:
				{
					startReleaseAmp = lastOutputDecayAmp;
					break;
				}
				case SUSTAIN:
				{
					startReleaseAmp = lastOutputSustainAmp;
					break;
				}
				case RELEASE:
				{
					startReleaseAmp = lastOutputReleaseAmp;
					break;
				}
				default:
				{
				}
			}
			currentSegment = EnvelopeSegment.RELEASE;
		}
		indexInCurrentSegment = 0;
	}

	public void outputEnvelope( final Envelope envelope,
			final float[] outputGate,
			final float[] outputAmps,
			final int outputOffset,
			final int lastOutputOffset,
			final int length )
	{
		int curOutputOffset = outputOffset;
		// Output amps
		if( currentSegment == EnvelopeSegment.OFF )
		{
			Arrays.fill( outputAmps, curOutputOffset, lastOutputOffset, 0.0f );
			Arrays.fill( outputGate, curOutputOffset, lastOutputOffset, 0.0f );
		}
		else if( currentSegment == EnvelopeSegment.SUSTAIN )
		{
			final float sustainLevel = envelope.getSustainLevel();
			Arrays.fill( outputAmps, curOutputOffset, lastOutputOffset, sustainLevel );
			Arrays.fill( outputGate, curOutputOffset, lastOutputOffset, 1.0f );
			lastOutputSustainAmp = sustainLevel;
		}
		else
		{
			// For the amps, we have to loop around draining the values out of the envelope wavetables
			int samplesLeft = length;

			do
			{
//				log.debug( "Looping around outputting envelope with " + samplesLeft + " samples left at output index " + outputOffset + " in state " + currentSegment.toString() );
				switch( currentSegment )
				{
					case ATTACK:
					{
						// Find out how many samples the envelope should output for the current segment
						final int samplesForSegment = envelope.getAttackSamples();

						// Work out if we have any left
						final int samplesLeftInSegment = samplesForSegment - indexInCurrentSegment;
//						log.debug("Num attack samples left " + samplesLeftInSegment );

						// Now work out how many we can output
						if( samplesLeftInSegment > 0 )
						{
							final ValueMappingWaveTable waveTableForSegment = envelope.getAttackWaveTable();

							final int samplesThisRound = (samplesLeftInSegment < samplesLeft ? samplesLeftInSegment : samplesLeft );
//							log.debug("Doing " + samplesThisRound + " attack samples this round");
							final float progressPerSample = 1.0f / samplesForSegment;
							float curPosition = progressPerSample * indexInCurrentSegment;
							for( int i = 0 ; i < samplesThisRound ; i++, curPosition += progressPerSample )
							{
								curPosition = (curPosition > 1.0f ? 1.0f : curPosition);
								final float wtValue = waveTableForSegment.getValueAtNormalisedPosition( curPosition );
								final float nonWtVaue = (1.0f - wtValue );
								outputAmps[ curOutputOffset + i ] = startAttackAmp * nonWtVaue + wtValue;
							}
							final int lastOutputIndex = curOutputOffset + samplesThisRound;
							Arrays.fill( outputGate, curOutputOffset, lastOutputIndex, 1.0f );

							indexInCurrentSegment += samplesThisRound;
							curOutputOffset += samplesThisRound;
							samplesLeft -= samplesThisRound;
							// Make sure final attack sample is one
							if( indexInCurrentSegment >= samplesForSegment )
							{
								outputAmps[ lastOutputIndex - 1 ] = 1.0f;
							}
							lastOutputAttackAmp = outputAmps[ lastOutputIndex - 1];
						}
						else
						{
							// Transition from attack
							if( samplesForSegment == 0 )
							{
								lastOutputAttackAmp =1.0f;
							}

							if( envelope.getDecaySamples() > 0 )
							{
								currentSegment = EnvelopeSegment.DECAY;
								indexInCurrentSegment = 0;
							}
							else if( envelope.getSustainLevel() > 0.0f )
							{
								currentSegment = EnvelopeSegment.SUSTAIN;
								indexInCurrentSegment = 0;
							}
							else
							{
								currentSegment = EnvelopeSegment.OFF;
								indexInCurrentSegment = 0;
							}
						}

						break;
					}
					case DECAY:
					{
						// Find out how many samples the envelope should output for the current segment
						final int samplesForSegment = envelope.getDecaySamples();

						// Work out if we have any left
						final int samplesLeftInSegment = samplesForSegment - indexInCurrentSegment;

						// Now work out how many we can output
						if( samplesLeftInSegment > 0 )
						{
							final ValueMappingWaveTable waveTableForSegment = envelope.getDecayWaveTable();
							final float sustainLevel = envelope.getSustainLevel();

							final int samplesThisRound = (samplesLeftInSegment < samplesLeft ? samplesLeftInSegment : samplesLeft );
							final float progressPerSample = 1.0f / samplesForSegment;
							float curPosition = progressPerSample * indexInCurrentSegment;
							for( int i = 0 ; i < samplesThisRound ; i++, curPosition += progressPerSample )
							{
								curPosition = (curPosition > 1.0f ? 1.0f : curPosition );
								final float wtValue = waveTableForSegment.getValueAtNormalisedPosition( curPosition );
								final float nonWtValue = (1.0f - wtValue);
								outputAmps[ curOutputOffset + i ] = lastOutputAttackAmp * nonWtValue + (sustainLevel * wtValue);
							}
							final int lastOutputIndex = curOutputOffset + samplesThisRound;
							Arrays.fill( outputGate, curOutputOffset, lastOutputIndex, 1.0f );

							lastOutputDecayAmp = outputAmps[ lastOutputIndex - 1];
							indexInCurrentSegment += samplesThisRound;
							curOutputOffset += samplesThisRound;
							samplesLeft -= samplesThisRound;
						}
						else
						{
							// Transition from decay
							if( envelope.getSustainLevel() > 0.0f )
							{
								currentSegment = EnvelopeSegment.SUSTAIN;
								indexInCurrentSegment = 0;
							}
							else
							{
								currentSegment = EnvelopeSegment.OFF;
								indexInCurrentSegment = 0;
							}
						}

						break;
					}
					case SUSTAIN:
					{
						final float sustainLevel = envelope.getSustainLevel();
						final int lastOutputIndex = curOutputOffset + samplesLeft;
						Arrays.fill( outputAmps, curOutputOffset, lastOutputIndex, sustainLevel );
						Arrays.fill( outputGate, curOutputOffset, lastOutputIndex, 1.0f );
						lastOutputSustainAmp = sustainLevel;
						curOutputOffset += samplesLeft;
						samplesLeft -= samplesLeft;
						break;
					}
					case RELEASE:
					{
						final int samplesForSegment = envelope.getReleaseSamples();

						// Work out if we have any left
						final int samplesLeftInSegment = samplesForSegment - indexInCurrentSegment;

						// Now work out how many we can output
						if( samplesLeftInSegment > 0 )
						{
							final ValueMappingWaveTable waveTableForSegment = envelope.getReleaseWaveTable();

							final int samplesThisRound = (samplesLeftInSegment < samplesLeft ? samplesLeftInSegment : samplesLeft );
							final float progressPerSample = 1.0f / samplesForSegment;
							float curPosition = progressPerSample * indexInCurrentSegment;

							for( int i = 0 ; i < samplesThisRound ; i++, curPosition += progressPerSample )
							{
								curPosition = (curPosition > 1.0f ? 1.0f : curPosition );
								final float wtValue = waveTableForSegment.getValueAtNormalisedPosition( curPosition );
								final float nonWtValue = (1.0f - wtValue);
								outputAmps[ curOutputOffset + i ] = startReleaseAmp * nonWtValue;
							}
							final int lastOutputIndex = curOutputOffset + samplesThisRound;
							Arrays.fill( outputGate, curOutputOffset, lastOutputIndex, 1.0f );
							lastOutputReleaseAmp = outputAmps[ lastOutputIndex - 1];
							indexInCurrentSegment += samplesThisRound;
							curOutputOffset += samplesThisRound;
							samplesLeft -= samplesThisRound;
						}
						else
						{
							currentSegment = EnvelopeSegment.OFF;
							indexInCurrentSegment = 0;
						}
						break;
					}
					case OFF:
					{
						final int lastOutputIndex = curOutputOffset + samplesLeft;
						Arrays.fill( outputAmps, curOutputOffset, lastOutputIndex, 0.0f );
						Arrays.fill( outputGate, curOutputOffset, lastOutputIndex, 0.0f );
						curOutputOffset += samplesLeft;
						samplesLeft -= samplesLeft;
						break;
					}
				}
			}
			while( samplesLeft > 0 );
		}
	}

	public void reset()
	{
		currentSegment = EnvelopeSegment.OFF;
		indexInCurrentSegment = 0;
		startAttackAmp = 0.0f;
	}


}
