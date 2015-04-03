package uk.co.modularaudio.service.libsndfileaudiofileio;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.libsndfilewrapper.swig.CArrayFloat;
import uk.co.modularaudio.libsndfilewrapper.swig.SF_INFO;
import uk.co.modularaudio.libsndfilewrapper.swig.SWIGTYPE_p_SNDFILE_tag;
import uk.co.modularaudio.libsndfilewrapper.swig.SWIGTYPE_p_float;
import uk.co.modularaudio.service.audiofileio.AudioFileHandleAtom;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService.AudioFileDirection;
import uk.co.modularaudio.service.audiofileio.StaticMetadata;

public class LibSndfileAtom implements AudioFileHandleAtom
{
	private static Log log = LogFactory.getLog( LibSndfileAtom.class.getName() );

	protected final static int CARRAY_BUFFER_LENGTH = 4096;

	protected final LibSndfileAudioFileIOService service;
	protected final AudioFileDirection direction;
	protected final StaticMetadata staticMetadata;

	protected SF_INFO sfInfo;
	protected SWIGTYPE_p_SNDFILE_tag sndfilePtr;
	protected CArrayFloat cArrayFloat;
	protected SWIGTYPE_p_float floatPtr;

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

		cArrayFloat = new CArrayFloat( CARRAY_BUFFER_LENGTH );
		floatPtr = cArrayFloat.cast();
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
