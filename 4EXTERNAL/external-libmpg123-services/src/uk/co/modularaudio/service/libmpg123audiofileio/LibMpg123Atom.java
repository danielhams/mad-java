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

package uk.co.modularaudio.service.libmpg123audiofileio;

import uk.co.modularaudio.libmpg123wrapper.swig.CArrayInt;
import uk.co.modularaudio.libmpg123wrapper.swig.SWIGTYPE_p_int;
import uk.co.modularaudio.libmpg123wrapper.swig.SWIGTYPE_p_mpg123_handle_struct;
import uk.co.modularaudio.service.audiofileio.AudioFileHandleAtom;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService.AudioFileDirection;
import uk.co.modularaudio.service.audiofileio.StaticMetadata;

public class LibMpg123Atom implements AudioFileHandleAtom
{
//	private static Log log = LogFactory.getLog( LibMpg123Atom.class.getName() );

	protected final static int CARRAY_BUFFER_LENGTH = 4096;

	protected final LibMpg123AudioFileIOService service;
	protected final AudioFileDirection direction;
	protected final StaticMetadata staticMetadata;

	protected long currentHandleFrameOffset;

	protected SWIGTYPE_p_mpg123_handle_struct handle;

	protected long currentPosition;
	protected CArrayInt doneArray;
	protected SWIGTYPE_p_int donePtr;

	public LibMpg123Atom( final LibMpg123AudioFileIOService service,
			final AudioFileDirection direction,
			final StaticMetadata staticMetadata,
			final SWIGTYPE_p_mpg123_handle_struct handle  )
	{
		this.service = service;
		this.direction = direction;
		this.staticMetadata = staticMetadata;
		this.handle = handle;

		doneArray = new CArrayInt(1);
		donePtr = doneArray.cast();
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
