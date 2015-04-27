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

package uk.co.modularaudio.mads.base.imixern.ui.lane;

import java.awt.Color;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.mads.base.imixern.mu.MixerNMadDefinition;
import uk.co.modularaudio.mads.base.imixern.mu.MixerNMadInstance;
import uk.co.modularaudio.mads.base.imixern.ui.MixerNMadUiInstance;

public class LaneMuteSolo<D extends MixerNMadDefinition<D, I>,
		I extends MixerNMadInstance<D, I>,
		U extends MixerNMadUiInstance<D, I>>
	extends JPanel
{
	private static final long serialVersionUID = 1325276594564910791L;

//	private static Log log = LogFactory.getLog( AmpMuteSolo.class.getName() );

	private final LaneMixerPanelUiInstance<D,I,U> laneMixerUiInstance;

	private final MuteSoloToggleButton muteButton;
	private final MuteSoloToggleButton soloButton;

	public LaneMuteSolo( final LaneMixerPanelUiInstance<D,I,U> channelLaneMixerPanelUiInstance )
	{
		laneMixerUiInstance = channelLaneMixerPanelUiInstance;

		this.setBackground( Color.cyan );

		this.setOpaque( false );
		final MigLayout compLayout = new MigLayout("insets 5, gap 0, fill, center");
		this.setLayout( compLayout );

		muteButton = new MuteSoloToggleButton( "m" )
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void receiveToggleEvent( final boolean value )
			{
				muteButtonClick( value );
			}
		};
		this.add( muteButton, "bottom, alignx center, shrink, wrap" );
		soloButton = new MuteSoloToggleButton( "s" )
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void receiveToggleEvent( final boolean value )
			{
				soloButtonClick( value );
			}
		};
		this.add( soloButton, "top, alignx center, shrink" );

		this.validate();
	}

	private void muteButtonClick( final boolean toggleValue )
	{
		laneMixerUiInstance.setMuteValue( toggleValue );
	}

	private void soloButtonClick( final boolean toggleValue )
	{
		laneMixerUiInstance.setSoloValue( toggleValue );
	}

	public void receiveMuteSet( final boolean muted )
	{
		muteButton.setSelected( muted );
	}

	public void receiveSoloSet( final boolean solod )
	{
		soloButton.setSelected( solod );
	}

	public void receiveControlValue( final Object source, final String muteSoloSetting )
	{
		if( muteSoloSetting != null && muteSoloSetting.length() >= 2 )
		{
			muteButton.receiveControlValue( (muteSoloSetting.charAt( 0 ) == 'M' ? "true" : "false" ) );
			soloButton.receiveControlValue( (muteSoloSetting.charAt( 1 ) == 'S' ? "true" : "false" ) );
		}
	}

	public String getControlValue()
	{
		return (muteButton.isSelected() ? "M" : " " ) + (soloButton.isSelected() ? "S" : " " );
	}
}
