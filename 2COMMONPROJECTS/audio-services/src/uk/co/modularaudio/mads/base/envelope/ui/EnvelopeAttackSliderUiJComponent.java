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

package uk.co.modularaudio.mads.base.envelope.ui;

import java.awt.Color;

import javax.swing.JComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.envelope.mu.EnvelopeMadDefinition;
import uk.co.modularaudio.mads.base.envelope.mu.EnvelopeMadInstance;
import uk.co.modularaudio.mads.base.envelope.mu.EnvelopeDefaults;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacADSRSlider;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDisplayView.DisplayOrientation;
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDisplayView.SatelliteOrientation;

public class EnvelopeAttackSliderUiJComponent extends PacADSRSlider
	implements IMadUiControlInstance<EnvelopeMadDefinition, EnvelopeMadInstance, EnvelopeMadUiInstance>,
		TimescaleChangeListener, EnvelopeValueProducer
{
	private static Log log = LogFactory.getLog( EnvelopeAttackSliderUiJComponent.class.getName() );

	private static final long serialVersionUID = 7923855236169668204L;

	private float lastDisplayValue = -1.0f;

	private final EnvelopeMadUiInstance uiInstance;

	public EnvelopeAttackSliderUiJComponent( final EnvelopeMadDefinition definition,
			final EnvelopeMadInstance instance,
			final EnvelopeMadUiInstance uiInstance,
			final int controlIndex )
	{
		super( 0.0f, 10.0f, 4.0f, 4.0f,
				"ms",
				SatelliteOrientation.LEFT,
				DisplayOrientation.VERTICAL,
				SatelliteOrientation.BELOW,
				"A:",
				Color.WHITE,
				Color.WHITE,
				false );
//		this.setBackground( Color.GREEN );
		this.uiInstance = uiInstance;
		uiInstance.addTimescaleChangeListener( this );
		uiInstance.addEnvelopeProducer( this );

		model.setValue( EnvelopeDefaults.class, EnvelopeDefaults.ATTACK_MILLIS );
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	private void passChangeToInstanceData( final float newValue )
	{
		uiInstance.setAttackMillis( newValue );
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
		uiInstance.removeTimescaleChangeListener( this );
		uiInstance.removeEnvelopeProducer( this );
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
			if( log.isDebugEnabled() )
			{
				log.debug("Received control value " + valueStr );
			}
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
		if( newValue != lastDisplayValue )
		{
			lastDisplayValue = newValue;
			passChangeToInstanceData( newValue );
		}
	}

	@Override
	public void receiveTimescaleChange( final float newTimescaleUpperLimit )
	{
		model.setMaxValue( newTimescaleUpperLimit );
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}
}
