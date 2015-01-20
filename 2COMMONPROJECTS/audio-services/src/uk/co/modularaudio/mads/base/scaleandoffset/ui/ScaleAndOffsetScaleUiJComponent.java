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

package uk.co.modularaudio.mads.base.scaleandoffset.ui;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;

import uk.co.modularaudio.mads.base.scaleandoffset.mu.ScaleAndOffsetMadDefinition;
import uk.co.modularaudio.mads.base.scaleandoffset.mu.ScaleAndOffsetMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.paccontrols.PacFloatSlider;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class ScaleAndOffsetScaleUiJComponent extends PacFloatSlider
	implements IMadUiControlInstance<ScaleAndOffsetMadDefinition, ScaleAndOffsetMadInstance, ScaleAndOffsetMadUiInstance>
{
	private static final long serialVersionUID = 2730117917575342829L;
	
	private ScaleAndOffsetMadUiInstance uiInstance = null;

	public ScaleAndOffsetScaleUiJComponent( ScaleAndOffsetMadDefinition definition,
			ScaleAndOffsetMadInstance instance,
			ScaleAndOffsetMadUiInstance uiInstance,
			int controlIndex )
	{
		super( uiInstance.scaleFloatSliderModel );

		this.uiInstance = uiInstance;
		this.setOpaque( false );
		setFont( this.getFont().deriveFont( 9f ) );
//		this.setPaintLabels( true );
//		this.setMinimum( 1 );
//		this.setMaximum( 20000 );
		// Default value
		this.setValue( 0 );
		this.setValue( ScaleAndOffsetMadUiInstance.START_VAL );
	}

	public JComponent getControl()
	{
		return this;
	}

	public void stateChanged( ChangeEvent e )
	{
		this.passChangeToInstanceData( this.getValue() );
	}

	private void passChangeToInstanceData( double value )
	{
		// Convert it into a float
		float floatValue = (float)value;
		uiInstance.sendScaleChange( floatValue );
	}

	@Override
	public void doDisplayProcessing(ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		// log.debug("Received display tick");
	}

	@Override
	public void receiveValueUpdate( double previousValue, double newValue )
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
