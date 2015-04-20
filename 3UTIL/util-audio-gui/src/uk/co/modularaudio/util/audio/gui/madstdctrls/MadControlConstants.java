package uk.co.modularaudio.util.audio.gui.madstdctrls;

import java.awt.Color;
import java.awt.Font;

import uk.co.modularaudio.util.audio.gui.madstdctrls.AbstractMadButton.MadButtonState;

public class MadControlConstants
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

	public final static Font RACK_FONT = getRackFont();

	public final static Font getRackFont()
	{
		return new Font( "SansSerif", Font.PLAIN, 11 );
	}

	public final static MadButtonColours STD_BUTTON_COLOURS = new StdButtonColours();
	public final static MadButtonColours STD_TOGGLE_BUTTON_COLOURS = new StdToggleButtonColours();

	private static class StdButtonColours implements MadButtonColours
	{
		private final MadButtonStateColours[] stateToColoursMap;

		public StdButtonColours()
		{
			stateToColoursMap = new MadButtonStateColours[ MadButtonState.values().length ];

			stateToColoursMap[ MadButtonState.OUT_NO_MOUSE.ordinal() ] = new MadButtonStateColours()
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
			stateToColoursMap[ MadButtonState.OUT_MOUSE.ordinal() ] = new MadButtonStateColours()
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
			stateToColoursMap[ MadButtonState.IN_MOUSE.ordinal() ] = new MadButtonStateColours()
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
			stateToColoursMap[ MadButtonState.IN_NO_MOUSE.ordinal() ] = new MadButtonStateColours()
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
		public MadButtonStateColours getColoursForState( final MadButtonState state )
		{
			return stateToColoursMap[ state.ordinal() ];
		}
	};

	private static class StdToggleButtonColours implements MadButtonColours
	{
		private final MadButtonStateColours[] stateToColoursMap;

		public StdToggleButtonColours()
		{
			stateToColoursMap = new MadButtonStateColours[ MadButtonState.values().length ];

			stateToColoursMap[ MadButtonState.OUT_NO_MOUSE.ordinal() ] = new MadButtonStateColours()
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
			stateToColoursMap[ MadButtonState.OUT_MOUSE.ordinal() ] = new MadButtonStateColours()
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
			stateToColoursMap[ MadButtonState.IN_MOUSE.ordinal() ] = new MadButtonStateColours()
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
			stateToColoursMap[ MadButtonState.IN_NO_MOUSE.ordinal() ] = new MadButtonStateColours()
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
		public MadButtonStateColours getColoursForState( final MadButtonState state )
		{
			return stateToColoursMap[ state.ordinal() ];
		}
	};
}
