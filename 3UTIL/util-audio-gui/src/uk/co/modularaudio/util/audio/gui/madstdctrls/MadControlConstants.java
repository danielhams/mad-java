package uk.co.modularaudio.util.audio.gui.madstdctrls;

import java.awt.Color;
import java.awt.Font;

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

	public final static Font RACK_FONT = getRackFont();

	public final static Font getRackFont()
	{
		return new Font( "SansSerif", Font.PLAIN, 11 );
	}

	public final static MadButtonColours STD_BUTTON_COLOURS = getStdButtonColours();

	public final static MadButtonColours getStdButtonColours()
	{
		return new MadButtonColours()
		{

			@Override
			public Color getNoMouseUnselectedGradStart()
			{
				return CONTROL_BUTTON_OUT_GRAD_START;
			}

			@Override
			public Color getNoMouseUnselectedGradEnd()
			{
				return CONTROL_BUTTON_OUT_GRAD_END;
			}

			@Override
			public Color getNoMouseSelectedGradStart()
			{
				return CONTROL_BUTTON_IN_GRAD_START;
			}

			@Override
			public Color getNoMouseSelectedGradEnd()
			{
				return CONTROL_BUTTON_IN_GRAD_END;
			}

			@Override
			public Color getForegroundTextUnselected()
			{
				return CONTROL_FOREGROUND_TEXT_UNSELECTED;
			}

			@Override
			public Color getForegroundTextSelected()
			{
				return CONTROL_FOREGROUND_TEXT_UNSELECTED;
			}

			@Override
			public Color getControlOutline()
			{
				return CONTROL_OUTLINE;
			}

			@Override
			public Color getButtonHighlightUnselected()
			{
				return CONTROL_BUTTON_OUT_HILIGHT;
			}

			@Override
			public Color getButtonHighlightSelected()
			{
				return CONTROL_BUTTON_IN_HILIGHT;
			}

			@Override
			public Color getButtonFocusColour()
			{
				return CONTROL_BUTTON_OUT_HILIGHT;
			}
		};
	}

	public final static MadButtonColours STD_TOGGLE_BUTTON_COLOURS = getStdToggleButtonColours();

	public final static MadButtonColours getStdToggleButtonColours()
	{
		return new MadButtonColours()
		{

			@Override
			public Color getNoMouseUnselectedGradStart()
			{
				return CONTROL_BUTTON_OUT_GRAD_START;
			}

			@Override
			public Color getNoMouseUnselectedGradEnd()
			{
				return CONTROL_BUTTON_OUT_GRAD_END;
			}

			@Override
			public Color getNoMouseSelectedGradStart()
			{
				return CONTROL_BUTTON_IN_GRAD_START;
			}

			@Override
			public Color getNoMouseSelectedGradEnd()
			{
				return CONTROL_BUTTON_IN_GRAD_END;
			}

			@Override
			public Color getForegroundTextUnselected()
			{
				return CONTROL_FOREGROUND_TEXT_UNSELECTED;
			}

			@Override
			public Color getForegroundTextSelected()
			{
				return CONTROL_FOREGROUND_TEXT_SELECTED;
			}

			@Override
			public Color getControlOutline()
			{
				return CONTROL_OUTLINE;
			}

			@Override
			public Color getButtonHighlightUnselected()
			{
				return CONTROL_BUTTON_OUT_HILIGHT;
			}

			@Override
			public Color getButtonHighlightSelected()
			{
				return CONTROL_BUTTON_IN_HILIGHT;
			}

			@Override
			public Color getButtonFocusColour()
			{
				return CONTROL_BUTTON_OUT_HILIGHT;
			}
		};
	}
}
