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
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDisplayView.DisplayOrientation;
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDisplayView.SatelliteOrientation;

public class WaveRollerCaptureLengthSliderUiJComponent extends PacCaptureLengthSlider
	implements IMadUiControlInstance<WaveRollerMadDefinition, WaveRollerMadInstance, WaveRollerMadUiInstance>,
		WaveRollerCaptureTimeProducer
{
	private static Log log = LogFactory.getLog( WaveRollerCaptureLengthSliderUiJComponent.class.getName() );
	
	private static final long serialVersionUID = 2538907435465770032L;
	
//	private RPOscilloscopeMadUiInstance uiInstance = null;
	
	private WaveRollerDataListener dataListener = null;

	public WaveRollerCaptureLengthSliderUiJComponent( WaveRollerMadDefinition definition,
			WaveRollerMadInstance instance,
			WaveRollerMadUiInstance uiInstance,
			int controlIndex )
	{
		super( 1.0f, 5000.0f, 1500.0f,
				"ms",
				SatelliteOrientation.LEFT,
				DisplayOrientation.HORIZONTAL,
				SatelliteOrientation.RIGHT,
				"Capture Time:",
				Color.WHITE,
				Color.WHITE,
				false );
//		this.uiInstance = uiInstance;
		
		uiInstance.setCaptureTimeProducer( this );
	}

	public JComponent getControl()
	{
		return this;
	}

	private void passChangeToInstanceData( float newValue )
	{
		if( dataListener != null )
		{
			dataListener.setCaptureTimeMillis( newValue );
		}
	}

	@Override
	public void doDisplayProcessing( ThreadSpecificTemporaryEventStorage tempEventStorage,
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
	public float getCaptureTimeMillis()
	{
		return model.getValue();
	}

	@Override
	public void setScopeDataListener( WaveRollerDataListener dataListener )
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
