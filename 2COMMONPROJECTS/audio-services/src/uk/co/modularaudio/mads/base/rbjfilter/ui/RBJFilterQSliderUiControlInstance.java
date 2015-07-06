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

package uk.co.modularaudio.mads.base.rbjfilter.ui;

import java.awt.Color;

import javax.swing.JComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.rbjfilter.mu.RBJFilterMadDefinition;
import uk.co.modularaudio.mads.base.rbjfilter.mu.RBJFilterMadInstance;
import uk.co.modularaudio.util.audio.dsp.RBJFilter;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacLogSlider;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDisplayView.DisplayOrientation;
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDisplayView.SatelliteOrientation;

public class RBJFilterQSliderUiControlInstance extends PacLogSlider
	implements IMadUiControlInstance<RBJFilterMadDefinition, RBJFilterMadInstance, RBJFilterMadUiInstance>
{
	private static Log log = LogFactory.getLog( RBJFilterQSliderUiControlInstance.class.getName() );

	private static final long serialVersionUID = 6068897521037173787L;

	private final RBJFilterMadUiInstance uiInstance;

	public RBJFilterQSliderUiControlInstance(
			final RBJFilterMadDefinition definition,
			final RBJFilterMadInstance instance,
			final RBJFilterMadUiInstance uiInstance,
			final int controlIndex )
	{
		super( 0.1f, 10.0f, RBJFilter.ZERO_RESONANCE,
				RBJFilter.ZERO_RESONANCE,
				SatelliteOrientation.LEFT,
				DisplayOrientation.HORIZONTAL,
				SatelliteOrientation.RIGHT, "Q:",
				Color.BLACK,
				"",
				Color.BLACK,
				false );
		this.uiInstance = uiInstance;
		this.setOpaque( false );
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	private void passChangeToInstanceData( final float value )
	{
//		log.debug("Passing change to instance data: " + value );
		// float valueToPass = value / 1000.0f;
		// float valueToPass = value / 10.0f;
		final float valueToPass = value;
		uiInstance.sendBandwidthChange( valueToPass );
	}

	@Override
	public void doDisplayProcessing(final ThreadSpecificTemporaryEventStorage tempEventStorage,
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
		return Float.toString(model.getValue());
	}

	@Override
	public void receiveControlValue( final String value )
	{
		try
		{
//			log.debug("Received control value " + value );
			final float asFloat = Float.parseFloat( value );
			model.setValue( this, asFloat );
			receiveValueChange( this, asFloat );
		}
		catch( final Exception e )
		{
			final String msg = "Failed to parse control value: " + value;
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
}
