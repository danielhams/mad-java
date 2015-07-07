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

import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadDefinition;
import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.PlaybackSpeedSliderModel;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayController;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel.ValueChangeListener;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView.DisplayOrientation;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView.SatelliteOrientation;

public class SoundfilePlayerSpeedSliderUiJComponent
	implements IMadUiControlInstance<SoundfilePlayerMadDefinition, SoundfilePlayerMadInstance, SoundfilePlayerMadUiInstance>
{
//	private static Log log = LogFactory.getLog( SoundfilePlayerSpeedSliderUiJComponent.class.getName() );

	private final PlaybackSpeedSliderModel model;
	private final SliderDisplayController controller;
	private final LWTCSliderDisplayView view;

	public SoundfilePlayerSpeedSliderUiJComponent( final SoundfilePlayerMadDefinition definition,
			final SoundfilePlayerMadInstance instance,
			final SoundfilePlayerMadUiInstance uiInstance,
			final int controlIndex )
	{
		model = new PlaybackSpeedSliderModel();

		controller = new SliderDisplayController( model );

		view = new LWTCSliderDisplayView(
				model,
				controller,
				SatelliteOrientation.BELOW,
				DisplayOrientation.VERTICAL,
				SatelliteOrientation.BELOW,
				LWTCControlConstants.SLIDER_VIEW_COLORS,
				"Speed",
				false,
				true );

		model.addChangeListener( new ValueChangeListener()
		{

			@Override
			public void receiveValueChange( final Object source, final float newValue )
			{
				uiInstance.sendPlayingSpeed( newValue );
			}
		} );
	}

	@Override
	public JComponent getControl()
	{
		return view;
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
		return Float.toString( model.getValue() );
	}

	@Override
	public void receiveControlValue( final String valueStr )
	{
//			log.debug("Received control value " + value );
		final float asFloat = Float.parseFloat( valueStr );
		controller.setValue( this, asFloat );
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}
}
