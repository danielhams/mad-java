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

package uk.co.modularaudio.mads.base.interptester.ui;

import java.awt.Color;

import javax.swing.JComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.interptester.mu.InterpTesterMadDefinition;
import uk.co.modularaudio.mads.base.interptester.mu.InterpTesterMadInstance;
import uk.co.modularaudio.mads.base.interptester.utils.SliderModelValueConverter;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDisplayView.DisplayOrientation;
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDisplayView.SatelliteOrientation;

public class InterpTesterValueSliderUiJComponent extends ValueSlider
	implements IMadUiControlInstance<InterpTesterMadDefinition, InterpTesterMadInstance, InterpTesterMadUiInstance>,
	ModelChangeReceiver
{
	private static Log log = LogFactory.getLog( InterpTesterValueSliderUiJComponent.class.getName() );

	private static final long serialVersionUID = 2538907435465770032L;

	private final InterpTesterMadUiInstance uiInstance;

	private SliderModelValueConverter valueConverter;

	public InterpTesterValueSliderUiJComponent( final InterpTesterMadDefinition definition,
			final InterpTesterMadInstance instance,
			final InterpTesterMadUiInstance uiInstance,
			final int controlIndex )
	{
		super( InterpTesterMadDefinition.SLIDER_MODELS.getModelAt( 0 ),
				SatelliteOrientation.ABOVE,
				DisplayOrientation.VERTICAL,
				SatelliteOrientation.BELOW,
				"Value:",
				Color.BLACK,
				Color.BLACK,
				false );
		this.uiInstance = uiInstance;
		uiInstance.setModelChangeReceiver( this );
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	private void passChangeToInstanceData( float sliderValue )
	{
		if( valueConverter != null )
		{
			sliderValue = valueConverter.convertValue( sliderValue );
		}
		uiInstance.setValue( sliderValue );
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
		uiInstance.setModelChangeReceiver( null );
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
			final float asFloat = Float.parseFloat( valueStr );
//			log.debug("Received control value " + asFloat );
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
	public boolean needsDisplayProcessing()
	{
		return false;
	}

	@Override
	public void receiveNewModelIndex( final int selectedIndex )
	{
		final SliderDisplayModel newModel = InterpTesterMadDefinition.SLIDER_MODELS.getModelAt( selectedIndex );
		valueConverter = InterpTesterMadDefinition.SLIDER_MODELS.getValueConverterAt( selectedIndex );
		changeModel( newModel );
	}
}
