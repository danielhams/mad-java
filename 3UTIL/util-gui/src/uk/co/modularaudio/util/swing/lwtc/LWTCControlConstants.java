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

package uk.co.modularaudio.util.swing.lwtc;

import java.awt.Color;
import java.awt.Font;

import uk.co.modularaudio.util.swing.lwtc.AbstractLWTCButton.MadButtonState;

public class LWTCControlConstants
{
	public final static Color CONTROL_OUTLINE = Color.decode( "#000000" ); // Black
	public final static Color CONTROL_FLAT_BACKGROUND = Color.decode( "#393f3f" );  // Dark grey

	public final static Color CONTROL_BUTTON_OUT_GRAD_START = Color.decode( "#444a4a" );
	public final static Color CONTROL_BUTTON_OUT_GRAD_END = Color.decode( "#2d3232" );
	public final static Color CONTROL_BUTTON_OUT_HILIGHT = Color.decode( "#575d5d" );

	public final static Color CONTROL_BUTTON_IN_GRAD_START = Color.decode( "#444a4a" ).darker();
	public final static Color CONTROL_BUTTON_IN_GRAD_END = Color.decode( "#2d3232" ).darker();
	public final static Color CONTROL_BUTTON_IN_HILIGHT = Color.decode( "#575d5d" ).darker();

	public final static Color CONTROL_FOREGROUND_TEXT_UNSELECTED = Color.decode( "#ffffff" );
	public final static Color CONTROL_FOREGROUND_TEXT_SELECTED = Color.decode( "#ff0000" );

	public final static Color CONTROL_FOCUS = CONTROL_FLAT_BACKGROUND.brighter();

	public final static Color CONTROL_LABEL_BACKGROUND = Color.decode( "#393f3f" );
	public final static Color CONTROL_LABEL_FOREGROUND = Color.decode( "#ffffff" );
	public final static Color CONTROL_LABEL_BORDER = Color.decode( "#000000" );

	public final static Color CONTROL_SLIDER_OUTLINE = Color.decode( "#1b1c1c" );
	public final static Color CONTROL_SLIDER_BACK_GRAD_START = Color.decode( "#414848" );
	public final static Color CONTROL_SLIDER_BACK_GRAD_END = Color.decode( "#3b4242" );
	public final static Color CONTROL_SLIDER_BARREL_GRAD_START = Color.decode( "#363b3b" );
	public final static Color CONTROL_SLIDER_BARREL_GRAD_END = Color.decode( "#313636" );
	public final static Color CONTROL_SLIDER_DIAL_GRAD_START = Color.decode( "#404747" );
	public final static Color CONTROL_SLIDER_DIAL_GRAD_END = Color.decode( "#343a3a" );
	public final static Color CONTROL_SLIDER_DIMPLE_DARK = Color.decode( "#2c2f2f" );
	public final static Color CONTROL_SLIDER_DIMPLE_LIGHT = Color.decode( "#3a4141" );
	public final static Color CONTROL_SLIDER_INDICATOR = Color.decode( "#00ff00" );

	public final static Color CONTROL_SLIDER_VALLEY_PERIMETER = Color.decode( "#282b2b" );
	public final static Color CONTROL_SLIDER_VALLEY_PLAIN = Color.decode( "#323737" );

	public final static Color CONTROL_SLIDER_SIDE_SHADE = Color.decode( "#353b3b" );
	public final static Color CONTROL_SLIDER_SIDE_LIGHT = Color.decode( "#3c4343" );

	public final static Color CONTROL_SLIDER_FOCUS = Color.decode( "#888888" );

	public final static Color CONTROL_TEXTBOX_BACKGROUND = Color.decode( "#bababa");
	public final static Color CONTROL_TEXTBOX_FOREGROUND = Color.decode( "#393f3f");
	public final static Color CONTROL_TEXTBOX_SELECTION = CONTROL_TEXTBOX_FOREGROUND;
	public final static Color CONTROL_TEXTBOX_SELECTED_TEXT = CONTROL_TEXTBOX_BACKGROUND;

	public final static Font RACK_FONT = getRackFont();
	public final static Font LABEL_FONT = getLabelFont();
	public final static Font LABEL_SMALL_FONT = getLabelSmallFont();

	public final static Font getRackFont()
	{
		return new Font( "SansSerif", Font.PLAIN, 11 );
	}

	public final static Font getLabelFont()
	{
		return new Font( "SansSerif", Font.PLAIN, 11 );
	}

	public final static Font getLabelSmallFont()
	{
		return new Font( "Dialog", Font.PLAIN, 9 );
	}

	public final static LWTCButtonColours STD_BUTTON_COLOURS = new StdButtonColours();
	public final static LWTCButtonColours STD_TOGGLE_BUTTON_COLOURS = new StdToggleButtonColours();

	public final static LWTCLabelColours STD_LABEL_COLOURS = new StdLabelColours();

	public final static LWTCSliderColours STD_SLIDER_COLOURS = new StdSliderColours();
	public final static LWTCTextFieldColours STD_TEXTFIELD_COLOURS = new StdTextfieldColours();

	public static class StdTextfieldColours implements LWTCTextFieldColours
	{

