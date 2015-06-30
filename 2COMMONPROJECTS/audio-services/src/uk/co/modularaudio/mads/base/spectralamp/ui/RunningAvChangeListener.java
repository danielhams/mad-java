package uk.co.modularaudio.mads.base.spectralamp.ui;

import uk.co.modularaudio.util.audio.spectraldisplay.runav.RunningAverageComputer;

public interface RunningAvChangeListener
{

	void receiveRunAvComputer( RunningAverageComputer desiredRunningAverageComputer );

}
