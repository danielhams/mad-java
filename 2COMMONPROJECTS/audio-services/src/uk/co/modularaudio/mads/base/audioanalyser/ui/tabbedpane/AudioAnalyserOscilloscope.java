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

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import uk.co.modularaudio.mads.base.audioanalyser.ui.AudioAnalyserUiBufferState;
import uk.co.modularaudio.mads.base.audioanalyser.ui.tabbedpane.oscope.OscilloscopeWavePanelAndScales;
import uk.co.modularaudio.mads.base.audioanalyser.ui.tabbedpane.oscope.WaveDisplay;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacLabel;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacPanel;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacToggleButton;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacToggleGroup;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class AudioAnalyserOscilloscope extends JPanel
{
	private static final long serialVersionUID = -7810773245971714032L;

//	private static Log log = LogFactory.getLog( AudioAnalyserOscilloscope.class.getName() );

	private final PacLabel ampLabel;

	private final static String[] AMP_OPTION_LABELS = new String[] {
		"lin",
		"dB",
		"3speech",
		"3music"
	};

	private final static WaveDisplay.DisplayTypeEnum[] AMP_OPTION_DISPLAY_TYPES = new WaveDisplay.DisplayTypeEnum[] {
		WaveDisplay.DisplayTypeEnum.RAW,
		WaveDisplay.DisplayTypeEnum.dB,
		WaveDisplay.DisplayTypeEnum.ThreeSpeech,
		WaveDisplay.DisplayTypeEnum.ThreeMusic
	};

//	private final AudioAnalyserUiBufferState uiBufferState;

	private final PacToggleGroup ampToggleGroup;
	private final PacPanel ampPanel;
	private final LeftButton leftButton;
	private final RightButton rightButton;
	private final FreezeButton freezeButton;
	private final OscilloscopeWavePanelAndScales wavePanelAndScales;
	private final AudioAnalyserDisplay display;

	private static void doSetFont( JComponent comp )
	{
		Font f = comp.getFont();
		Font newFont = f.deriveFont( 9.0f );
		comp.setFont( newFont );
	}

	public AudioAnalyserOscilloscope( AudioAnalyserUiBufferState uiBufferState, BufferedImageAllocator bia )
	{
//		this.uiBufferState = uiBufferState;

		MigLayoutStringHelper msh = new MigLayoutStringHelper();
//		msh.addLayoutConstraint( "debug" );

		msh.addLayoutConstraint("fill");
		msh.addColumnConstraint("[][][grow][]");
		msh.addRowConstraint("[][grow]");

		setLayout( msh.createMigLayout() );
		ampLabel = new PacLabel("Amp:");
		doSetFont( ampLabel );
		add( ampLabel, "align right" );

		ampToggleGroup = new PacToggleGroup( AMP_OPTION_LABELS, 0 )
		{

			@Override
			public void receiveUpdateEvent(int previousSelection, int newSelection)
			{
				if( newSelection != previousSelection )
				{
					wavePanelAndScales.setDisplayType( AMP_OPTION_DISPLAY_TYPES[ newSelection ] );
				}
			}
		};
		ampPanel = new PacPanel();
		MigLayoutStringHelper amh = new MigLayoutStringHelper();
		amh.addLayoutConstraint("gap 0");
		amh.addLayoutConstraint("insets 0");
		amh.addLayoutConstraint("fill");
		ampPanel.setLayout( amh.createMigLayout());
		PacToggleButton[] atbs = ampToggleGroup.getToggleButtons();
		for( PacToggleButton tb : atbs )
		{
			doSetFont(tb);
			ampPanel.add( tb, "");
		}
		add( ampPanel, "align left");
		leftButton = new LeftButton( uiBufferState );
		doSetFont( leftButton );
		add( leftButton, "align right");
		rightButton = new RightButton( uiBufferState );
		doSetFont( rightButton );
		add( rightButton, "align left");

		freezeButton = new FreezeButton( uiBufferState );
		doSetFont(freezeButton);
		add( freezeButton, "wrap");

		wavePanelAndScales = new OscilloscopeWavePanelAndScales( uiBufferState, bia );
		display = wavePanelAndScales.getDisplay();
		add( wavePanelAndScales, "span 5, grow" );

		this.setBorder( new EmptyBorder( 0, 0, 0, 0 ) );
	}

	public AudioAnalyserDisplay getDisplay()
	{
		return display;
	}
}