		@Override
		public Color getBackground()
		{
			return CONTROL_TEXTBOX_BACKGROUND;
		}

		@Override
		public Color getForeground()
		{
			return CONTROL_TEXTBOX_FOREGROUND;
		}
	};

	public static class StdSliderColours implements LWTCSliderColours
	{

		@Override
		public Color getControlOutline()
		{
			return CONTROL_SLIDER_OUTLINE;
		}

		@Override
		public Color getBackgroundGradStart()
		{
			return CONTROL_SLIDER_BACK_GRAD_START;
		}

		@Override
		public Color getBackgroundGradEnd()
		{
			return CONTROL_SLIDER_BACK_GRAD_END;
		}

		@Override
		public Color getBarrelGradStart()
		{
			return CONTROL_SLIDER_BARREL_GRAD_START;
		}

		@Override
		public Color getBarrelGradEnd()
		{
			return CONTROL_SLIDER_BARREL_GRAD_END;
		}

		@Override
		public Color getDialGradStart()
		{
			return CONTROL_SLIDER_DIAL_GRAD_START;
		}

		@Override
		public Color getDialGradEnd()
		{
			return CONTROL_SLIDER_DIAL_GRAD_END;
		}

		@Override
		public Color getDimpleDark()
		{
			return CONTROL_SLIDER_DIMPLE_DARK;
		}

		@Override
		public Color getDimpleLight()
		{
			return CONTROL_SLIDER_DIMPLE_LIGHT;
		}

		@Override
		public Color getValleyPerimeter()
		{
			return CONTROL_SLIDER_VALLEY_PERIMETER;
		}

		@Override
		public Color getValleyPlain()
		{
			return CONTROL_SLIDER_VALLEY_PLAIN;
		}

		@Override
		public Color getSideShade()
		{
			return CONTROL_SLIDER_SIDE_SHADE;
		}

		@Override
		public Color getSideLight()
		{
			return CONTROL_SLIDER_SIDE_LIGHT;
		}

		@Override
		public Color getFocus()
		{
			return CONTROL_SLIDER_FOCUS;
		}

		@Override
		public Color getIndicatorColor()
		{
			return CONTROL_SLIDER_INDICATOR;
		}
	};

	public static class StdLabelColours implements LWTCLabelColours
	{

		@Override
		public Color getBackground()
		{
			return CONTROL_LABEL_BACKGROUND;
		}

		@Override
		public Color getForeground()
		{
			return CONTROL_LABEL_FOREGROUND;
		}

		@Override
		public Color getBorder()
		{
			return CONTROL_LABEL_BORDER;
		}
	};

	public static class StdButtonColours implements LWTCButtonColours
	{
		private final LWTCButtonStateColours[] stateToColoursMap;

		public StdButtonColours()
		{
			stateToColoursMap = new LWTCButtonStateColours[ MadButtonState.values().length ];

			stateToColoursMap[ MadButtonState.OUT_NO_MOUSE.ordinal() ] = new LWTCButtonStateColours()
			{

				@Override
				public Color getHighlight()
				{
					return CONTROL_BUTTON_OUT_HILIGHT;
				}

				@Override
				public Color getForegroundText()
				{
					return CONTROL_FOREGROUND_TEXT_UNSELECTED;
				}

				@Override
				public Color getFocus()
				{
					return CONTROL_FOCUS;
				}

				@Override
				public Color getControlOutline()
				{
					return CONTROL_OUTLINE;
				}

				@Override
				public Color getContentGradStart()
				{
					return CONTROL_BUTTON_OUT_GRAD_START;
				}

				@Override
				public Color getContentGradEnd()
				{
					return CONTROL_BUTTON_OUT_GRAD_END;
				}
			};
			stateToColoursMap[ MadButtonState.OUT_MOUSE.ordinal() ] = new LWTCButtonStateColours()
			{

				@Override
				public Color getHighlight()
				{
					return CONTROL_BUTTON_OUT_HILIGHT.brighter();
				}

				@Override
				public Color getForegroundText()
				{
					return CONTROL_FOREGROUND_TEXT_UNSELECTED;
				}

				@Override
				public Color getFocus()
				{
					return CONTROL_FOCUS;
				}

				@Override
				public Color getControlOutline()
				{
					return CONTROL_OUTLINE;
				}

				@Override
				public Color getContentGradStart()
				{
					return CONTROL_BUTTON_OUT_GRAD_START.brighter();
				}

				@Override
				public Color getContentGradEnd()
				{
					return CONTROL_BUTTON_OUT_GRAD_END.brighter();
				}
			};
			stateToColoursMap[ MadButtonState.IN_MOUSE.ordinal() ] = new LWTCButtonStateColours()
			{

				@Override
				public Color getHighlight()
				{
					return CONTROL_BUTTON_IN_HILIGHT;
				}

				@Override
				public Color getForegroundText()
				{
					return CONTROL_FOREGROUND_TEXT_SELECTED;
				}

				@Override
				public Color getFocus()
				{
					return CONTROL_FOCUS;
				}

				@Override
				public Color getControlOutline()
				{
					return CONTROL_OUTLINE;
				}

				@Override
				public Color getContentGradStart()
				{
					return CONTROL_BUTTON_IN_GRAD_START;
				}

				@Override
				public Color getContentGradEnd()
				{
					return CONTROL_BUTTON_IN_GRAD_END;
				}
			};
			stateToColoursMap[ MadButtonState.IN_NO_MOUSE.ordinal() ] = new LWTCButtonStateColours()
			{

				@Override
				public Color getHighlight()
				{
					return CONTROL_BUTTON_IN_HILIGHT.brighter();
				}

				@Override
				public Color getForegroundText()
				{
					return CONTROL_FOREGROUND_TEXT_SELECTED;
				}

				@Override
				public Color getFocus()
				{
					return CONTROL_FOCUS;
				}

				@Override
				public Color getControlOutline()
				{
					return CONTROL_OUTLINE;
				}

				@Override
				public Color getContentGradStart()
				{
					return CONTROL_BUTTON_IN_GRAD_START.brighter();
				}

				@Override
				public Color getContentGradEnd()
				{
					return CONTROL_BUTTON_IN_GRAD_END.brighter();
				}
			};
		}

