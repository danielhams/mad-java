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

package uk.co.modularaudio.util.audio.gui.mad.rack;

import java.awt.Dimension;

public class GuiConstants
{
	private static final int DEFAULT_WINDOW_WIDTH = 1250;
	private static final int DEFAULT_WINDOW_HEIGHT = 750;
	private static final int MINIMUM_WINDOW_HEIGHT = 400;

	// How big should the initial window be?
	public final static Dimension GUI_DEFAULT_DIMENSIONS;
	public final static Dimension GUI_MINIMUM_DIMENSIONS;

	// How wide the rack is in total
	public final static int GUI_RACK_WIDTH = 610;

	// How wide the rack edge are - there are two edges of course, so the rack still sticks out INSET amount each side.
	public final static int GUI_RACK_FRAME_WIDTH = 20;

	// How much in from the rack edge does a unit actually begin? This is where we paint the edges of the component
	// the drag handles, in fact.
	public final static int GUI_UNIT_RACK_FRAME_INSET = 5;
	// Default height. Will see if this need deeper thought later.
	public final static int GUI_UNIT_HEIGHT = 100;
	// A fatboy spans the entire width of the rack - but must still leave enough space for the inset on either side.
	public final static int GUI_UNIT_FATBOY_WIDTH = GUI_RACK_WIDTH - ( 2 * GUI_UNIT_RACK_FRAME_INSET );
	// The "simple" (hahaha a Pascal Jost joke after all these years) width is half the full width minus the necessary space for two insets
	public final static int GUI_UNIT_SIMPLE_WIDTH = (GUI_RACK_WIDTH - ( 4 * GUI_UNIT_RACK_FRAME_INSET )) / 2;
	// How big the "handles" on the sides of a component should be for drag and drop unit re-ordering
	public final static int GUI_UNIT_DRAG_DROP_HANDLE_WIDTH = 10;

	// Preferences dialog
	public static final int DEFAULT_PREFS_WIDTH = 850;
	public static final int DEFAULT_PREFS_HEIGHT = 350;
	public static final int MIN_PREFS_WIDTH = 850;
	public static final int MIN_PREFS_HEIGHT = 350;

	public static final Dimension GUI_PREFERENCES_DEFAULT_DIMENSIONS;
	public static final Dimension GUI_PREFERENCES_MINIMUM_DIMENSIONS;

	// Some GUI messages
	public static final String DIALOG_UNABLE_TO_PERFORM_TITLE = "Warning";

	static
	{
		GUI_DEFAULT_DIMENSIONS = new Dimension( DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
		GUI_MINIMUM_DIMENSIONS = new Dimension( DEFAULT_WINDOW_WIDTH, MINIMUM_WINDOW_HEIGHT);

		GUI_PREFERENCES_DEFAULT_DIMENSIONS = new Dimension( DEFAULT_PREFS_WIDTH, DEFAULT_PREFS_HEIGHT );
		GUI_PREFERENCES_MINIMUM_DIMENSIONS = new Dimension( MIN_PREFS_WIDTH, MIN_PREFS_HEIGHT );
	}
}
