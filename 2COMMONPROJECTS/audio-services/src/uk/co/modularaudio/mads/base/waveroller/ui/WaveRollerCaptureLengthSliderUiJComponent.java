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

package uk.co.modularaudio.mads.base.waveroller.ui;

import java.awt.Color;

import javax.swing.JComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.waveroller.mu.WaveRollerMadDefinition;
import uk.co.modularaudio.mads.base.waveroller.mu.WaveRollerMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView.DisplayOrientation;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView.SatelliteOrientation;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderViewColors;

public class WaveRollerCaptureLengthSliderUiJComponent extends PacCaptureLengthSlider
	implements IMadUiControlInstance<WaveRollerMadDefinition, WaveRollerMadInstance, WaveRollerMadUiInstance>,
		WaveRollerCaptureTimeProducer
{
	private static final long serialVersionUID = 2538907435465770032L;

	private static Log log = LogFactory.getLog( WaveRollerCaptureLengthSliderUiJComponent.class.getName() );

	public static final LWTCSliderViewColors SLIDER_COLORS = getSliderColors();

	private static final LWTCSliderViewColors getSliderColors()
	{
		final Color bgColor = Color.black;
		final Color fgColor = Color.white;
		final Color indicatorColor = null;
		final Color textboxBgColor = LWTCControlConstants.CONTROL_TEXTBOX_BACKGROUND;
		final Color textboxFgColor = LWTCControlConstants.CONTROL_TEXTBOX_FOREGROUND;
		final Color selectionColor = LWTCControlConstants.CONTROL_TEXTBOX_SELECTION;
		final Color selectedTextColor = LWTCControlConstants.CONTROL_TEXTBOX_SELECTED_TEXT;
		final Color labelColor = LWTCControlConstants.CONTROL_LABEL_FOREGROUND;
		final Color unitsColor = labelColor;

		return new LWTCSliderViewColors( bgColor,
				fgColor,
				indicatorColor,
				textboxBgColor,
				textboxFgColor,
				selectionColor,
				selectedTextColor,
				labelColor,
				unitsColor );
	}

	private WaveRollerDataListener dataListener;

	public WaveRollerCaptureLengthSliderUiJComponent( final WaveRollerMadDefinition definition,
			final WaveRollerMadInstance instance,
			final WaveRollerMadUiInstance uiInstance,
			final int controlIndex )
	{
		super( 1.0f, 5000.0f, 1500.0f, 1500f,
				"ms",
				SatelliteOrientation.LEFT,
				DisplayOrientation.HORIZONTAL,
				SatelliteOrientation.RIGHT,
				SLIDER_COLORS,
				"Capture Time:",
				false );
//		this.uiInstance = uiInstance;

		uiInstance.setCaptureTimeProducer( this );
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	private void passChangeToInstanceData( final float newValue )
	{
		if( dataListener != null )
		{
			dataListener.setCaptureTimeMillis( newValue );
		}
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		// log.debug("Received display tick");
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
	public float getCaptureTimeMillis()
	{
		return model.getValue();
	}

	@Override
	public void setScopeDataListener( final WaveRollerDataListener dataListener )
	{
		this.dataListener = dataListener;
		dataListener.setCaptureTimeMillis( model.getValue() );
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}
}
