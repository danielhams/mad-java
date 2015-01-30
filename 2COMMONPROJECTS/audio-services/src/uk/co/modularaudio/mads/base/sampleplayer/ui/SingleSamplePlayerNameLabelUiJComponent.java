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

package uk.co.modularaudio.mads.base.sampleplayer.ui;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.border.LineBorder;

import uk.co.modularaudio.mads.base.sampleplayer.mu.SingleSamplePlayerMadDefinition;
import uk.co.modularaudio.mads.base.sampleplayer.mu.SingleSamplePlayerMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacLabel;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class SingleSamplePlayerNameLabelUiJComponent extends PacLabel
	implements IMadUiControlInstance<SingleSamplePlayerMadDefinition, SingleSamplePlayerMadInstance, SingleSamplePlayerMadUiInstance>
{
//	private static Log log = LogFactory.getLog( SingleSamplePlayerNameLabelUiJComponent.class.getName() );

	private static final long serialVersionUID = -9143119755763498436L;

	public SingleSamplePlayerNameLabelUiJComponent( final SingleSamplePlayerMadDefinition definition,
			final SingleSamplePlayerMadInstance instance,
			final SingleSamplePlayerMadUiInstance uiInstance,
			final int controlIndex )
	{
		this.setOpaque( true );

		setFont( this.getFont().deriveFont( 9f ) );
		this.setBackground( Color.WHITE );
		this.setBorder( new LineBorder( Color.BLACK ) );
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
	}

	@Override
	public JComponent getControl()
	{
		return this;
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
