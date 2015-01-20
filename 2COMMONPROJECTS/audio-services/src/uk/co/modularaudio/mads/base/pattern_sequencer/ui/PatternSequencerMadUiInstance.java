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

package uk.co.modularaudio.mads.base.pattern_sequencer.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.pattern_sequencer.mu.PatternSequencerMadDefinition;
import uk.co.modularaudio.mads.base.pattern_sequencer.mu.PatternSequencerMadInstance;
import uk.co.modularaudio.mads.base.pattern_sequencer.mu.PatternSequencerIOQueueBridge;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEventUiConsumer;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.general.FloatJSliderModel;

public class PatternSequencerMadUiInstance extends AbstractNonConfigurableMadUiInstance<PatternSequencerMadDefinition, PatternSequencerMadInstance>
	implements IOQueueEventUiConsumer<PatternSequencerMadInstance>
{
	private final static Log log = LogFactory.getLog( PatternSequencerMadUiInstance.class.getName() );
	
	public final static double START_VAL = 128.0;
	public final static double MIN_VAL = 100.0;
	public final static double MAX_VAL = 200.0;
	public final static double VAL_RES = 1.0;
	
	protected FloatJSliderModel valueFloatSliderModel = new FloatJSliderModel( START_VAL, MIN_VAL, MAX_VAL, VAL_RES );
	
	protected long knownAudioIOLatencyNanos = 0;
	
	public PatternSequencerMadUiInstance( PatternSequencerMadInstance instance,
			PatternSequencerMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}

	@Override
	public void doDisplayProcessing( ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTick )
	{
		long newAln = timingParameters.getNanosOutputLatency();
		if( newAln != knownAudioIOLatencyNanos )
		{
			knownAudioIOLatencyNanos = newAln;
		}
		localQueueBridge.receiveQueuedEventsToUi( tempEventStorage, instance, this );
		super.doDisplayProcessing( tempEventStorage, timingParameters, currentGuiTick );
	}

	@Override
	public void consumeQueueEntry( PatternSequencerMadInstance instance,
			IOQueueEvent nextOutgoingEntry )
	{
		switch( nextOutgoingEntry.command )
		{
			default:
			{
				log.warn("Unhandled outgoing queue command: " + nextOutgoingEntry.command);
			}
		}
	}
	
	public void sendToggleRun( boolean desiredRunValue )
	{
		sendTemporalValueToInstance( PatternSequencerIOQueueBridge.COMMAND_IN_TOGGLE_RUN,  (desiredRunValue ? 1 : 0 ) );
	}

	public void sendBpmChange( float floatValue )
	{
		sendTemporalValueToInstance( PatternSequencerIOQueueBridge.COMMAND_IN_BPM, Float.floatToIntBits( floatValue ) );
	}
}
