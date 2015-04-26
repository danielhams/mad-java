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

import javax.swing.JComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadDefinition;
import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadInstance;
import uk.co.modularaudio.mads.base.waveroller.ui.WaveRollerCaptureLengthSliderUiJComponent;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView.DisplayOrientation;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView.SatelliteOrientation;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDoubleClickMouseListener.SliderDoubleClickReceiver;

public class SoundfilePlayerSpeedSliderUiJComponent extends PacPlaybackSpeedSlider
	implements IMadUiControlInstance<SoundfilePlayerMadDefinition, SoundfilePlayerMadInstance, SoundfilePlayerMadUiInstance>,
	SliderDoubleClickReceiver
{
	private static Log log = LogFactory.getLog( SoundfilePlayerSpeedSliderUiJComponent.class.getName() );

	private static final long serialVersionUID = 2538907435465770032L;

	private final SoundfilePlayerMadUiInstance uiInstance;

	public SoundfilePlayerSpeedSliderUiJComponent( final SoundfilePlayerMadDefinition definition,
			final SoundfilePlayerMadInstance instance,
			final SoundfilePlayerMadUiInstance uiInstance,
			final int controlIndex )
	{
		super( -150.0f, 150.0f, 100.0f, 100.0f,
				"%",
				SatelliteOrientation.ABOVE,
				DisplayOrientation.VERTICAL,
				SatelliteOrientation.BELOW,
				WaveRollerCaptureLengthSliderUiJComponent.SLIDER_COLORS,
				"Speed:",
				false );

		view.addDoubleClickReceiver(this);

		this.uiInstance = uiInstance;
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	private void passChangeToInstanceData( final float newValue )
	{
		uiInstance.sendPlayingSpeed(newValue/100.0f);
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
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
	public void receiveControlValue( final String valueStr )
	{
		try
		{
//			log.debug("Received control value " + value );
			final float asFloat = Float.parseFloat( valueStr );
			model.setValue( this, asFloat );
			receiveValueChange( this, asFloat );
		}
		catch( final Exception e )
		{
			final String msg = "Failed to parse control value: " + valueStr;
			log.error( msg, e );
		}
	}

	@Override
	public void receiveValueChange( final Object source, final float newValue )
	{
		passChangeToInstanceData( newValue );
	}

	@Override
	public void receiveDoubleClick()
	{
		controller.setValue(this, model.getDefaultValue());
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}
}
