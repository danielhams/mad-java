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

package uk.co.modularaudio.mads.base.oscilloscope.ui;

import javax.swing.JComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeMadDefinition;
import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeMadInstance;
import uk.co.modularaudio.mads.base.waveroller.ui.PacCaptureLengthSlider;
import uk.co.modularaudio.mads.base.waveroller.ui.WaveRollerCaptureLengthSliderUiJComponent;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView.DisplayOrientation;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView.SatelliteOrientation;

public class OscilloscopeCaptureTimeUiJComponent extends PacCaptureLengthSlider
	implements IMadUiControlInstance<OscilloscopeMadDefinition, OscilloscopeMadInstance, OscilloscopeMadUiInstance>,
		OscilloscopeCaptureTimeProducer
{
	private static Log log = LogFactory.getLog( OscilloscopeCaptureTimeUiJComponent.class.getName() );

	private static final long serialVersionUID = 2538907435465770032L;

	private ScopeDataListener dataListener;

	public OscilloscopeCaptureTimeUiJComponent( final OscilloscopeMadDefinition definition,
			final OscilloscopeMadInstance instance,
			final OscilloscopeMadUiInstance uiInstance,
			final int controlIndex )
	{
		super( 1.0f, 5000.0f, 60f,
				"ms",
				SatelliteOrientation.LEFT,
				DisplayOrientation.HORIZONTAL,
				SatelliteOrientation.RIGHT,
				WaveRollerCaptureLengthSliderUiJComponent.SLIDER_COLORS,
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
		else
		{
			log.debug("Data listener is null");
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
	public void setScopeDataListener( final ScopeDataListener dataListener )
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
