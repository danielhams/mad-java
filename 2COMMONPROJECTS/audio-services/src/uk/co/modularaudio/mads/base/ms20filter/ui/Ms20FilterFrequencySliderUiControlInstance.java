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

package uk.co.modularaudio.mads.base.ms20filter.ui;

import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.ms20filter.mu.Ms20FilterMadDefinition;
import uk.co.modularaudio.mads.base.ms20filter.mu.Ms20FilterMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacSlider;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.math.NormalisedValuesMapper;

public class Ms20FilterFrequencySliderUiControlInstance extends PacSlider
	implements IMadUiControlInstance<Ms20FilterMadDefinition, Ms20FilterMadInstance, Ms20FilterMadUiInstance>
{
	private static final long serialVersionUID = 6068897521037173787L;

	private Ms20FilterMadUiInstance uiInstance = null;

	public Ms20FilterFrequencySliderUiControlInstance(
			Ms20FilterMadDefinition definition,
			Ms20FilterMadInstance instance,
			Ms20FilterMadUiInstance uiInstance,
			int controlIndex )
	{
		this.uiInstance = uiInstance;
		this.setOpaque( false );
		setFont( this.getFont().deriveFont( 9f ) );
		this.setPaintLabels( true );
		this.setMinimum( 0 );
		this.setMaximum( 1000 );
		// Default value
		this.setValue( 1000 );
	}

	public JComponent getControl()
	{
		return this;
	}

	private void passChangeToInstanceData( int value )
	{
		float valueToPass = value / 1000.0f;
		// Now scale it exponentially
//		float scaleExpValue = (float)(Math.pow( 10, (valueToPass - 1) ) * valueToPass );
		float scaleExpValue = NormalisedValuesMapper.expMinMaxMapF( valueToPass, 60.0f, 20000.0f);

		// And make it a frequency
		float newFreq = scaleExpValue * 22100.0f;
//		float newFreq = scaleExpValue;
		uiInstance.sendFrequencyChange( newFreq );
	}

	@Override
	public void doDisplayProcessing(ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		// log.debug("Received display tick");
	}

	@Override
	public void processValueChange( int previousValue, int newValue )
	{
		if( previousValue != newValue )
		{
			passChangeToInstanceData( newValue );
		}
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}
}