		@Override
		public LWTCButtonStateColours getButtonColoursForState( final MadButtonState state )
		{
			return stateToColoursMap[ state.ordinal() ];
		}
	};

	public static class StdToggleButtonColours implements LWTCButtonColours
	{
		private final LWTCButtonStateColours[] stateToColoursMap;

		public StdToggleButtonColours()
		{
			stateToColoursMap = new LWTCButtonStateColours[ MadButtonState.values().length ];

			stateToColoursMap[ MadButtonState.OUT_NO_MOUSE.ordinal() ] = new LWTCButtonStateColours()
			{

				@Override
				public Color getHighlight()
				{
					return CONTROL_BUTTON_OUT_HILIGHT;
				}

				@Override
				public Color getForegroundText()
				{
					return CONTROL_FOREGROUND_TEXT_UNSELECTED;
				}

				@Override
				public Color getFocus()
				{
					return CONTROL_FOCUS;
				}

				@Override
				public Color getControlOutline()
				{
					return CONTROL_OUTLINE;
				}

				@Override
				public Color getContentGradStart()
				{
					return CONTROL_BUTTON_OUT_GRAD_START;
				}

				@Override
				public Color getContentGradEnd()
				{
					return CONTROL_BUTTON_OUT_GRAD_END;
				}
			};
			stateToColoursMap[ MadButtonState.OUT_MOUSE.ordinal() ] = new LWTCButtonStateColours()
			{

				@Override
				public Color getHighlight()
				{
					return CONTROL_BUTTON_OUT_HILIGHT.brighter();
				}

				@Override
				public Color getForegroundText()
				{
					return CONTROL_FOREGROUND_TEXT_UNSELECTED;
				}

				@Override
				public Color getFocus()
				{
					return CONTROL_FOCUS;
				}

				@Override
				public Color getControlOutline()
				{
					return CONTROL_OUTLINE;
				}

				@Override
				public Color getContentGradStart()
				{
					return CONTROL_BUTTON_OUT_GRAD_START.brighter();
				}

				@Override
				public Color getContentGradEnd()
				{
					return CONTROL_BUTTON_OUT_GRAD_END.brighter();
				}
			};
			stateToColoursMap[ MadButtonState.IN_MOUSE.ordinal() ] = new LWTCButtonStateColours()
			{

				@Override
				public Color getHighlight()
				{
					return CONTROL_BUTTON_IN_HILIGHT;
				}

				@Override
				public Color getForegroundText()
				{
					return CONTROL_FOREGROUND_TEXT_SELECTED;
				}

				@Override
				public Color getFocus()
				{
					return CONTROL_FOCUS;
				}

				@Override
				public Color getControlOutline()
				{
					return CONTROL_OUTLINE;
				}

				@Override
				public Color getContentGradStart()
				{
					return CONTROL_FLAT_BACKGROUND;
				}

				@Override
				public Color getContentGradEnd()
				{
					return CONTROL_FLAT_BACKGROUND;
				}
			};
			stateToColoursMap[ MadButtonState.IN_NO_MOUSE.ordinal() ] = new LWTCButtonStateColours()
			{

				@Override
				public Color getHighlight()
				{
					return CONTROL_BUTTON_IN_HILIGHT.brighter();
				}

				@Override
				public Color getForegroundText()
				{
					return CONTROL_FOREGROUND_TEXT_SELECTED;
				}

				@Override
				public Color getFocus()
				{
					return CONTROL_FOCUS;
				}

				@Override
				public Color getControlOutline()
				{
					return CONTROL_OUTLINE;
				}

				@Override
				public Color getContentGradStart()
				{
					return CONTROL_FLAT_BACKGROUND;
				}

				@Override
				public Color getContentGradEnd()
				{
					return CONTROL_FLAT_BACKGROUND;
				}
			};
		}

		@Override
		public LWTCButtonStateColours getButtonColoursForState( final MadButtonState state )
		{
			return stateToColoursMap[ state.ordinal() ];
		}
	};
}
