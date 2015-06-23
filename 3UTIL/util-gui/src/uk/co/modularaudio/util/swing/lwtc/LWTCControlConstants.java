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
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderViewColors;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryViewColors;

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

	public final static Color CONTROL_FOCUS = Color.decode( "#888888" );

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

	public static final Color CONTROL_ROTCHO_OUTLINE = Color.decode("#393f3f");
	public static final Color CONTROL_ROTCHO_CHOICE_BACKGROUND = Color.decode("#292929");
	public static final Color CONTROL_ROTCHO_INNER_OUTLINE = Color.decode("#1d1d1d");
	public static final Color CONTROL_ROTCHO_HIGHLIGHT = Color.decode("#575d5d");
	public static final Color CONTROL_ROTCHO_TOP_SHADOW = Color.decode("#2e3333");
	public static final Color CONTROL_ROTCHO_BOTTOM_SHADOW = Color.decode("#272b2b");
	public static final Color CONTROL_ROTCHO_FOCUS = Color.decode("#888888");
	public static final Color CONTROL_ROTCHO_TEXT_FOREGROUND = Color.decode("#ffffff");
	public static final Color CONTROL_ROTCHO_GRAD_START = CONTROL_BUTTON_OUT_GRAD_START;
	public static final Color CONTROL_ROTCHO_GRAD_END = CONTROL_BUTTON_OUT_GRAD_END;
	public static final Color CONTROL_ROTCHO_FLECHE_ACTIVE = Color.decode( "#cfcfcf" );
	public static final Color CONTROL_ROTCHO_FLECHE_INACTIVE = Color.decode( "#575d5d" );
	public static final Color CONTROL_ROTCHO_FLECHE_DOWN = CONTROL_FLAT_BACKGROUND;

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
	public final static LWTCSliderColours STD_SLIDER_NOMARK_COLOURS = new StdSliderNoMarkColours();
	public final static LWTCTextFieldColours STD_TEXTFIELD_COLOURS = new StdTextfieldColours();

	public final static LWTCRotaryChoiceColours STD_ROTARY_CHOICE_COLOURS = new StdRotaryChoiceColours();

	public static class StdRotaryChoiceColours implements LWTCRotaryChoiceColours
	{

		@Override
		public Color getControlOutline()
		{
			return CONTROL_ROTCHO_OUTLINE;
		}

		@Override
		public Color getChoiceBackground()
		{
			return CONTROL_ROTCHO_CHOICE_BACKGROUND;
		}

		@Override
		public Color getInnerOutline()
		{
			return CONTROL_ROTCHO_INNER_OUTLINE;
		}

		@Override
		public Color getHighlight()
		{
			return CONTROL_ROTCHO_HIGHLIGHT;
		}

		@Override
		public Color getTopShadow()
		{
			return CONTROL_ROTCHO_TOP_SHADOW;
		}

		@Override
		public Color getBottomShadow()
		{
			return CONTROL_ROTCHO_BOTTOM_SHADOW;
		}

		@Override
		public Color getFocus()
		{
			return CONTROL_ROTCHO_FOCUS;
		}

		@Override
		public Color getForegroundText()
		{
			return CONTROL_ROTCHO_TEXT_FOREGROUND;
		}

		@Override
		public Color getContentGradStart()
		{
			return CONTROL_ROTCHO_GRAD_START;
		}

		@Override
		public Color getContentGradEnd()
		{
			return CONTROL_ROTCHO_GRAD_END;
		}

		@Override
		public Color getFlecheInactive()
		{
			return CONTROL_ROTCHO_FLECHE_INACTIVE;
		}

		@Override
		public Color getFlecheActive()
		{
			return CONTROL_ROTCHO_FLECHE_ACTIVE;
		}

		@Override
		public Color getFlecheDown()
		{
			return CONTROL_ROTCHO_FLECHE_DOWN;
		}
	};

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

	public static class StdSliderNoMarkColours extends StdSliderColours
	{
		@Override
		public Color getIndicatorColor()
		{
			return null;
		}
	}

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
	}

	private static final LWTCSliderViewColors getSliderViewColors()
	{
		final Color bgColor = Color.black;
		final Color fgColor = Color.white;
		final Color indicatorColor = null;
		final Color textboxBgColor = CONTROL_TEXTBOX_BACKGROUND;
		final Color textboxFgColor = CONTROL_TEXTBOX_FOREGROUND;
		final Color selectionColor = CONTROL_TEXTBOX_SELECTION;
		final Color selectedTextColor = CONTROL_TEXTBOX_SELECTED_TEXT;
		final Color labelColor = CONTROL_LABEL_FOREGROUND;
		final Color unitsColor = labelColor;

		return new LWTCSliderViewColors( bgColor,
				fgColor,
				indicatorColor,
				textboxBgColor,
				textboxFgColor,
				selectionColor,
				selectedTextColor,
				labelColor,
				unitsColor );
	}

	public static final LWTCSliderViewColors SLIDER_VIEW_COLORS = getSliderViewColors();;

	public final static Color ROTARY_VIEW_BACKGROUND_COLOR = new Color(72,72,72);
	public final static Color ROTARY_VIEW_FOREGROUND_COLOR = new Color(110,110,110);
	public final static Color ROTARY_VIEW_KNOB_COLOR = new Color(62,69,69);
	public final static Color ROTARY_VIEW_EXTENT_COLOR = new Color( 90, 90, 90 );
	public final static Color ROTARY_VIEW_OUTLINE_COLOR = new Color(27,29,29);
	public final static Color ROTARY_VIEW_INDICATOR_COLOR = new Color(0,255,0);
	public final static Color ROTARY_VIEW_FOCUS_COLOR = new Color(200,200,200);

	public final static RotaryViewColors STD_ROTARY_VIEW_COLORS = getRotaryViewColours();

	private final static RotaryViewColors getRotaryViewColours()
	{
		final Color bgColor = ROTARY_VIEW_BACKGROUND_COLOR;
		final Color fgColor = ROTARY_VIEW_FOREGROUND_COLOR;
		final Color textboxBgColor = CONTROL_TEXTBOX_BACKGROUND;
		final Color textboxFgColor = CONTROL_TEXTBOX_FOREGROUND;
		final Color selectionColor = CONTROL_TEXTBOX_SELECTION;
		final Color selectedTextColor = CONTROL_TEXTBOX_SELECTED_TEXT;
		final Color knobOutlineColor = ROTARY_VIEW_OUTLINE_COLOR;
		final Color knobFillColor = ROTARY_VIEW_KNOB_COLOR;
		final Color knobExtentColor = ROTARY_VIEW_EXTENT_COLOR;
		final Color knobIndicatorColor = ROTARY_VIEW_INDICATOR_COLOR;
		final Color knobFocusColor = ROTARY_VIEW_FOCUS_COLOR;
		final Color labelColor = CONTROL_LABEL_FOREGROUND;
		final Color unitsColor = labelColor;

		return new RotaryViewColors( bgColor,
				fgColor,
				textboxBgColor,
				textboxFgColor,
				selectionColor,
				selectedTextColor,
				knobOutlineColor,
				knobFillColor,
				knobExtentColor,
				knobIndicatorColor,
				knobFocusColor,
				labelColor,
				unitsColor );

	}

	public static class StdRotaryViewColor extends RotaryViewColors
	{
		public final static Color IBGCOLOR = ROTARY_VIEW_BACKGROUND_COLOR;
		public final static Color IFGCOLOR = ROTARY_VIEW_FOREGROUND_COLOR;
		public final static Color ITXTBOXBG = CONTROL_TEXTBOX_BACKGROUND;
		public final static Color ITXTBOXFG = CONTROL_TEXTBOX_FOREGROUND;
		public final static Color ITXTBOXSEL = CONTROL_TEXTBOX_SELECTION;
		public final static Color ITXTBOXSELTEXT = CONTROL_TEXTBOX_SELECTED_TEXT;
		public final static Color IKNOBOUTLINE = ROTARY_VIEW_OUTLINE_COLOR;
		public final static Color IKNOBFILL = ROTARY_VIEW_KNOB_COLOR;
		public final static Color IKNOBEXTENT = ROTARY_VIEW_EXTENT_COLOR;
		public final static Color IKNOBINDICATOR = ROTARY_VIEW_INDICATOR_COLOR;
		public final static Color IKNOBFOCUS = ROTARY_VIEW_FOCUS_COLOR;
		public final static Color ILABELCOLOR = CONTROL_LABEL_FOREGROUND;
		public final static Color IUNITSCOLOR = ILABELCOLOR;

		public StdRotaryViewColor()
		{
			super( IBGCOLOR,
					IFGCOLOR,
					ITXTBOXBG,
					ITXTBOXFG,
					ITXTBOXSEL,
					ITXTBOXSELTEXT,
					IKNOBOUTLINE,
					IKNOBFILL,
					IKNOBEXTENT,
					IKNOBINDICATOR,
					IKNOBFOCUS,
					ILABELCOLOR,
					IUNITSCOLOR );
		}
	};
}
