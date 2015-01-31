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

import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.lookuptable.valuemapping.StandardValueMappingWaveTables;
import uk.co.modularaudio.util.audio.lookuptable.valuemapping.ValueMappingWaveTable;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;

public class Envelope
{
	private int sampleRate = DataRate.SR_44100.getValue();

	private boolean attackFromZero = EnvelopeDefaults.ATTACK_FROM_ZERO;
	private float attackMillis = EnvelopeDefaults.ATTACK_MILLIS;
	private int attackSamplesLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, attackMillis );
	private ValueMappingWaveTable attackWaveTable = getTableForChoice( EnvelopeDefaults.ATTACK_WAVE_CHOICE );

	private float decayMillis = EnvelopeDefaults.DECAY_MILLIS;
	private int decaySamplesLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, decayMillis );
	private ValueMappingWaveTable decayWaveTable = getTableForChoice( EnvelopeDefaults.DECAY_WAVE_CHOICE );

	private float sustainLevel = EnvelopeDefaults.SUSTAIN_LEVEL;

	private float releaseMillis = EnvelopeDefaults.RELEASE_MILLIS;
	private int releaseSamplesLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, releaseMillis );
	private ValueMappingWaveTable releaseWaveTable = getTableForChoice( EnvelopeDefaults.RELEASE_WAVE_CHOICE );

	public Envelope()
	{
	}

	public void setSampleRate( final int sampleRate )
	{
		this.sampleRate = sampleRate;
		setAttackMillis( attackMillis );
		setDecayMillis( decayMillis );
		setReleaseMillis( releaseMillis );
	}

	public void setAttackFromZero( final boolean attackFromZero )
	{
		this.attackFromZero = attackFromZero;
	}

	public boolean getAttackFromZero()
	{
		return attackFromZero;
	}

	public void setAttackMillis( final float attackMillis )
	{
		this.attackMillis = attackMillis;
		attackSamplesLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, attackMillis );
	}

	public int getAttackSamples()
	{
		return attackSamplesLength;
	}

	public void setAttackWaveChoice( final EnvelopeWaveChoice attackWaveChoice )
	{
		attackWaveTable = getTableForChoice( attackWaveChoice );
	}

	public ValueMappingWaveTable getAttackWaveTable()
	{
		return attackWaveTable;
	}

	public void setDecayMillis( final float decayMillis )
	{
		this.decayMillis = decayMillis;
		decaySamplesLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, decayMillis );
	}

	public int getDecaySamples()
	{
		return decaySamplesLength;
	}

	public void setDecayWaveChoice( final EnvelopeWaveChoice decayWaveChoice )
	{
		decayWaveTable = getTableForChoice( decayWaveChoice );
	}

	public ValueMappingWaveTable getDecayWaveTable()
	{
		return decayWaveTable;
	}

	public void setSustainLevel( final float sustainLevel )
	{
		this.sustainLevel = sustainLevel;
	}

	public float getSustainLevel()
	{
		return sustainLevel;
	}

	public void setReleaseMillis( final float releaseMillis )
	{
		this.releaseMillis = releaseMillis;
		releaseSamplesLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, releaseMillis );
	}

	public int getReleaseSamples()
	{
		return releaseSamplesLength;
	}

	public void setReleaseWaveChoice( final EnvelopeWaveChoice releaseWaveChoice )
	{
		this.releaseWaveTable = getTableForChoice( releaseWaveChoice );
	}

	public ValueMappingWaveTable getReleaseWaveTable()
	{
		return releaseWaveTable;
	}

	private final static ValueMappingWaveTable getTableForChoice( final EnvelopeWaveChoice waveChoice )
	{
		switch( waveChoice )
		{
			default:
			case LINEAR:
			{
				return StandardValueMappingWaveTables.getLinearAttackMappingWaveTable();
			}
			case EXP:
			{
				return StandardValueMappingWaveTables.getExpAttackMappingWaveTable();
			}
			case EXP_FREQ:
			{
				return StandardValueMappingWaveTables.getExpFreqAttackMappingWaveTable();
			}
			case LOG:
			{
				return StandardValueMappingWaveTables.getLogAttackMappingWaveTable();
			}
			case LOG_FREQ:
			{
				return StandardValueMappingWaveTables.getLogFreqAttackMappingWaveTable();
			}
		}
	}

	public float getAttackMillis()
	{
		return attackMillis;
	}

	public float getDecayMillis()
	{
		return decayMillis;
	}

	public float getReleaseMillis()
	{
		return releaseMillis;
	}

	public void setFromEnvelope( final Envelope e )
	{
		this.sampleRate = e.sampleRate;
		this.attackFromZero = e.attackFromZero;
		this.attackMillis = e.attackMillis;
		this.attackSamplesLength = e.attackSamplesLength;
		this.attackWaveTable = e.attackWaveTable;
		this.decayMillis = e.decayMillis;
		this.decaySamplesLength = e.decaySamplesLength;
		this.decayWaveTable = e.decayWaveTable;
		this.sustainLevel = e.sustainLevel;
		this.releaseMillis = e.releaseMillis;
		this.releaseSamplesLength = e.releaseSamplesLength;
		this.releaseWaveTable = e.releaseWaveTable;
	}
}
