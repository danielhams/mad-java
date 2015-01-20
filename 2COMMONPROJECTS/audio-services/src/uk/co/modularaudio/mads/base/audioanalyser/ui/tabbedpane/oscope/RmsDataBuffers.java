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
import uk.co.modularaudio.util.audio.dsp.RBJFilterRT;
import uk.co.modularaudio.util.audio.dsp.RMSFilter;
import uk.co.modularaudio.util.audio.dsp.RMSFilterRT;
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

	private RBJFilterRT lowFilterRt = new RBJFilterRT();
	private RBJFilterRT midHighFilterRt = new RBJFilterRT();
	private RBJFilterRT midFilterRt = new RBJFilterRT();
	private RBJFilterRT highFilterRt = new RBJFilterRT();
	
	public float[] lowDataBuffer;
	public float[] midDataBuffer;
	public float[] hiDataBuffer;
	
	private RMSFilterRT lowRmsFilterRt;
	private RMSFilterRT midRmsFilterRt;
	private RMSFilterRT hiRmsFilterRt;
	
	public float[] lowRmsBuffer;
	public float[] midRmsBuffer;
	public float[] hiRmsBuffer;
	
	public RmsDataBuffers( AudioAnalyserUiBufferState uiBufferState,
			float lowRmsFreq,
			float midRmsFreq,
			float hiRmsFreq,
			float lowCof,
			float midCof,
			float highCof )
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
		
		AudioAnalyserDataBuffers dataBuffers = uiBufferState.getDataBuffers();
		if( dataBuffers.bufferLength > 0 )
		{
			setup( dataBuffers.sampleRate, dataBuffers.bufferLength );
		}
		
		uiBufferState.addBufferStateListener(this);
	}
	
	@Override
	public int write(float[] buffer, int pos, int length)
	{
//		log.debug("write called with pos(" + pos + ") length(" + length + ")");
		
		// Use the low data buffer as temp storage for midhigh processing
		RBJFilter.filterIt( midHighFilterRt, buffer, pos, lowDataBuffer, pos, length );
		RBJFilter.filterIt( midFilterRt, lowDataBuffer, pos, midDataBuffer, pos, length );
		RBJFilter.filterIt( highFilterRt, lowDataBuffer, pos, hiDataBuffer, pos, length );
		RBJFilter.filterIt( lowFilterRt, buffer, pos, lowDataBuffer, pos, length );
		
		RMSFilter.filterIt(lowRmsFilterRt, lowDataBuffer, pos, lowRmsBuffer, pos, length );
		RMSFilter.filterIt(midRmsFilterRt, midDataBuffer, pos, midRmsBuffer, pos, length );
		RMSFilter.filterIt(hiRmsFilterRt, hiDataBuffer, pos, hiRmsBuffer, pos, length );
		
//		System.arraycopy( lowDataBuffer, lrp, lowRmsBuffer, lrp, numBefore );

		return length;
	}

	@Override
	public void receiveStartup(HardwareIOChannelSettings ratesAndLatency,
			MadTimingParameters timingParameters)
	{
		int sampleRate = ratesAndLatency.getAudioChannelSetting().getDataRate().getValue();
		int bufferLength = uiBufferState.getDataBuffers().bufferLength;
		setup( sampleRate, bufferLength );

		reset();
	}
	
	public void reset()
	{
		lowRmsFilterRt.reset();
		midRmsFilterRt.reset();
		hiRmsFilterRt.reset();
	}

	@Override
	public void receiveStop()
	{
	}

	@Override
	public void receiveDestroy()
	{
	}
	
	private void setup( int sampleRate, int bufferLength )
	{
		lowFilterRt.recompute(sampleRate, FrequencyFilterMode.LP, lowCof, RBJFilterRT.ZERO_RESONANCE );
		midHighFilterRt.recompute(sampleRate, FrequencyFilterMode.HP, midCof, RBJFilterRT.ZERO_RESONANCE );
		midFilterRt.recompute(sampleRate, FrequencyFilterMode.LP, highCof, RBJFilterRT.ZERO_RESONANCE );
		highFilterRt.recompute(sampleRate, FrequencyFilterMode.HP, highCof, RBJFilterRT.ZERO_RESONANCE );
		
		lowDataBuffer = new float[ bufferLength ];
		midDataBuffer = new float[ bufferLength ];
		hiDataBuffer = new float[ bufferLength ];
		
		lowRmsFilterRt = new RMSFilterRT(sampleRate, lowRmsFreq);
		midRmsFilterRt = new RMSFilterRT(sampleRate, midRmsFreq );
		hiRmsFilterRt = new RMSFilterRT(sampleRate, hiRmsFreq );
		
		lowRmsBuffer = new float[ bufferLength ];
		midRmsBuffer = new float[ bufferLength ];
		hiRmsBuffer = new float[ bufferLength ];
	}
}
