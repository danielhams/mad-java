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

import uk.co.modularaudio.util.audio.wavetable.valuemapping.ValueMappingWaveTable;

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
	private int indexInCurrentSegment = -1;
	
	private float startAttackAmp = 0.0f;
	private float startReleaseAmp = 0.0f;

	private float lastOutputAttackAmp = 0.0f;
	private float lastOutputDecayAmp = 0.0f;
	private float lastOutputSustainAmp = 0.0f;
	private float lastOutputReleaseAmp = 0.0f;
	
	public EnvelopeRuntime()
	{
	}
	
	public EnvelopeSegment getCurrentSegment()
	{
		return currentSegment;
	}
	
	public void trigger( Envelope envelope )
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
	
	public void release( Envelope envelope )
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

	public void outputEnvelope( Envelope envelope,
			float[] outputGate,
			float[] outputAmps,
			int outputOffset,
			int lastOutputOffset,
			int length )
	{
		// Output amps
		if( currentSegment == EnvelopeSegment.OFF )
		{
			Arrays.fill( outputAmps, outputOffset, lastOutputOffset, 0.0f );
			Arrays.fill( outputGate, outputOffset, lastOutputOffset, 0.0f );
		}
		else if( currentSegment == EnvelopeSegment.SUSTAIN )
		{
			float sustainLevel = envelope.getSustainLevel();
			Arrays.fill( outputAmps, outputOffset, lastOutputOffset, sustainLevel );
			Arrays.fill( outputGate, outputOffset, lastOutputOffset, 1.0f );
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
						int samplesForSegment = envelope.getAttackSamples();
						
						// Work out if we have any left
						int samplesLeftInSegment = samplesForSegment - indexInCurrentSegment;
//						log.debug("Num attack samples left " + samplesLeftInSegment );
						
						// Now work out how many we can output
						if( samplesLeftInSegment > 0 )
						{
							ValueMappingWaveTable waveTableForSegment = envelope.getAttackWaveTable();
							
							int samplesThisRound = (samplesLeftInSegment < samplesLeft ? samplesLeftInSegment : samplesLeft );
//							log.debug("Doing " + samplesThisRound + " attack samples this round");
							float progressPerSample = 1.0f / samplesForSegment;
							float curPosition = progressPerSample * indexInCurrentSegment;
							for( int i = 0 ; i < samplesThisRound ; i++, curPosition += progressPerSample )
							{
								curPosition = (curPosition > 1.0f ? 1.0f : curPosition);
								float wtValue = waveTableForSegment.getValueAtNormalisedPosition( curPosition );
								float nonWtVaue = (1.0f - wtValue );
								outputAmps[ outputOffset + i ] = startAttackAmp * nonWtVaue + wtValue;
							}
							int lastOutputIndex = outputOffset + samplesThisRound;
							Arrays.fill( outputGate, outputOffset, lastOutputIndex, 1.0f );
							
							indexInCurrentSegment += samplesThisRound;
							outputOffset += samplesThisRound;
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
						int samplesForSegment = envelope.getDecaySamples();
						
						// Work out if we have any left
						int samplesLeftInSegment = samplesForSegment - indexInCurrentSegment;
						
						// Now work out how many we can output
						if( samplesLeftInSegment > 0 )
						{
							ValueMappingWaveTable waveTableForSegment = envelope.getDecayWaveTable();
							float sustainLevel = envelope.getSustainLevel();
							
							int samplesThisRound = (samplesLeftInSegment < samplesLeft ? samplesLeftInSegment : samplesLeft );
							float progressPerSample = 1.0f / samplesForSegment;
							float curPosition = progressPerSample * indexInCurrentSegment;
							for( int i = 0 ; i < samplesThisRound ; i++, curPosition += progressPerSample )
							{
								curPosition = (curPosition > 1.0f ? 1.0f : curPosition );
								float wtValue = waveTableForSegment.getValueAtNormalisedPosition( curPosition );
								float nonWtValue = (1.0f - wtValue);
								outputAmps[ outputOffset + i ] = lastOutputAttackAmp * nonWtValue + (sustainLevel * wtValue);
							}
							int lastOutputIndex = outputOffset + samplesThisRound;
							Arrays.fill( outputGate, outputOffset, lastOutputIndex, 1.0f );
							
							lastOutputDecayAmp = outputAmps[ lastOutputIndex - 1];
							indexInCurrentSegment += samplesThisRound;
							outputOffset += samplesThisRound;
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
						float sustainLevel = envelope.getSustainLevel();
						int lastOutputIndex = outputOffset + samplesLeft;
						Arrays.fill( outputAmps, outputOffset, lastOutputIndex, sustainLevel );
						Arrays.fill( outputGate, outputOffset, lastOutputIndex, 1.0f );
						lastOutputSustainAmp = sustainLevel;
						outputOffset += samplesLeft;
						samplesLeft -= samplesLeft;
						break;
					}
					case RELEASE:
					{
						int samplesForSegment = envelope.getReleaseSamples();
						
						// Work out if we have any left
						int samplesLeftInSegment = samplesForSegment - indexInCurrentSegment;
						
						// Now work out how many we can output
						if( samplesLeftInSegment > 0 )
						{
							ValueMappingWaveTable waveTableForSegment = envelope.getReleaseWaveTable();
							
							int samplesThisRound = (samplesLeftInSegment < samplesLeft ? samplesLeftInSegment : samplesLeft );
							float progressPerSample = 1.0f / samplesForSegment;
							float curPosition = progressPerSample * indexInCurrentSegment;

							for( int i = 0 ; i < samplesThisRound ; i++, curPosition += progressPerSample )
							{
								curPosition = (curPosition > 1.0f ? 1.0f : curPosition );
								float wtValue = waveTableForSegment.getValueAtNormalisedPosition( curPosition );
								float nonWtValue = (1.0f - wtValue);
								outputAmps[ outputOffset + i ] = startReleaseAmp * nonWtValue;
							}
							int lastOutputIndex = outputOffset + samplesThisRound;
							Arrays.fill( outputGate, outputOffset, lastOutputIndex, 1.0f );
							lastOutputReleaseAmp = outputAmps[ lastOutputIndex - 1];
							indexInCurrentSegment += samplesThisRound;
							outputOffset += samplesThisRound;
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
						int lastOutputIndex = outputOffset + samplesLeft;
						Arrays.fill( outputAmps, outputOffset, lastOutputIndex, 0.0f );
						Arrays.fill( outputGate, outputOffset, lastOutputIndex, 0.0f );
						outputOffset += samplesLeft;
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
