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

package uk.co.modularaudio.mads.base.djeq2.ui;

import java.awt.Color;
import java.awt.Component;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.djeq2.mu.DJEQ2MadDefinition;
import uk.co.modularaudio.mads.base.djeq2.mu.DJEQ2MadInstance;
import uk.co.modularaudio.mads.base.djeqn.ui.ColorDefines;
import uk.co.modularaudio.mads.base.djeqn.ui.FaderMarks;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacPanel;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.DJDeckFaderSliderModel;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayController;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel.ValueChangeListener;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplaySlider;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayTextbox;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView.DisplayOrientation;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderViewColors;

public class DJEQ2FaderMarksMeter extends PacPanel
	implements IMadUiControlInstance<DJEQ2MadDefinition, DJEQ2MadInstance, DJEQ2MadUiInstance>
{
	private static final long serialVersionUID = -4624215012389837804L;

	private static Log log = LogFactory.getLog( DJEQ2FaderMarksMeter.class.getName() );

	private final static LWTCSliderViewColors SLIDER_COLORS = getSliderColours();

	private final static LWTCSliderViewColors getSliderColours()
	{
		final Color bgColor = ColorDefines.BACKGROUND_COLOR;
		final Color fgColor = ColorDefines.FOREGROUND_COLOR;
		final Color indicatorColor = ColorDefines.INDICATOR_COLOR;
		final Color textboxBgColor = LWTCControlConstants.CONTROL_TEXTBOX_BACKGROUND;
		final Color textboxFgColor = LWTCControlConstants.CONTROL_TEXTBOX_FOREGROUND;
		final Color selectionColor = LWTCControlConstants.CONTROL_TEXTBOX_SELECTION;
		final Color selectedTextColor = LWTCControlConstants.CONTROL_TEXTBOX_SELECTED_TEXT;
		final Color labelColor = ColorDefines.LABEL_COLOR;
		final Color unitsColor = ColorDefines.UNITS_COLOR;

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

	private final DJEQ2MadUiInstance uiInstance;

	private final DJDeckFaderSliderModel sdm;
	private final SliderDisplayController sdc;
	private final LWTCSliderDisplaySlider fader;
	private final FaderMarks faderMarks;
	private final DjEQ2LaneStereoAmpMeter sam;
	private final LWTCSliderDisplayTextbox faderTextbox;

	private boolean previouslyShowing;

	public DJEQ2FaderMarksMeter( final DJEQ2MadDefinition definition,
			final DJEQ2MadInstance instance,
			final DJEQ2MadUiInstance uiInstance,
			final int controlIndex )
	{
		this.uiInstance = uiInstance;

		setOpaque( false );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "fill" );
		msh.addLayoutConstraint( "insets 0" );
		msh.addLayoutConstraint( "gap 0" );
		setLayout( msh.createMigLayout() );

		sdm = new DJDeckFaderSliderModel();
		sdc = new SliderDisplayController( sdm );

		fader = new LWTCSliderDisplaySlider( sdm, sdc, DisplayOrientation.VERTICAL, SLIDER_COLORS, false, true );
		this.add( fader, "cell 0 0, growy, pushy 100" );

		faderMarks = new FaderMarks( sdm, Color.BLACK, false );
		this.add( faderMarks, "cell 1 0, growy, pushy 100" );

		sam = new DjEQ2LaneStereoAmpMeter( uiInstance,
				((DJEQ2MadUiDefinition)uiInstance.getUiDefinition()).getBufferedImageAllocator(),
				true );
		this.add( sam, "cell 2 0, growy");

		faderTextbox = new LWTCSliderDisplayTextbox( sdm, sdc,
				SLIDER_COLORS,
				false );
		this.add( faderTextbox, "cell 0 1, spanx 3, growy 0, align left" );

		sdm.addChangeListener( new ValueChangeListener()
		{
			@Override
			public void receiveValueChange( final Object source, final float newValue )
			{
				final float realAmp = AudioMath.dbToLevelF( newValue );
				uiInstance.setFaderAmp( realAmp );
			}
		} );

		uiInstance.setStereoAmpMeter( sam );

	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return true;
	}

	@Override
	public String getControlValue()
	{
		return Float.toString( sdm.getValue() );
	}

	@Override
	public void receiveControlValue( final String value )
	{
		if( value != null && value.length() > 0 )
		{
			try
			{
				final float floatVal = Float.parseFloat( value );
				sdc.setValue( this, floatVal );
			}
			catch( final NumberFormatException nfe )
			{
				if( log.isWarnEnabled() )
				{
					log.warn("Failed parsing DJEQFader value: " + value );
				}
			}
		}
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final int U_currentGuiTime,
			final int framesSinceLastTick )
	{
		final boolean showing = isShowing();

		if( previouslyShowing != showing )
		{
			uiInstance.sendUiActive( showing );
			previouslyShowing = showing;
		}

		sam.receiveDisplayTick( U_currentGuiTime, framesSinceLastTick );
	}

	@Override
	public Component getControl()
	{
		return this;
	}

	@Override
	public void destroy()
	{
		sam.destroy();
	}
}
