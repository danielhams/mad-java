package uk.co.modularaudio.util.audio.math;

import uk.co.modularaudio.util.exception.DatastoreException;

public class TooManyBitsException extends DatastoreException
{
	private static final long serialVersionUID = 7349753816438331227L;

	public TooManyBitsException( final String cause )
	{
		super( cause );
	}
}