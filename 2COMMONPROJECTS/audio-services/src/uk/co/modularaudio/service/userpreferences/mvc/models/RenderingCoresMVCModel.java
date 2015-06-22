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

package uk.co.modularaudio.service.userpreferences.mvc.models;

import javax.swing.SpinnerNumberModel;

public class RenderingCoresMVCModel extends SpinnerNumberModel
{
	private static final long serialVersionUID = 2267418391790286214L;

	public RenderingCoresMVCModel( final int maxCores )
	{
		super( 1, 1, maxCores-1, 1 );
	}

	public int getIntValue()
	{
		return (Integer)(super.getValue());
	}

	public void setIntValue( final int value )
	{
		super.setValue( value );
	}

}
