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

package uk.co.modularaudio.mads.base.staticvalue.ui;

import java.awt.Color;

import javax.swing.JComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.staticvalue.mu.StaticValueMadDefinition;
import uk.co.modularaudio.mads.base.staticvalue.mu.StaticValueMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDisplayView.DisplayOrientation;
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDisplayView.SatelliteOrientation;

public class StaticValueSliderUiJComponent extends ValueSlider
	implements IMadUiControlInstance<StaticValueMadDefinition, StaticValueMadInstance, StaticValueMadUiInstance>
{
	private static Log log = LogFactory.getLog( StaticValueSliderUiJComponent.class.getName() );
	
	private static final long serialVersionUID = 2538907435465770032L;
	
	private StaticValueMadUiInstance uiInstance = null;

	public StaticValueSliderUiJComponent( StaticValueMadDefinition definition,
			StaticValueMadInstance instance,
			StaticValueMadUiInstance uiInstance,
			int controlIndex )
	{
		super( -500.0f, 500.0f, 0.1f,
				"",
				SatelliteOrientation.LEFT,
				DisplayOrientation.HORIZONTAL,
				SatelliteOrientation.RIGHT,
				"Value:",
				Color.BLACK,
				Color.BLACK,
				false );
//		this.setBackground( Color.ORANGE );
		this.uiInstance = uiInstance;
	}

	public JComponent getControl()
	{
		return this;
	}

	private void passChangeToInstanceData( float newValue )
	{
		uiInstance.sendValueChange( newValue );
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
	public boolean needsDisplayProcessing()
	{
		return false;
	}
}
