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

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.util.audio.math.DbToLevelComputer;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class AmpSliderAndMeter extends JPanel
{
	private static final long serialVersionUID = 4413604865297302014L;

//	private static Log log = LogFactory.getLog( AmpSliderAndMeter.class.getName() );

	private AmpSlider ampSlider = null;
	private StereoAmpMeter stereoAmpMeter = null;
//	private AmpMeter leftAmpMeter = null;
//	private AmpMeterLevelMarks ampMeterLabels = null;
//	private AmpMeter rightAmpMeter = null;

	public AmpSliderAndMeter( MixerMadUiInstance uiInstance,
			BufferedImageAllocator bia,
			boolean showClipBox,
			Color foregroundColour )
	{
		this.setOpaque( false );
		MigLayoutStringHelper msh = new MigLayoutStringHelper();
		msh.addLayoutConstraint( "insets 1" );
		msh.addLayoutConstraint( "gap 0" );
		msh.addLayoutConstraint( "fill" );
//		msh.addLayoutConstraint( "debug" );
		MigLayout compLayout = msh.createMigLayout();
		this.setLayout( compLayout );

		ampSlider = new AmpSlider( foregroundColour );

		this.add( ampSlider, " growy, gapright 2" );

		stereoAmpMeter = new StereoAmpMeter( uiInstance, bia, showClipBox );
		this.add( stereoAmpMeter, "alignx right, growy, growx 0");
		this.validate();
	}

	public String getControlValue()
	{
		return ampSlider.getControlValue();
	}

	public void receiveControlValue( String value )
	{
		ampSlider.receiveControlValue( value );
	}

	public void receiveMeterReadingInDb( long currentTimestamp, int channelNum, float meterReadingDb )
	{
//		switch( channelNum )
//		{
//			case 0:
//			{
//				leftAmpMeter.receiveMeterReadingInDb( currentTimestamp, meterReadingDb );
//				break;
//			}
//			case 1:
//			{
//				rightAmpMeter.receiveMeterReadingInDb( currentTimestamp, meterReadingDb );
//				break;
//			}
//			default:
//			{
//				log.error("Oops. Which channel was this for?");
//				break;
//			}
//		}
		stereoAmpMeter.receiveMeterReadingInDb( currentTimestamp, channelNum, meterReadingDb );
	}

	public void receiveDisplayTick( long currentGuiTime )
	{
//		leftAmpMeter.receiveDisplayTick( currentGuiTime );
//		rightAmpMeter.receiveDisplayTick( currentGuiTime );
		stereoAmpMeter.receiveDisplayTick( currentGuiTime );
	}

	public void destroy()
	{
//		leftAmpMeter.destroy();
//		rightAmpMeter.destroy();
		stereoAmpMeter.destroy();
	}

	public DbToLevelComputer getDbToLevelComputer()
	{
		return ampSlider.getSliderLevelsAndLabels().getDbToLevelComputer();
	}

	public void setChangeReceiver( AmpSliderChangeReceiver changeReceiver )
	{
		ampSlider.setChangeReceiver( changeReceiver );
	}
}
