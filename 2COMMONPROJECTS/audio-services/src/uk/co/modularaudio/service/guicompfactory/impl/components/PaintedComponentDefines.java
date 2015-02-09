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

//	public final static int DRAG_BAR_WIDTH = 17;
	public final static int DRAG_BAR_WIDTH = 20;
//	public final static int DRAG_BAR_WIDTH = 22;
	public final static int INSET = 3;
	public final static float OUTSIDE_BORDER_ARC = 10;

//	public final static float EMPTY_COMPONENT_GREY_LEVEL = 0.6f;
	public final static float EMPTY_COMPONENT_GREY_LEVEL = 0.35f;
//	public final static float EMPTY_COMPONENT_GREY_LEVEL = 0.2f;
	public static final Color BLANK_FRONT_COLOR = new Color( EMPTY_COMPONENT_GREY_LEVEL, EMPTY_COMPONENT_GREY_LEVEL, EMPTY_COMPONENT_GREY_LEVEL );

	public static final Color BLANK_BACK_COLOR = new Color( EMPTY_COMPONENT_GREY_LEVEL, EMPTY_COMPONENT_GREY_LEVEL, EMPTY_COMPONENT_GREY_LEVEL );

	public static final boolean DRAWING_DEBUG = false;

	public static final Composite OPAQUE_COMPOSITE = AlphaComposite.getInstance( AlphaComposite.SRC );
	public static final Composite SRCOVER_COMPOSITE = AlphaComposite.getInstance( AlphaComposite.SRC_OVER );
	public static final Composite ERASE_COMPOSITE = AlphaComposite.getInstance( AlphaComposite.CLEAR );


	static
	{
		HIGHLIGHT_COLOR = CONTENTS_COLOR.brighter();
		LOWLIGHT_COLOR = CONTENTS_COLOR.darker();
	}

	public static final int HOLE_BASE_RADIUS = 2;
	public static final int HOLE_SURROUND_RADIUS_X = 2;
	public static final int HOLE_SURROUND_RADIUS_Y = 1;
	public static final int HOLE_POS_Y_OFFSET = 12;
	public static final int HOLE_POS_X_OFFSET = 7;

	// Plus one for the bar we will stretch the whole width/height as a "surround"
	public final static int FRONT_MIN_WIDTH = DRAG_BAR_WIDTH * 2 + 1;
	public final static int FRONT_MIN_HEIGHT = ((HOLE_POS_Y_OFFSET * 2) * 2) + 1;
	public final static int FRONT_ONE_CORNER_HEIGHT = HOLE_POS_Y_OFFSET * 2;
	public final static int FRONT_ONE_CORNER_WIDTH = DRAG_BAR_WIDTH;
	public final static int FRONT_BOTTOM_TOP_INSET = 4;

	public final static int BACK_INSET_WIDTH = 5;
	public final static int BACK_MIN_WIDTH = BACK_INSET_WIDTH * 2 + 1;
	public final static int BACK_MIN_HEIGHT = BACK_INSET_WIDTH * 2 + 1;
	public final static int BACK_ONE_CORNER_HEIGHT = BACK_INSET_WIDTH;
	public final static int BACK_ONE_CORNER_WIDTH = BACK_INSET_WIDTH;
	public final static int BACK_BOTTOM_TOP_INSET = 4;

}
