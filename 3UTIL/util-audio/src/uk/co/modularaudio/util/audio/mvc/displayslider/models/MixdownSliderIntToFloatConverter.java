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

package uk.co.modularaudio.util.audio.mvc.displayslider.models;

public class MixdownSliderIntToFloatConverter extends LogarithmicDbFaderIntToFloatConverter
{
//	private static Log log = LogFactory.getLog( MixdownSliderIntToFloatConverter.class.getName() );

	public final static float DEFAULT_LIN_HIGHEST_DB = 10.0f;
	public final static float DEFAULT_LIN_LOWEST_DB = -30.0f;
	public final static float DEFAULT_COM_HIGHEST_DB = -30.0f;
	public final static float DEFAULT_COM_LOWEST_DB = -90.0f;

	public MixdownSliderIntToFloatConverter()
	{
		super( DEFAULT_LIN_HIGHEST_DB,
				DEFAULT_LIN_LOWEST_DB,
				DEFAULT_COM_HIGHEST_DB,
				DEFAULT_COM_LOWEST_DB );
	}
}
