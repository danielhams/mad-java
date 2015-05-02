package uk.co.modularaudio.service.blockresampler;

import uk.co.modularaudio.service.samplecaching.SampleCacheClient;

public interface NTBlockResamplingClient
{
	long getFramePosition();
	void setFramePosition(long newPosition);

	void setFpOffset(float newFpOffset);
	float getFpOffset();

	long getTotalNumFrames();

	SampleCacheClient getSampleCacheClient();
}
