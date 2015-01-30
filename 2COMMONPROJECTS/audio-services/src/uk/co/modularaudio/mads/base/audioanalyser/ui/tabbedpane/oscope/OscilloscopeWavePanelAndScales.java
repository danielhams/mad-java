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

package uk.co.modularaudio.mads.base.audioanalyser.ui.tabbedpane.oscope;

import uk.co.modularaudio.mads.base.audioanalyser.ui.AudioAnalyserUiBufferState;
import uk.co.modularaudio.mads.base.audioanalyser.ui.tabbedpane.AAColours;
import uk.co.modularaudio.mads.base.audioanalyser.ui.tabbedpane.AudioAnalyserDisplay;
import uk.co.modularaudio.mads.base.audioanalyser.ui.tabbedpane.oscope.WaveDisplay.DisplayTypeEnum;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacPanel;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class OscilloscopeWavePanelAndScales extends PacPanel
{
//	private static Log log = LogFactory.getLog( OscilloscopeWavePanelAndScales.class.getName() );

	private static final long serialVersionUID = -4571595436958689774L;

	private final VerticalScroller verticalScroller;
	private final WaveScale leftWaveScale;
	private final WaveDisplay waveDisplay;
	private final WaveScale rightWaveScale;
	private final HorizontalScroller horizontalScroller;

	public OscilloscopeWavePanelAndScales( AudioAnalyserUiBufferState uiBufferState, BufferedImageAllocator bia )
	{
		setOpaque( true );

		setBackground(AAColours.BACKGROUND);

		MigLayoutStringHelper msh = new MigLayoutStringHelper();
//		msh.addLayoutConstraint("debug");
		msh.addLayoutConstraint("fill");
		msh.addLayoutConstraint("gap 0");
//		msh.addLayoutConstraint("insets 0");
		// 449 211
		msh.addColumnConstraint("[][25!][" + WaveDisplay.DISPLAY_WIDTH + "!][25!]");
		msh.addRowConstraint("[" + WaveDisplay.DISPLAY_HEIGHT + "!][25!]");
		setLayout(msh.createMigLayout());

		verticalScroller = new VerticalScroller();
		add( verticalScroller, "grow, cell 0 0");
		leftWaveScale = new WaveScale( true );
		add( leftWaveScale, "grow, cell 1 0");
		waveDisplay = new WaveDisplay( uiBufferState, bia );
		add( waveDisplay, "grow, cell 2 0");
		rightWaveScale = new WaveScale( false );
		add( rightWaveScale, "grow, cell 3 0");
		horizontalScroller = new HorizontalScroller( uiBufferState );
		add( horizontalScroller, "grow, cell 2 1");
	}

	public AudioAnalyserDisplay getDisplay()
	{
		return waveDisplay;
	}

	public void setDisplayType(DisplayTypeEnum displayTypeEnum)
	{
		waveDisplay.setDisplayType( displayTypeEnum );
	}
}
