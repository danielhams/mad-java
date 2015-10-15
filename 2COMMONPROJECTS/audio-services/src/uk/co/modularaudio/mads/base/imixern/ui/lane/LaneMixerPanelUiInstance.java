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

package uk.co.modularaudio.mads.base.imixern.ui.lane;

import java.awt.Color;

import javax.swing.JComponent;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.mads.base.djeq.ui.DJEQColorDefines;
import uk.co.modularaudio.mads.base.imixern.mu.MixerNMadDefinition;
import uk.co.modularaudio.mads.base.imixern.mu.MixerNMadInstance;
import uk.co.modularaudio.mads.base.imixern.ui.MixerNMadUiDefinition;
import uk.co.modularaudio.mads.base.imixern.ui.MixerNMadUiInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacPanel;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.audio.mvc.rotarydisplay.models.MixerLanePanRotaryDisplayModel;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayController;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayModel;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayTextbox;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderViewColors;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayKnob;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayKnob.KnobType;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryViewColors;

public class LaneMixerPanelUiInstance<D extends MixerNMadDefinition<D,I>,
		I extends MixerNMadInstance<D,I>,
		U extends MixerNMadUiInstance<D,I>>
	extends PacPanel
	implements IMadUiControlInstance<D,I,U>
{
	private static final long serialVersionUID = -3862457210177904367L;

//	private static Log log = LogFactory.getLog( LaneMixerPanelUiInstance.class.getName() );

	public final static LWTCSliderViewColors SLIDER_COLORS = getSliderColors();

	private final static LWTCSliderViewColors getSliderColors()
	{
		final Color bgColor = MixerNMadUiDefinition.LANE_BG_COLOR;
		final Color fgColor = MixerNMadUiDefinition.LANE_FG_COLOR;
		final Color indicatorColor = MixerNMadUiDefinition.LANE_INDICATOR_COLOR;
		final Color textboxBgColor = LWTCControlConstants.CONTROL_TEXTBOX_BACKGROUND;
		final Color textboxFgColor = LWTCControlConstants.CONTROL_TEXTBOX_FOREGROUND;
		final Color selectionColor = LWTCControlConstants.CONTROL_TEXTBOX_SELECTION;
		final Color selectedTextColor = LWTCControlConstants.CONTROL_TEXTBOX_SELECTED_TEXT;
		final Color labelColor = Color.LIGHT_GRAY;
		final Color unitsColor = MixerNMadUiDefinition.LANE_FG_COLOR;

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

	public final static RotaryViewColors ROTARY_COLORS = getRotaryColors();

	private final static RotaryViewColors getRotaryColors()
	{
		final Color bgColor = MixerNMadUiDefinition.LANE_BG_COLOR;
		final Color fgColor = MixerNMadUiDefinition.LANE_FG_COLOR;
		final Color textboxBgColor = LWTCControlConstants.CONTROL_TEXTBOX_BACKGROUND;
		final Color textboxFgColor = LWTCControlConstants.CONTROL_TEXTBOX_FOREGROUND;
		final Color selectionColor = LWTCControlConstants.CONTROL_TEXTBOX_SELECTION;
		final Color selectedTextColor = LWTCControlConstants.CONTROL_TEXTBOX_SELECTED_TEXT;
		final Color knobOutlineColor = DJEQColorDefines.OUTLINE_COLOR;
		final Color knobFillColor = DJEQColorDefines.KNOB_COLOR;
		final Color knobExtentColor = DJEQColorDefines.EXTENT_COLOR;
		final Color knobIndicatorColor = DJEQColorDefines.INDICATOR_COLOR;
		final Color knobFocusColor = DJEQColorDefines.FOCUS_COLOR;
		final Color labelColor = LWTCControlConstants.CONTROL_LABEL_FOREGROUND;
		final Color unitsColor = MixerNMadUiDefinition.LANE_FG_COLOR;

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

	private final int laneNumber;

	private final LaneFaderAndMarks<D,I> faderAndMarks;
	private final LaneStereoAmpMeter<D,I> stereoAmpMeter;
	private final MixerLanePanRotaryDisplayModel panModel;
	private final RotaryDisplayKnob panControl;
	private final LaneMuteSolo<D,I,U> muteSolo;

	private final LWTCSliderDisplayTextbox ampSliderTextbox;

	public LaneMixerPanelUiInstance( final D definition,
			final I instance,
			final U uiInstance,
			final int controlIndex )
	{
		this.setOpaque( true );
		this.setBackground( MixerNMadUiDefinition.LANE_BG_COLOR );

		laneNumber = controlIndex;

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();

		msh.addRowConstraint( "[][fill]" );

//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "insets 5" );
		msh.addLayoutConstraint( "gap 0" );
		msh.addLayoutConstraint( "fill" );

		final MigLayout compLayout = msh.createMigLayout();
		this.setLayout( compLayout );

		panModel = new MixerLanePanRotaryDisplayModel();

		panModel.addChangeListener( new RotaryDisplayModel.ValueChangeListener()
		{
			@Override
			public void receiveValueChange( final Object source, final float newValue )
			{
				uiInstance.sendLanePan( laneNumber, newValue );
			}
		} );
		final RotaryDisplayController panController = new RotaryDisplayController( panModel );
		panControl = new RotaryDisplayKnob( panModel,
				panController,
				KnobType.BIPOLAR,
				ROTARY_COLORS,
				false,
				true );
		this.add( panControl, "cell 0 0, pushx 50, width 33, height 33, growy 0, align center" );
		panControl.setDiameter( 31 );

		muteSolo = new LaneMuteSolo<D,I,U>( uiInstance, laneNumber );
		this.add( muteSolo, "cell 1 0, pushx 50, growy 0, align center" );

		faderAndMarks = new LaneFaderAndMarks<D,I>( uiInstance,
				uiInstance.getUiDefinition().getBufferedImageAllocator(),
				true,
				SLIDER_COLORS, new SliderDisplayModel.ValueChangeListener()
				{
					@Override
					public void receiveValueChange( final Object source, final float newValue )
					{
						final float ampForDb = (float)AudioMath.dbToLevel( newValue );
						uiInstance.sendLaneAmp( laneNumber, ampForDb );
					}
				} );

		this.add( faderAndMarks, "cell 0 1, grow, pushy 100" );

		stereoAmpMeter = new LaneStereoAmpMeter<D,I>( uiInstance,
				uiInstance.getUiDefinition().getBufferedImageAllocator(),
				true );

		this.add( stereoAmpMeter, "cell 1 1, grow, pushy 100" );

		ampSliderTextbox = new LWTCSliderDisplayTextbox(
				faderAndMarks.getFaderModel(),
				faderAndMarks.getFaderController(),
				SLIDER_COLORS,
				isOpaque() );

		this.add( ampSliderTextbox, "cell 0 2, spanx 2, grow 0" );

		uiInstance.registerLaneMeterReceiver( laneNumber, stereoAmpMeter );
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime )
	{
		stereoAmpMeter.receiveDisplayTick( currentGuiTime );
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	@Override
	public String getControlValue()
	{
		return faderAndMarks.getControlValue() + ":" + panModel.getValue() + ":" + muteSolo.getControlValue();
	}

	@Override
	public void receiveControlValue( final String value )
	{
		final String[] vals = value.split( ":" );
		if( vals.length > 0 )
		{
			faderAndMarks.receiveControlValue( this, vals[0] );
		}
		if( vals.length > 1 )
		{
			panModel.setValue( this, Float.parseFloat( vals[1] ) );
		}
		if( vals.length > 2 )
		{
			muteSolo.receiveControlValue( this, vals[2] );
		}
	}

	@Override
	public void destroy()
	{
		stereoAmpMeter.destroy();
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return true;
	}
}
