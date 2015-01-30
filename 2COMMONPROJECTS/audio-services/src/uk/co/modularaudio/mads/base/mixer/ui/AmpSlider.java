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

package uk.co.modularaudio.mads.base.mixer.ui;

import java.awt.Color;
import java.awt.Font;

import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacSlider;
import uk.co.modularaudio.util.audio.math.DbToLevelComputer;
import uk.co.modularaudio.util.swing.mvc.SliderDoubleClickMouseListener;
import uk.co.modularaudio.util.swing.mvc.SliderDoubleClickMouseListener.SliderDoubleClickReceiver;

public class AmpSlider extends PacSlider implements SliderDoubleClickReceiver
{
	private static final long serialVersionUID = -5719433475892570967L;

//	private static Log log = LogFactory.getLog( AmpSlider.class.getName() );

	private AmpSliderChangeReceiver changeReceiver;

	private final AmpSliderLevelsAndLabels ampSliderModel;
	private final DbToLevelComputer sliderDbToLevelComputer;

	private final SliderDoubleClickMouseListener ampSliderMouseListener;

	public AmpSlider( final Color foregroundColour )
	{

		this.setOpaque( false );
		this.setOrientation( VERTICAL );
		final Font f = this.getFont().deriveFont( 9.5f );
//		Font f = this.getFont();

		ampSliderModel = AmpSliderLevelsAndLabels.getInstance( f, foregroundColour );

		setMinimum( 0 );
		setMaximum( AmpSliderLevelsAndLabels.AMP_SLIDER_NUM_STEPS );

		setMajorTickSpacing( 100 );
//		setPaintTicks( true );
		setPaintLabels( true );

		sliderDbToLevelComputer  = ampSliderModel.getDbToLevelComputer();

		setLabelTable( ampSliderModel.getLabels() );

		setFont( f );
		setForeground( foregroundColour );

		setValue( 0 );

		ampSliderMouseListener = new SliderDoubleClickMouseListener( this );

		this.addMouseListener( ampSliderMouseListener );
	}

	@Override
	public void processValueChange( final int previousValue, final int newValue )
	{
		if( changeReceiver != null )
		{
			final float floatVal = (float)newValue / AmpSliderLevelsAndLabels.AMP_SLIDER_NUM_STEPS;
			changeReceiver.receiveAmpSliderChange( floatVal );
		}
	}

	@Override
	public void receiveDoubleClick()
	{
		// Reset to zero DB
		final float sliderVal = sliderDbToLevelComputer.toNormalisedSliderLevelFromDb( 0.0f ) * AmpSliderLevelsAndLabels.AMP_SLIDER_NUM_STEPS;
//		log.debug("Resetting to 0dB (" + sliderVal + ")");
		this.setValue( (int)sliderVal );
	}

	public AmpSliderLevelsAndLabels getSliderLevelsAndLabels()
	{
		return ampSliderModel;
	}

	public void setChangeReceiver( final AmpSliderChangeReceiver changeReceiver )
	{
		this.changeReceiver = changeReceiver;
		final int valueToBroadcast = getValue();
		processValueChange( valueToBroadcast, valueToBroadcast );
	}
}
