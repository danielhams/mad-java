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

package uk.co.modularaudio.mads.base.pattern_sequencer.mu;

import java.awt.Dimension;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.util.audio.gui.patternsequencer.PatternSequenceDefines;
import uk.co.modularaudio.util.audio.gui.patternsequencer.model.PatternSequenceModel;
import uk.co.modularaudio.util.audio.gui.patternsequencer.model.PatternSequenceModelImpl;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class PatternSequencerMadInstance extends MadInstance<PatternSequencerMadDefinition,PatternSequencerMadInstance>
{
//	private static Log log = LogFactory.getLog( PatternSequencerMadInstance.class.getName() );
	
	private static final int VALUE_CHASE_MILLIS = 1;
	private long sampleRate = 0;
	private int periodLength = -1;
	private int notePeriodLength = -1;
	
	protected float curValueRatio = 0.0f;
	protected float newValueRatio = 1.0f;
	
	private PatternSequenceModel patternDataModel = null;
	
	private AtomicReference<PatternSequencerRuntimePattern> atomicRuntimePattern = new AtomicReference<PatternSequencerRuntimePattern>();

	protected boolean desiredRun = false;
	protected float desiredBpm = 120.0f;
	
	private PatternSequencerOutputProcessor outputProcessor = null;
	

	public AtomicReference<PatternSequencerRuntimePattern> getAtomicRuntimePattern()
	{
		return atomicRuntimePattern;
	}

	public PatternSequencerMadInstance( BaseComponentsCreationContext creationContext,
			String instanceName,
			PatternSequencerMadDefinition definition,
			Map<MadParameterDefinition, String> creationParameterValues,
			MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
		
		Dimension tableDimensions = new Dimension( PatternSequenceDefines.DEFAULT_PATTERN_LENGTH, PatternSequenceDefines.DEFAULT_NUM_KEYS );
		patternDataModel = new PatternSequenceModelImpl( tableDimensions.width );
	}

	@Override
	public void startup( HardwareIOChannelSettings hardwareChannelSettings, MadTimingParameters timingParameters, MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		try
		{
			sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();
			periodLength = hardwareChannelSettings.getAudioChannelSetting().getChannelBufferLength();
			notePeriodLength = hardwareChannelSettings.getNoteChannelSetting().getChannelBufferLength();
			newValueRatio = AudioTimingUtils.calculateNewValueRatioHandwaveyVersion( sampleRate, VALUE_CHASE_MILLIS );
			curValueRatio = 1.0f - newValueRatio;
			
			outputProcessor = new PatternSequencerOutputProcessor( sampleRate, periodLength, notePeriodLength );
		}
		catch (Exception e)
		{
			throw new MadProcessingException( e );
		}
	}

	@Override
	public void stop() throws MadProcessingException
	{
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			MadTimingParameters timingParameters,
			long periodStartFrameTime,
			MadChannelConnectedFlags channelConnectedFlags,
			MadChannelBuffer[] channelBuffers, int numFrames )
	{
		boolean noteConnected = channelConnectedFlags.get( PatternSequencerMadDefinition.PRODUCER_NOTE_OUT );
		boolean cvConnected = channelConnectedFlags.get( PatternSequencerMadDefinition.PRODUCER_CV_OUT );

		MadChannelBuffer noteBufferChannel = channelBuffers[ PatternSequencerMadDefinition.PRODUCER_NOTE_OUT ];

		MadChannelBuffer cvBufferChannel = channelBuffers[ PatternSequencerMadDefinition.PRODUCER_CV_OUT ];
		float[] cvBuffer = null;
		if( cvConnected )
		{
			cvBuffer = cvBufferChannel.floatBuffer;
		}

		PatternSequencerRuntimePattern patternToRun = atomicRuntimePattern.get();
		
		if( patternToRun != null )
		{
			outputProcessor.setPatternDataBeforeStep( patternToRun, desiredBpm, desiredRun );
			
			outputProcessor.doStep( numFrames );
			
			if( noteConnected )
			{
				outputProcessor.outputNoteData( noteBufferChannel );
			}
			
			if( cvConnected )
			{
				outputProcessor.outputCvData( cvBuffer );
			}
		}
		else
		{
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	public PatternSequenceModel getPatternDataModel()
	{
		return patternDataModel;
	}
}
