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

package uk.co.modularaudio.mads.base.soundfile_player.ui.runnable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.controller.advancedcomponents.AdvancedComponentsFrontController;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;

public class LoadNewSoundFileRunnable implements Runnable
{
	private static Log log = LogFactory.getLog( LoadNewSoundFileRunnable.class.getName() );

	private final AdvancedComponentsFrontController acfc;

	private final String fileToLoad;
	private final long position;
	private final SoundFileLoadCompletionListener completionListener;

	public LoadNewSoundFileRunnable(
			final AdvancedComponentsFrontController acfc,
			final String fileToLoad,
			final long position,
			final SoundFileLoadCompletionListener completionListener )
	{
		this.acfc = acfc;
		this.fileToLoad = fileToLoad;
		this.position = position;
		this.completionListener = completionListener;
	}

	@Override
	public void run()
	{
		try
		{
			final SampleCacheClient sampleCacheClient = acfc.registerCacheClientForFile( fileToLoad );
			if( log.isTraceEnabled() )
			{
				log.trace( "Setting position to " + position );
			}
			sampleCacheClient.setCurrentFramePosition( position );
			acfc.registerForBufferFillCompletion( sampleCacheClient, completionListener );
		}
		catch ( final Exception e )
		{
			final String msg = "Exception caught attempting to create sample cache client for file " + fileToLoad +
					": " + e.toString();
			log.error( msg, e );
			completionListener.notifyLoadFailure();
		}
	}

}
