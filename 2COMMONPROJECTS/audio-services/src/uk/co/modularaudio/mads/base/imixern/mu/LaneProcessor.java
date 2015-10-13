package uk.co.modularaudio.mads.base.imixern.mu;

import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;

public interface LaneProcessor
{

	void setSampleRate( int sampleRate );

	void processLane( ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			MadChannelConnectedFlags channelConnectedFlags,
			MadChannelBuffer[] channelBuffers,
			int frameProcessingOffset,
			int numThisRound );

	void emitLaneMeterReadings( ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			long emitFrameTime );

	void setLaneAmp( float ampValue );
	void setLanePan( float panValue );

	void setLaneActive( boolean active );

}
