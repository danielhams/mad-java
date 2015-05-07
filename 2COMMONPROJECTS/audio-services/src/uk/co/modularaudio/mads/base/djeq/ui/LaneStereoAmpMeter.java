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

package uk.co.modularaudio.mads.base.djeq.ui;

import java.awt.Color;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.common.ampmeter.BIAmpMeter;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class LaneStereoAmpMeter extends JPanel
{
	private static final long serialVersionUID = 1358562457507980606L;

	private static Log log = LogFactory.getLog( LaneStereoAmpMeter.class.getName() );

	private final BIAmpMeter leftMeter;
	private final AmpMeterMarks meterMarks;
	private final BIAmpMeter rightMeter;

	public LaneStereoAmpMeter( final DJEQMadUiInstance uiInstance, final BufferedImageAllocator bia, final boolean showClipBox )
	{
		super();
		setOpaque( false );
		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "filly" );
		msh.addLayoutConstraint( "gap 0" );
		msh.addLayoutConstraint( "insets 1" );

		this.setLayout( msh.createMigLayout() );

		leftMeter = new BIAmpMeter( bia, showClipBox );
		this.add( leftMeter, "gapbottom " +
				AmpMeterMarks.METER_LABEL_NEEDED_TOP_BOTTOM_INSET_PIXELS + ", alignx right, growy" );
		meterMarks = new AmpMeterMarks( Color.BLACK, false );
		this.add( meterMarks, "growy, growx 0");
		rightMeter = new BIAmpMeter( bia, showClipBox );
		this.add( rightMeter, "gapbottom "+
				AmpMeterMarks.METER_LABEL_NEEDED_TOP_BOTTOM_INSET_PIXELS + ", alignx left, growy" );
		this.validate();
	}

	public void receiveMeterReadingInDb( final long currentTimestamp, final int channelNum,
			final float meterReadingDb )
	{
		switch( channelNum )
		{
			case 0:
			{
				leftMeter.receiveMeterReadingInDb( currentTimestamp, meterReadingDb );
				break;
			}
			case 1:
			{
				rightMeter.receiveMeterReadingInDb( currentTimestamp, meterReadingDb );
				break;
			}
			default:
			{
				log.error("Oops. Which channel was this for?");
				break;
			}
		}
	}

	public void receiveDisplayTick( final long currentGuiTime )
	{
		leftMeter.receiveDisplayTick( currentGuiTime );
		rightMeter.receiveDisplayTick( currentGuiTime );
	}

	public void destroy()
	{
		leftMeter.destroy();
		rightMeter.destroy();
	}

	public void setFramesBetweenPeakReset( final int framesBetweenPeakReset )
	{
		leftMeter.setFramesBetweenPeakReset( framesBetweenPeakReset );
		rightMeter.setFramesBetweenPeakReset( framesBetweenPeakReset );
	}

}
