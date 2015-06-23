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

import javax.swing.JComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.interptester.mu.InterpTesterMadDefinition;
import uk.co.modularaudio.mads.base.interptester.mu.InterpTesterMadInstance;
import uk.co.modularaudio.mads.base.interptester.utils.InterpTesterSliderModels;
import uk.co.modularaudio.mads.base.interptester.utils.SliderModelValueConverter;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView.DisplayOrientation;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView.SatelliteOrientation;

public class InterpTesterValueSliderUiJComponent extends ValueSlider
	implements IMadUiControlInstance<InterpTesterMadDefinition, InterpTesterMadInstance, InterpTesterMadUiInstance>,
	ModelChangeReceiver
{
	private static Log log = LogFactory.getLog( InterpTesterValueSliderUiJComponent.class.getName() );

	private static final long serialVersionUID = 2538907435465770032L;

	private final InterpTesterMadUiInstance uiInstance;

	private SliderModelValueConverter valueConverter;

	private final InterpTesterSliderModels sliderModels;

	private long lastEventCountNanos = 0;
	private int numEvents = 0;

	private final static boolean DEBUG_EVENTS_PER_SECOND = false;

	public InterpTesterValueSliderUiJComponent( final InterpTesterMadDefinition definition,
			final InterpTesterMadInstance instance,
			final InterpTesterMadUiInstance uiInstance,
			final int controlIndex )
	{
		super( instance.getModels().getModelAt( 0 ),
				SatelliteOrientation.ABOVE,
				DisplayOrientation.VERTICAL,
				SatelliteOrientation.BELOW,
				"Value:",
				false );
		this.uiInstance = uiInstance;
		uiInstance.setModelChangeReceiver( this );
		this.sliderModels = instance.getModels();
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	private void passChangeToInstanceData( final float iSliderValue )
	{
		final float checkedValue = ( valueConverter == null ? iSliderValue :
			valueConverter.convertValue( iSliderValue ) );

		uiInstance.setValue( checkedValue );

		if( DEBUG_EVENTS_PER_SECOND )
		{
			final long curNanos = System.nanoTime();

			final long diff = curNanos - lastEventCountNanos;
			if( diff > 1000 * 1000 * 1000 )
			{
				lastEventCountNanos = curNanos;
				if( log.isDebugEnabled() )
				{
					log.debug("Did " + numEvents + " within the last second");
				}
				numEvents = 0;
			}
			numEvents++;
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
		final SliderDisplayModel newModel = sliderModels.getModelAt( selectedIndex );
		valueConverter = sliderModels.getValueConverterAt( selectedIndex );
		changeModel( newModel );
	}
}
