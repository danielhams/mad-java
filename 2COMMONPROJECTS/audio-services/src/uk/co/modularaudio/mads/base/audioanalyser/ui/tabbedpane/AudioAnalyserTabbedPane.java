/**
 *
 * Copyright (C) 2015 - Daniel Hams, Modular Audio Limited
 *                      daniel.hams@gmail.com
 *
 * Mad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mad.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.co.modularaudio.mads.base.audioanalyser.ui.tabbedpane;

import java.awt.Font;

import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import uk.co.modularaudio.mads.base.audioanalyser.ui.AudioAnalyserUiBufferState;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;

public class AudioAnalyserTabbedPane extends JTabbedPane
{
	private static final long serialVersionUID = -4248511027552356776L;

//	private final Log log = LogFactory.getLog( AudioAnalyserTabbedPane.class.getName() );

	private final AudioAnalyserOscilloscope oscilloscopeComponent;
	private final AudioAnalyserSpectralRoll spectralRollComponent;
	private final AudioAnalyserSpectralAmp spectralAmpComponent;

	private final AudioAnalyserDisplay[] displays;

	private int currentSelectedDisplay = 0;

	public AudioAnalyserTabbedPane( final AudioAnalyserUiBufferState uiBufferState, final BufferedImageAllocator bia )
	{
		super( JTabbedPane.TOP );
		setOpaque(false);

		final Font f = getFont();
		final Font newFont = f.deriveFont(9.0f);
		setFont( newFont );

		displays = new AudioAnalyserDisplay[3];

		oscilloscopeComponent = new AudioAnalyserOscilloscope( uiBufferState, bia );
		displays[0] = oscilloscopeComponent.getDisplay();
		spectralRollComponent = new AudioAnalyserSpectralRoll( uiBufferState, bia );
		displays[1] = spectralRollComponent.getDisplay();
		spectralAmpComponent = new AudioAnalyserSpectralAmp( uiBufferState, bia );
		displays[2] = spectralAmpComponent.getDisplay();
		this.addTab( "Oscilloscope", oscilloscopeComponent );
		this.addTab( "Spectral Roll", spectralRollComponent );
		this.addTab( "Spectral Amps", spectralAmpComponent );

		this.setBorder( new EmptyBorder( 0,0,0,0 ) );
	}

	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime )
	{
		final AudioAnalyserDisplay whichDisplay = displays[currentSelectedDisplay];
		whichDisplay.doDisplayProcessing( tempEventStorage,
					timingParameters,
					currentGuiTime );
	}

	@Override
	public void setSelectedIndex(final int index)
	{
		currentSelectedDisplay = index;
		super.setSelectedIndex(index);
		displays[index].setNeedsFullUpdate();
	}
}
