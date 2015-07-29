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

package uk.co.modularaudio.mads.base.specampgen.ui;

import java.awt.Color;

public class SpectralAmpColours
{
	final static Color BACKGROUND_COLOR = Color.BLACK;

//	final static Color SPECTRAL_BODY = new Color( 209, 139, 46, 255 );
//	final static Color SPECTRAL_BODY = new Color( 146, 97, 32, 255 );
	final static Color SPECTRAL_BODY = new Color( 177, 118, 39, 255 );

	final static Color RUNNING_PEAK_COLOUR = new Color( 209, 188, 46, 255 );

	final static Color SCALE_AXIS_DETAIL = RUNNING_PEAK_COLOUR.darker().darker();

}
