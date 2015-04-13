package uk.co.modularaudio.mads.base.soundfile_player.ui;

import java.util.ArrayList;

import uk.co.modularaudio.service.samplecaching.BufferFillCompletionListener;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;

public class PositionJumpCacheRefresher implements BufferFillCompletionListener
{
	private final ArrayList<SoundfileSampleEventListener> sampleEventListeners;

	public PositionJumpCacheRefresher(final ArrayList<SoundfileSampleEventListener> sampleEventListeners )
	{
		this.sampleEventListeners = sampleEventListeners;
	}

	@Override
	public void notifyBufferFilled( final SampleCacheClient sampleCacheClient )
	{
		for( final SoundfileSampleEventListener sel : sampleEventListeners )
		{
			sel.receiveCacheRefreshCompletionEvent();
		}
	}

}
