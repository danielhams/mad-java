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

package uk.co.modularaudio.mads.subrack.ui.runnable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.rackmarshalling.RackMarshallingService;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;

public class LoadNewSubrackRunnable implements Runnable
{
	private static Log log = LogFactory.getLog( LoadNewSubrackRunnable.class.getName() );

	private final RackMarshallingService rackMarshallingService;

	private final String fileToLoad;
	private final SubrackLoadCompletionListener completionListener;

	public LoadNewSubrackRunnable(
			final RackMarshallingService rackMarshallingService,
			final String fileToLoad,
			final SubrackLoadCompletionListener completionListener )
	{
		this.rackMarshallingService = rackMarshallingService;
		this.fileToLoad = fileToLoad;
		this.completionListener = completionListener;
	}

	@Override
	public void run()
	{
		try
		{
			final RackDataModel newRackDataModel = rackMarshallingService.loadSubRackFromFile( fileToLoad );
			completionListener.notifyLoadCompleted( newRackDataModel );
		}
		catch ( final Exception e )
		{
			final String msg = "Exception caught attempting to load new rack from file " + fileToLoad +
					": " + e.toString();
			log.error( msg, e );
			completionListener.notifyLoadFailure();
		}
	}

}
