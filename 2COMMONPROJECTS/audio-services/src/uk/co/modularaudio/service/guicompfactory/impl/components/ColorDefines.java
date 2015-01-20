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

package uk.co.modularaudio.service.guicompfactory.impl.components;

import java.awt.Color;

public class ColorDefines
{

	public static final Color CONTENTS_COLOR = new Color( 0.3f, 0.3f, 0.3f );
//	public static final Color CONTENTS_COLOR = new Color( 0.6f, 0.6f, 0.6f );
	public static final Color HIGHLIGHT_COLOR;
	public static final Color LOWLIGHT_COLOR;
	
	static
	{
		HIGHLIGHT_COLOR = CONTENTS_COLOR.brighter();
		LOWLIGHT_COLOR = CONTENTS_COLOR.darker();
	}

}
