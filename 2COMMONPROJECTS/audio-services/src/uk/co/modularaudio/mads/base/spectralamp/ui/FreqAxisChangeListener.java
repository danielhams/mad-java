package uk.co.modularaudio.mads.base.spectralamp.ui;

import uk.co.modularaudio.util.audio.spectraldisplay.freqscale.FrequencyScaleComputer;

public interface FreqAxisChangeListener
{
	void receiveFreqScaleComputer( FrequencyScaleComputer desiredFreqScaleComputer );
}
