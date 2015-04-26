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

package uk.co.modularaudio.mads.base.stereo_compressor.ui;

import java.awt.Font;

import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.stereo_compressor.mu.StereoCompressorMadDefinition;
import uk.co.modularaudio.mads.base.stereo_compressor.mu.StereoCompressorMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacCheckBox;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class StereoCompressorLookaheadCheckboxUiJComponent extends PacCheckBox
		implements IMadUiControlInstance<StereoCompressorMadDefinition, StereoCompressorMadInstance, StereoCompressorMadUiInstance>
{
	private static final long serialVersionUID = 6068897521037173787L;

	private final StereoCompressorMadUiInstance uiInstance;

	public StereoCompressorLookaheadCheckboxUiJComponent(  final StereoCompressorMadDefinition definition,
			final StereoCompressorMadInstance instance,
			final StereoCompressorMadUiInstance uiInstance,
			final int controlIndex )
	{
		super();
		this.uiInstance = uiInstance;
		this.setOpaque( false );
//		Font f = this.getFont().deriveFont( 9f );
		final Font f = this.getFont();
		setFont( f );
		this.setText( "4ms Lookahead" );
		// Default value
		this.setSelected( true );
		this.setSelected( false );
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	private void passChangeToInstanceData( final boolean selected )
	{
		uiInstance.sendLookahead( selected );
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		// log.debug("Received display tick");
	}

	@Override
	public void receiveUpdate( final boolean statusBefore, final boolean newStatus )
	{
		if( statusBefore != newStatus )
		{
			passChangeToInstanceData( newStatus );
		}
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}
}
