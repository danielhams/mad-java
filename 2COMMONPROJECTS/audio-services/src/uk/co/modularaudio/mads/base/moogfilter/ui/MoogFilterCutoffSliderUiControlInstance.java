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

package uk.co.modularaudio.mads.base.moogfilter.ui;

import java.awt.Color;

import javax.swing.JComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.interptester.ui.ValueSlider;
import uk.co.modularaudio.mads.base.moogfilter.mu.MoogFilterMadDefinition;
import uk.co.modularaudio.mads.base.moogfilter.mu.MoogFilterMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.math.MathFormatter;
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDisplayView.DisplayOrientation;
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDisplayView.SatelliteOrientation;

public class MoogFilterCutoffSliderUiControlInstance extends ValueSlider
	implements IMadUiControlInstance<MoogFilterMadDefinition, MoogFilterMadInstance, MoogFilterMadUiInstance>
{
	private static Log log = LogFactory.getLog( MoogFilterCutoffSliderUiControlInstance.class.getName() );

	private static final long serialVersionUID = 6068897521037173787L;

	private final MoogFilterMadUiInstance uiInstance;

	public MoogFilterCutoffSliderUiControlInstance(
			final MoogFilterMadDefinition definition,
			final MoogFilterMadInstance instance,
			final MoogFilterMadUiInstance uiInstance,
			final int controlIndex )
	{
		super( uiInstance.getCutoffSliderModel(),
				SatelliteOrientation.LEFT,
				DisplayOrientation.HORIZONTAL,
				SatelliteOrientation.RIGHT,
				"Cutoff:",
				Color.BLACK,
				Color.black,
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
		final float valueToPass = value;
		uiInstance.sendCutoffChange( valueToPass );
	}

	@Override
	public void doDisplayProcessing(final ThreadSpecificTemporaryEventStorage tempEventStorage,
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
		return MathFormatter.slowFloatPrint( model.getValue(), 2, false );
	}

	@Override
	public void receiveControlValue( final String value )
	{
		try
		{
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
