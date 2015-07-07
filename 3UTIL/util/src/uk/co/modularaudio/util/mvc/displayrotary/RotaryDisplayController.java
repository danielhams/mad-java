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

package uk.co.modularaudio.util.mvc.displayrotary;


public class RotaryDisplayController
{
//	private static Log log = LogFactory.getLog( RotaryDisplayController.class.getName() );

	private RotaryDisplayModel sdm;

	public RotaryDisplayController( final RotaryDisplayModel sdm )
	{
		this.sdm = sdm;
	}

	public void setValue( final Object source, final float newFloatValue )
	{
//		log.debug("Controller received setValue from " + source.getClass().getSimpleName() );
		sdm.setValue( source, newFloatValue );
	}

	public void changeModel( final RotaryDisplayModel newModel )
	{
		this.sdm = newModel;
	}

	public RotaryDisplayModel getModel()
	{
		return sdm;
	}

	public void moveByMajorTick( final Object source, final int direction )
	{
		sdm.moveByMajorTick( source, direction );
	}

	public void moveByMinorTick( final Object source, final int direction )
	{
		sdm.moveByMinorTick( source, direction );
	}
}
