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

import javax.swing.JPanel;

import uk.co.modularaudio.mads.base.imixern.mu.MixerNMadDefinition;
import uk.co.modularaudio.mads.base.imixern.mu.MixerNMadInstance;
import uk.co.modularaudio.mads.base.imixern.ui.MixerNMadUiInstance;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.MixdownSliderModel;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayController;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel.ValueChangeListener;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplaySlider;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView.DisplayOrientation;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderViewColors;

public class LaneFaderAndMarks<D extends MixerNMadDefinition<D,I>, I extends MixerNMadInstance<D,I>>
	extends JPanel
{
	private static final long serialVersionUID = 4413604865297302014L;

//	private static Log log = LogFactory.getLog( AmpSliderAndMeter.class.getName() );

	private final MixdownSliderModel faderModel;
	private final SliderDisplayController faderController;
	private final LWTCSliderDisplaySlider mixerFader;
	private final MixerFaderMarks mixerFaderLabels;

	public LaneFaderAndMarks( final MixerNMadUiInstance<D,I> uiInstance,
			final BufferedImageAllocator bia,
			final boolean showClipBox,
			final LWTCSliderViewColors colors )
	{
		this.setOpaque( false );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "inset 0" );
		msh.addLayoutConstraint( "gap 0" );
		msh.addLayoutConstraint( "fill" );

		this.setLayout( msh.createMigLayout() );

		faderModel = new MixdownSliderModel();
		faderController = new SliderDisplayController( faderModel );
		mixerFader = new LWTCSliderDisplaySlider( faderModel,
				faderController,
				DisplayOrientation.VERTICAL,
				colors,
				false,
				true );
		this.add( mixerFader, "growy" );

		mixerFaderLabels = new MixerFaderMarks( faderModel,
				colors.labelColor,
				false );
		this.add( mixerFaderLabels, "growy" );
	}

	public String getControlValue()
	{
		return Float.toString( faderModel.getValue() );
	}

	public void receiveControlValue( final Object source, final String value )
	{
		if( source != this )
		{
			faderController.setValue( source, Float.parseFloat( value ) );
		}
	}

	public void setChangeReceiver( final LaneFaderChangeReceiver changeReceiver )
	{
		faderModel.addChangeListener( new ValueChangeListener()
		{
			@Override
			public void receiveValueChange( final Object source, final float newValue )
			{
				changeReceiver.receiveFaderAmpChange( source, newValue );
			}
		} );
	}

	public MixdownSliderModel getFaderModel()
	{
		return faderModel;
	}

	public SliderDisplayController getFaderController()
	{
		return faderController;
	}
}
