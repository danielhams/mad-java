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

package uk.co.modularaudio.mads.base.mixern.ui.lane;

import java.awt.Color;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.mads.base.mixern.mu.MixerNMadDefinition;
import uk.co.modularaudio.mads.base.mixern.mu.MixerNMadInstance;
import uk.co.modularaudio.mads.base.mixern.ui.MixerNMadUiInstance;
import uk.co.modularaudio.util.audio.math.DbToLevelComputer;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class AmpSliderAndMeter<D extends MixerNMadDefinition<D,I>, I extends MixerNMadInstance<D,I>>
	extends JPanel
{
	private static final long serialVersionUID = 4413604865297302014L;

//	private static Log log = LogFactory.getLog( AmpSliderAndMeter.class.getName() );

	private final AmpSlider ampSlider;
	private final StereoAmpMeter<D,I> stereoAmpMeter;

	public AmpSliderAndMeter( final MixerNMadUiInstance<D,I> uiInstance,
			final BufferedImageAllocator bia,
			final boolean showClipBox,
			final Color foregroundColour )
	{
		this.setOpaque( false );
		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
		msh.addLayoutConstraint( "insets 1" );
		msh.addLayoutConstraint( "gap 0" );
		msh.addLayoutConstraint( "fill" );
//		msh.addLayoutConstraint( "debug" );
		final MigLayout compLayout = msh.createMigLayout();
		this.setLayout( compLayout );

		ampSlider = new AmpSlider( foregroundColour );

		this.add( ampSlider, " growy, gapright 2" );

		stereoAmpMeter = new StereoAmpMeter<D,I>( uiInstance, bia, showClipBox );
		this.add( stereoAmpMeter, "alignx right, growy, growx 0");
		this.validate();
	}

	public String getControlValue()
	{
		return ampSlider.getControlValue();
	}

	public void receiveControlValue( final String value )
	{
		ampSlider.receiveControlValue( value );
	}

	public void receiveMeterReadingInDb( final long currentTimestamp, final int channelNum, final float meterReadingDb )
	{
		stereoAmpMeter.receiveMeterReadingInDb( currentTimestamp, channelNum, meterReadingDb );
	}

	public void receiveDisplayTick( final long currentGuiTime )
	{
		stereoAmpMeter.receiveDisplayTick( currentGuiTime );
	}

	public void destroy()
	{
		stereoAmpMeter.destroy();
	}

	public DbToLevelComputer getDbToLevelComputer()
	{
		return ampSlider.getSliderLevelsAndLabels().getDbToLevelComputer();
	}

	public void setChangeReceiver( final AmpSliderChangeReceiver changeReceiver )
	{
		ampSlider.setChangeReceiver( changeReceiver );
	}
}
