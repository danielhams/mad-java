package uk.co.modularaudio.libsndfilewrapper;

public class LibSndfileWrapperLoader
{
	static
	{
		System.loadLibrary( "sndfile_wrap" );
	}

	public static final void loadIt()
	{
		// Do nothing
	}
}
