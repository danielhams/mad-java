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

package uk.co.modularaudio.util.pooling.common;

/**
 * <P>Allows a factory to indicate that errors occured creating a resource.</P>
 * @author dan
 * @version 1.0
 * @see uk.co.modularaudio.util.pooling.common.Factory
 */
public class FactoryProductionException extends Exception
{
	private static final long serialVersionUID = -5154825899516537598L;

	public FactoryProductionException(String error)
	{
		this.errStr = error;
	}

	@Override
	public String toString()
	{
		return("FactoryProductionException: " + errStr);
	}

	private String errStr = "";
}


