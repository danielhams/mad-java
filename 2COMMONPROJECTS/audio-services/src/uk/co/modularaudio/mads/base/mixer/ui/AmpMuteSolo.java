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

package uk.co.modularaudio.mads.base.mixer.ui;

import java.awt.Color;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

public class AmpMuteSolo extends JPanel
{
	private static final long serialVersionUID = 1325276594564910791L;
	
//	private static Log log = LogFactory.getLog( AmpMuteSolo.class.getName() );
	
	private ChannelLaneMixerPanelUiInstance channelLaneMixerUiInstance = null;
	
	private MuteSoloToggleButton muteButton = null;
	private MuteSoloToggleButton soloButton = null;

	public AmpMuteSolo( ChannelLaneMixerPanelUiInstance channelLaneMixerPanelUiInstance )
	{
		channelLaneMixerUiInstance = channelLaneMixerPanelUiInstance;

		this.setBackground( Color.cyan );

		this.setOpaque( false );
		MigLayout compLayout = new MigLayout("insets 5, gap 0, fill");
		this.setLayout( compLayout );

		muteButton = new MuteSoloToggleButton( "m" )
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void receiveToggleEvent( boolean value )
			{
				muteButtonClick( value );
			}
		};
		this.add( muteButton, "align right, shrink" );
		soloButton = new MuteSoloToggleButton( "s" )
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void receiveToggleEvent( boolean value )
			{
				soloButtonClick( value );
			}
		};
		this.add( soloButton, "align left, shrink" );

		this.validate();
	}
	
	private void muteButtonClick( boolean toggleValue )
	{
		channelLaneMixerUiInstance.setMuteValue( toggleValue );
	}
	
	private void soloButtonClick( boolean toggleValue )
	{
		channelLaneMixerUiInstance.setSoloValue( toggleValue );
	}

	public void receiveMuteSet( boolean muted )
	{
		muteButton.setSelected( muted );
	}
	
	public void receiveSoloSet( boolean solod )
	{
		soloButton.setSelected( solod );
	}

	public void receiveControlValue( String muteSoloSetting )
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
