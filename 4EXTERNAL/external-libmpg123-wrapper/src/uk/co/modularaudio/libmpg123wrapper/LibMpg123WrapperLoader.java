package uk.co.modularaudio.libmpg123wrapper;

public class LibMpg123WrapperLoader
{
	static
	{
		System.loadLibrary( "mpg123_wrap" );
	}

	public static final void loadIt()
	{
		// Do nothing
	}
}
