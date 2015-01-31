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

package uk.co.modularaudio.mads.base.audioanalyser.ui.tabbedpane.oscope;

import uk.co.modularaudio.mads.base.audioanalyser.ui.AdditionalDataBuffers;
import uk.co.modularaudio.mads.base.audioanalyser.ui.AudioAnalyserDataBuffers;
import uk.co.modularaudio.mads.base.audioanalyser.ui.AudioAnalyserUiBufferState;
import uk.co.modularaudio.mads.base.audioanalyser.ui.BufferStateListener;
import uk.co.modularaudio.util.audio.dsp.FrequencyFilterMode;
import uk.co.modularaudio.util.audio.dsp.RBJFilter;
import uk.co.modularaudio.util.audio.dsp.RMSFilter;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class RmsDataBuffers implements AdditionalDataBuffers, BufferStateListener
{
//	private static Log log = LogFactory.getLog( RmsDataBuffers.class.getName() );

	private final AudioAnalyserUiBufferState uiBufferState;

	private final float lowRmsFreq;
	private final float midRmsFreq;
	private final float hiRmsFreq;
	private final float lowCof;
	private final float midCof;
	private final float highCof;

	private final RBJFilter lowFilter = new RBJFilter();
	private final RBJFilter midHighFilter = new RBJFilter();
	private final RBJFilter midFilter = new RBJFilter();
	private final RBJFilter highFilter = new RBJFilter();

	public float[] lowDataBuffer;
	public float[] midDataBuffer;
	public float[] hiDataBuffer;

	private RMSFilter lowRmsFilter;
	private RMSFilter midRmsFilter;
	private RMSFilter hiRmsFilter;

	public float[] lowRmsBuffer;
	public float[] midRmsBuffer;
	public float[] hiRmsBuffer;

	public RmsDataBuffers( final AudioAnalyserUiBufferState uiBufferState,
			final float lowRmsFreq,
			final float midRmsFreq,
			final float hiRmsFreq,
			final float lowCof,
			final float midCof,
			final float highCof )
	{
//		// Good for music
//		float minSmoothedFreq = 40.0f;
//		float lowCof = 100.0f;
//		float midCof = 400.0f;
//		float highCof = 10000.0f;

//		// For male speech
//		float minSmoothedFreq = 40.0f;
//		float lowCof = 500.0f;
//		float midCof = 500.0f;
//		float highCof = 4500.0f;

//		// For female speech
//		float minSmoothedFreq = 40.0f;
//		float lowCof = 550.0f;
//		float midCof = 550.0f;
//		float highCof = 4500.0f;

		this.uiBufferState = uiBufferState;
		this.lowRmsFreq = lowRmsFreq;
		this.midRmsFreq = midRmsFreq;
		this.hiRmsFreq = hiRmsFreq;
		this.lowCof = lowCof;
		this.midCof = midCof;
		this.highCof = highCof;

		final AudioAnalyserDataBuffers dataBuffers = uiBufferState.getDataBuffers();
		if( dataBuffers.bufferLength > 0 )
		{
			setup( dataBuffers.sampleRate, dataBuffers.bufferLength );
		}

		uiBufferState.addBufferStateListener(this);
	}

	@Override
	public int write(final float[] buffer, final int pos, final int length)
	{
//		log.debug("write called with pos(" + pos + ") length(" + length + ")");

		// Use the low data buffer as temp storage for midhigh processing
		midHighFilter.filter( buffer, pos, lowDataBuffer, pos, length );
		midFilter.filter( lowDataBuffer, pos, midDataBuffer, pos, length );
		highFilter.filter( lowDataBuffer, pos, hiDataBuffer, pos, length );
		lowFilter.filter( buffer, pos, lowDataBuffer, pos, length );

		lowRmsFilter.filter( lowDataBuffer, pos, lowRmsBuffer, pos, length );
		midRmsFilter.filter( midDataBuffer, pos, midRmsBuffer, pos, length );
		hiRmsFilter.filter( hiDataBuffer, pos, hiRmsBuffer, pos, length );

//		System.arraycopy( lowDataBuffer, lrp, lowRmsBuffer, lrp, numBefore );

		return length;
	}

	@Override
	public void receiveStartup(final HardwareIOChannelSettings ratesAndLatency,
			final MadTimingParameters timingParameters)
	{
		final int sampleRate = ratesAndLatency.getAudioChannelSetting().getDataRate().getValue();
		final int bufferLength = uiBufferState.getDataBuffers().bufferLength;
		setup( sampleRate, bufferLength );

		reset();
	}

	public void reset()
	{
		lowRmsFilter.reset();
		midRmsFilter.reset();
		hiRmsFilter.reset();
	}

	@Override
	public void receiveStop()
	{
	}

	@Override
	public void receiveDestroy()
	{
	}

	private void setup( final int sampleRate, final int bufferLength )
	{
		lowFilter.recompute(sampleRate, FrequencyFilterMode.LP, lowCof, RBJFilter.ZERO_RESONANCE );
		midHighFilter.recompute(sampleRate, FrequencyFilterMode.HP, midCof, RBJFilter.ZERO_RESONANCE );
		midFilter.recompute(sampleRate, FrequencyFilterMode.LP, highCof, RBJFilter.ZERO_RESONANCE );
		highFilter.recompute(sampleRate, FrequencyFilterMode.HP, highCof, RBJFilter.ZERO_RESONANCE );

		lowDataBuffer = new float[ bufferLength ];
		midDataBuffer = new float[ bufferLength ];
		hiDataBuffer = new float[ bufferLength ];

		lowRmsFilter = new RMSFilter(sampleRate, lowRmsFreq);
		midRmsFilter = new RMSFilter(sampleRate, midRmsFreq );
		hiRmsFilter = new RMSFilter(sampleRate, hiRmsFreq );

		lowRmsBuffer = new float[ bufferLength ];
		midRmsBuffer = new float[ bufferLength ];
		hiRmsBuffer = new float[ bufferLength ];
	}
}
