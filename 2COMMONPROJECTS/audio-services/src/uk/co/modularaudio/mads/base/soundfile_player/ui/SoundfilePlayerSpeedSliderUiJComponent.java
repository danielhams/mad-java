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

package uk.co.modularaudio.mads.base.soundfile_player.ui;

import java.awt.Color;

import javax.swing.JComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadDefinition;
import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDisplayView.DisplayOrientation;
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDisplayView.SatelliteOrientation;
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDoubleClickMouseListener.SliderDoubleClickReceiver;

public class SoundfilePlayerSpeedSliderUiJComponent extends PacPlaybackSpeedSlider
	implements IMadUiControlInstance<SoundfilePlayerMadDefinition, SoundfilePlayerMadInstance, SoundfilePlayerMadUiInstance>,
	SliderDoubleClickReceiver
{
	private static Log log = LogFactory.getLog( SoundfilePlayerSpeedSliderUiJComponent.class.getName() );
	
	private static final long serialVersionUID = 2538907435465770032L;
	
	private SoundfilePlayerMadUiInstance uiInstance = null;
	
	public SoundfilePlayerSpeedSliderUiJComponent( SoundfilePlayerMadDefinition definition,
			SoundfilePlayerMadInstance instance,
			SoundfilePlayerMadUiInstance uiInstance,
			int controlIndex )
	{
		super( -150.0f, 150.0f, 100.0f,
//		super( 0.0f, 200.0f, 100.0f,
				"%",
				SatelliteOrientation.ABOVE,
				DisplayOrientation.VERTICAL,
				SatelliteOrientation.BELOW,
				"Speed:",
				Color.WHITE,
				Color.WHITE,
				false );
				
		view.addDoubleClickReceiver(this);
		
		this.uiInstance = uiInstance;
	}

	public JComponent getControl()
	{
		return this;
	}

	private void passChangeToInstanceData( float newValue )
	{
		uiInstance.sendPlayingSpeed(newValue/100.0f);
	}

	@Override
	public void doDisplayProcessing( ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
	}

	@Override
	public void destroy()
	{
	}
	
	@Override
	public String getControlValue()
	{
		return model.getValue() + "";
	}

	@Override
	public void receiveControlValue( String valueStr )
	{
		try
		{
//			log.debug("Received control value " + value );
			float asFloat = Float.parseFloat( valueStr );
			model.setValue( this, asFloat );
			receiveValueChange( this, asFloat );
		}
		catch( Exception e )
		{
			String msg = "Failed to parse control value: " + valueStr;
			log.error( msg, e );
		}
	}

	@Override
	public void receiveValueChange( Object source, float newValue )
	{
		passChangeToInstanceData( newValue );
	}

	@Override
	public void receiveDoubleClick()
	{
		controller.setValue(this, model.getInitialValue());
		receiveValueChange(this, model.getInitialValue());
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}
}
