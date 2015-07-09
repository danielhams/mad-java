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

package uk.co.modularaudio.service.libsndfileaudiofileio;

import uk.co.modularaudio.libsndfilewrapper.swig.SF_INFO;
import uk.co.modularaudio.libsndfilewrapper.swig.SWIGTYPE_p_SNDFILE_tag;
import uk.co.modularaudio.service.audiofileio.AudioFileHandleAtom;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService.AudioFileDirection;
import uk.co.modularaudio.service.audiofileio.StaticMetadata;

public class LibSndfileAtom implements AudioFileHandleAtom
{
//	private static Log log = LogFactory.getLog( LibSndfileAtom.class.getName() );

	protected final static int CARRAY_BUFFER_LENGTH = 4096;

	protected final LibSndfileAudioFileIOService service;
	protected final AudioFileDirection direction;
	protected final StaticMetadata staticMetadata;

	protected SF_INFO sfInfo;
	protected SWIGTYPE_p_SNDFILE_tag sndfilePtr;

	protected long currentHandleFrameOffset;

	public LibSndfileAtom( final LibSndfileAudioFileIOService service,
			final AudioFileDirection direction,
			final StaticMetadata staticMetadata,
			final SF_INFO sfInfo,
			final SWIGTYPE_p_SNDFILE_tag sndfilePtr )
	{
		this.service = service;
		this.direction = direction;
		this.staticMetadata = staticMetadata;
		this.sfInfo = sfInfo;
		this.sndfilePtr = sndfilePtr;
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

	@Override
	public AudioFileIOService getAudioFileIOService()
	{
		return service;
	}

}
