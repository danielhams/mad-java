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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;

public class PaintedComponentDefines
{

	public static final Color CONTENTS_COLOR = new Color( 0.2f, 0.2f, 0.2f );
//	public static final Color CONTENTS_COLOR = new Color( 0.3f, 0.3f, 0.3f );
//	public static final Color CONTENTS_COLOR = new Color( 0.6f, 0.6f, 0.6f );
	public static final Color HIGHLIGHT_COLOR;
	public static final Color LOWLIGHT_COLOR;

	public final static int DRAG_BAR_WIDTH = 20;
	public final static int INSET = 3;
	public final static float ARC = 10;

	public final static int HORIZON_INSET = 20;

//	public final static float EMPTY_COMPONENT_GREY_LEVEL = 0.6f;
	public final static float EMPTY_COMPONENT_GREY_LEVEL = 0.35f;
//	public final static float EMPTY_COMPONENT_GREY_LEVEL = 0.2f;
	public static final Color BLANK_FRONT_COLOR = new Color( EMPTY_COMPONENT_GREY_LEVEL, EMPTY_COMPONENT_GREY_LEVEL, EMPTY_COMPONENT_GREY_LEVEL );

	public static final Color BLANK_BACK_COLOR = new Color( EMPTY_COMPONENT_GREY_LEVEL, EMPTY_COMPONENT_GREY_LEVEL, EMPTY_COMPONENT_GREY_LEVEL );

	public static final boolean DRAWING_DEBUG = true;

	public static final Composite OPAQUE_COMPOSITE = AlphaComposite.getInstance( AlphaComposite.SRC );
	public static final Composite ERASE_COMPOSITE = AlphaComposite.getInstance( AlphaComposite.CLEAR );


	static
	{
		HIGHLIGHT_COLOR = CONTENTS_COLOR.brighter();
		LOWLIGHT_COLOR = CONTENTS_COLOR.darker();
	}

}
