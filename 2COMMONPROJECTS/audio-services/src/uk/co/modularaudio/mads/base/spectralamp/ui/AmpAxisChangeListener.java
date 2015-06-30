package uk.co.modularaudio.mads.base.spectralamp.ui;

import uk.co.modularaudio.util.audio.spectraldisplay.ampscale.AmpScaleComputer;

public interface AmpAxisChangeListener
{
	void receiveAmpMinDbChange( float newMinDB );
	void receiveAmpMaxDbChange( float newMaxDB );

	void receiveAmpScaleComputer( AmpScaleComputer desiredAmpScaleComputer );
}
