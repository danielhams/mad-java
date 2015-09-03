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

package uk.co.modularaudio.mads.base.scope.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.scope.mu.ScopeMadDefinition;
import uk.co.modularaudio.mads.base.scope.mu.ScopeMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNoNameChangeNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEventUiConsumer;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class ScopeMadUiInstance extends
		AbstractNoNameChangeNonConfigurableMadUiInstance<ScopeMadDefinition, ScopeMadInstance> implements
		IOQueueEventUiConsumer<ScopeMadInstance>
{
	private static Log log = LogFactory.getLog( ScopeMadUiInstance.class.getName() );

	public ScopeMadUiInstance( final ScopeMadInstance instance,
			final ScopeMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}

	@Override
	public void destroy()
	{
		super.destroy();
	}

	@Override
	public void consumeQueueEntry( final ScopeMadInstance instance,
			final IOQueueEvent queueEvent )
	{
//		log.debug("Received queue event: " + queueEvent.toString() );
		switch( queueEvent.command )
		{
//			case SoundfilePlayerIOQueueBridge.COMMAND_OUT_RECYCLE_SAMPLE:
//			{
//				BlockResamplingClient resampledSample = (BlockResamplingClient)queueEvent.object;
//				try
//				{
//					advancedComponentsFrontController.unregisterCacheClientForFile( resampledSample.getSampleCacheClient() );
//				}
//				catch( final Exception e )
//				{
//					if( log.isErrorEnabled() )
//					{
//						log.error("Failed to unregister cache client for file: " + e.toString(), e );
//					}
//				}
//				resampledSample = null;
//				break;
//			}
			default:
			{
				if( log.isErrorEnabled() )
				{
					log.error("Unknown message received in UI: " + queueEvent.command );
				}
				break;
			}
		}
	}

	@Override
	public void doDisplayProcessing(
			final ThreadSpecificTemporaryEventStorage guiTemporaryEventStorage,
			final MadTimingParameters timingParameters, final long currentGuiTick)
	{
		localQueueBridge.receiveQueuedEventsToUi(guiTemporaryEventStorage, instance, this );
		super.doDisplayProcessing(guiTemporaryEventStorage, timingParameters, currentGuiTick);
	}

	@Override
	public void receiveStartup(final HardwareIOChannelSettings ratesAndLatency,
			final MadTimingParameters timingParameters,
			final MadFrameTimeFactory frameTimeFactory)
	{
		super.receiveStartup(ratesAndLatency, timingParameters, frameTimeFactory);
	}

	@Override
	public void receiveStop()
	{
		super.receiveStop();
	}
}
