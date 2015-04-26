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

package uk.co.modularaudio.mads.base.imixern.ui.master;

import java.awt.Color;

import javax.swing.JComponent;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.mads.base.imixern.mu.MixerNMadDefinition;
import uk.co.modularaudio.mads.base.imixern.mu.MixerNMadInstance;
import uk.co.modularaudio.mads.base.imixern.ui.MixerNMadUiDefinition;
import uk.co.modularaudio.mads.base.imixern.ui.MixerNMadUiInstance;
import uk.co.modularaudio.mads.base.imixern.ui.lane.AmpSlider;
import uk.co.modularaudio.mads.base.imixern.ui.lane.AmpSliderChangeReceiver;
import uk.co.modularaudio.mads.base.imixern.ui.lane.LaneMixerPanelUiInstance;
import uk.co.modularaudio.mads.base.imixern.ui.lane.MeterValueReceiver;
import uk.co.modularaudio.mads.base.imixern.ui.lane.PanChangeReceiver;
import uk.co.modularaudio.mads.base.imixern.ui.lane.PanControl;
import uk.co.modularaudio.mads.base.imixern.ui.lane.StereoAmpMeter;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacPanel;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayController;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayModel;
import uk.co.modularaudio.util.mvc.displayrotary.SimpleRotaryIntToFloatConverter;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayTextbox;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderViewColors;

public class MasterMixerPanelUiInstance<D extends MixerNMadDefinition<D, I>,
		I extends MixerNMadInstance<D, I>,
		U extends MixerNMadUiInstance<D, I>>
	extends PacPanel
	implements IMadUiControlInstance<D,I,U>,
	AmpSliderChangeReceiver, MeterValueReceiver, PanChangeReceiver
{
	private static final long serialVersionUID = 24665241385474657L;

//	private static Log log = LogFactory.getLog( ChannelMasterMixerPanelUiInstance.class.getName() );

	public final static LWTCSliderViewColors SLIDER_COLORS = getSliderColors();

	private final static LWTCSliderViewColors getSliderColors()
	{
		final Color bgColor = MixerNMadUiDefinition.MASTER_BG_COLOR;
		final Color fgColor = MixerNMadUiDefinition.MASTER_FG_COLOR;
		final Color textboxBgColor = LWTCControlConstants.CONTROL_TEXTBOX_BACKGROUND;
		final Color textboxFgColor = LWTCControlConstants.CONTROL_TEXTBOX_FOREGROUND;
		final Color selectionColor = LWTCControlConstants.CONTROL_TEXTBOX_SELECTION;
		final Color selectedTextColor = LWTCControlConstants.CONTROL_TEXTBOX_SELECTED_TEXT;
		final Color labelColor = MixerNMadUiDefinition.MASTER_FG_COLOR;
		final Color unitsColor = MixerNMadUiDefinition.MASTER_FG_COLOR;

		return new LWTCSliderViewColors( bgColor,
				fgColor,
				textboxBgColor,
				textboxFgColor,
				selectionColor,
				selectedTextColor,
				labelColor,
				unitsColor );
	}

	private final AmpSlider<D,I> ampSlider;
	private final StereoAmpMeter<D,I> stereoAmpMeter;
	private final RotaryDisplayModel panModel;
	private final PanControl panControl;

	private final U uiInstance;

	private boolean previouslyShowing;

	private final LWTCSliderDisplayTextbox ampSliderTextbox;

	public MasterMixerPanelUiInstance( final D definition,
			final I instance,
			final U uiInstance,
			final int controlIndex )
	{
		this.uiInstance = uiInstance;

		this.setOpaque( true );
		this.setBackground( MixerNMadUiDefinition.MASTER_BG_COLOR );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
		msh.addRowConstraint( "[][fill]" );

//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "insets 5" );
		msh.addLayoutConstraint( "gap 0" );
		msh.addLayoutConstraint( "fill" );

		final MigLayout compLayout = msh.createMigLayout();
		this.setLayout( compLayout );

		panModel = new RotaryDisplayModel(
				-1.0f, 1.0f, 0.0f,
				256, 32,
				new SimpleRotaryIntToFloatConverter(),
				3, 2, "dB" );
		final RotaryDisplayController panController = new RotaryDisplayController( panModel );
		panControl = new PanControl( panModel, panController, this, LaneMixerPanelUiInstance.ROTARY_COLORS );
		this.add( panControl, "cell 0 0, spanx 2, pushx 50, width 33, height 33, growy 0, align center" );

		ampSlider = new AmpSlider<D,I>(
				uiInstance,
				uiInstance.getUiDefinition().getBufferedImageAllocator(),
				true,
				SLIDER_COLORS );

		this.add( ampSlider, "cell 0 1, grow, pushy 100" );

		ampSlider.setChangeReceiver( this );


		stereoAmpMeter = new StereoAmpMeter<D,I>( uiInstance,
				uiInstance.getUiDefinition().getBufferedImageAllocator(),
				true );

		this.add( stereoAmpMeter, "cell 1 1, grow, pushy 100" );

		ampSliderTextbox = new LWTCSliderDisplayTextbox(
				ampSlider.getFaderModel(),
				ampSlider.getFaderController(),
				SLIDER_COLORS,
				isOpaque() );

		this.add( ampSliderTextbox, "cell 0 2, spanx 2, grow 0" );

		uiInstance.registerMasterMeterReceiver( this );
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime )
	{
		final boolean showing = isShowing();

		if( previouslyShowing != showing )
		{
			uiInstance.sendUiActive( showing );
			previouslyShowing = showing;
		}

		stereoAmpMeter.receiveDisplayTick( currentGuiTime );
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	@Override
	public void receiveAmpSliderChange( final Object source, final float newValue )
	{
		// Now translate this into amplitude
		final float ampForDb = (float)AudioMath.dbToLevel( newValue );
		uiInstance.sendMasterAmp( ampForDb );
	}

	@Override
	public String getControlValue()
	{
		return ampSlider.getControlValue() + ":" + panModel.getValue();
	}

	@Override
	public void receiveControlValue( final String value )
	{
		final String[] vals = value.split( ":" );
		if( vals.length == 2 )
		{
			ampSlider.receiveControlValue( this, vals[0] );
			panModel.setValue( this, Float.parseFloat( vals[1] ) );
		}
	}

	@Override
	public void receiveMeterReadingLevel( final long currentTimestamp, final int channelNum, final float meterReadingLevel )
	{
		final float meterReadingDb = (float)AudioMath.levelToDb( meterReadingLevel );
		stereoAmpMeter.receiveMeterReadingInDb( currentTimestamp, channelNum, meterReadingDb );
	}

	@Override
	public void receiveMuteSet( final long currentTimestamp, final boolean muted )
	{
		// Ignore
	}

	@Override
	public void receiveSoloSet( final long currentTimestamp, final boolean muted )
	{
		// Ignore
	}

	@Override
	public void destroy()
	{
		stereoAmpMeter.destroy();
	}

	@Override
	public void receivePanChange( final float panValue )
	{
		uiInstance.sendMasterPan( panValue );
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return true;
	}
}
