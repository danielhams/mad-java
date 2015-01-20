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

package uk.co.modularaudio.util.audio.gui.paccontrols;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.co.modularaudio.util.swing.general.FloatJSlider;
import uk.co.modularaudio.util.swing.general.FloatJSliderModel;

public abstract class PacFloatSlider extends FloatJSlider implements ChangeListener
{
	private static final long serialVersionUID = -7086322527151036644L;
	
	private double previousValue = 0.0;

	public PacFloatSlider(FloatJSliderModel model)
	{
		super(model);
		this.addChangeListener( this );
	}
	
	public String getControlValue()
	{
		double valueAsDouble = getValue();
		return Double.toString( valueAsDouble );
	}
	
	public void receiveControlValue( String strValue )
	{
		double value = Double.parseDouble( strValue );
		setValue( value );
	}

	@Override
	public void stateChanged( ChangeEvent e )
	{
		if( e.getSource() == this )
		{
			double newValue = getValue();
			receiveValueUpdate( previousValue, newValue );
			previousValue = newValue;
		}
	}
	
	public abstract void receiveValueUpdate( double previousValue, double newValue );

}
