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

package uk.co.modularaudio.service.brokenaudiofileio;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.audiofileio.AudioFileHandleAtom;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService;
import uk.co.modularaudio.service.audiofileio.StaticMetadata;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService.AudioFileDirection;
import uk.co.modularaudio.util.audio.fileio.IAudioDataFetcher;

public class InternalFileHandleAtom implements AudioFileHandleAtom
{
	private static Log log = LogFactory.getLog( InternalFileHandleAtom.class.getName() );

	protected final BrokenAudioFileIOService service;
	protected final AudioFileDirection direction;
	protected final StaticMetadata staticMetadata;

	protected final IAudioDataFetcher internalDataFetcher;

	public InternalFileHandleAtom( final BrokenAudioFileIOService service,
			final AudioFileDirection direction,
			final StaticMetadata staticMetadata,
			final IAudioDataFetcher dataFetcher )
	{
		this.service = service;
		this.direction = direction;
		this.staticMetadata = staticMetadata;
		this.internalDataFetcher = dataFetcher;
	}

	@Override
	public AudioFileDirection getDirection()
	{
		return direction;
	}

	@Override
	public StaticMetadata getStaticMetadata()
	{
		return staticMetadata;
	}

	protected int read( final float[] destFloats, final int destPosition, final int numFrames, final long frameReadOffset )
	{
		try
		{
			return internalDataFetcher.read( destFloats,
					destPosition,
					frameReadOffset * staticMetadata.numChannels,
					numFrames * staticMetadata.numChannels )
					/ staticMetadata.numChannels;
		}
		catch( final Exception e )
		{
			final String msg = "Exception caught reading floats: " + e.toString();
			log.error( msg, e );
			return -1;
		}
	}

	protected void close()
	{
		internalDataFetcher.close();
	}

	@Override
	public AudioFileIOService getAudioFileIOService()
	{
		return service;
	}
}
