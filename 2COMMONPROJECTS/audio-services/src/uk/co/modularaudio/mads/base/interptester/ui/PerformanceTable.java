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

package uk.co.modularaudio.mads.base.interptester.ui;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import uk.co.modularaudio.mads.base.interptester.mu.InterpolatorType;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCLabel;

public class PerformanceTable extends JPanel implements PerfDataReceiver
{
	private static final long serialVersionUID = -6944278603929566358L;

	private final LWTCLabel[] perfLabels;

	private class DarkLabel extends LWTCLabel
	{
		private static final long serialVersionUID = 1744316889663557010L;
		public DarkLabel( final String text )
		{
			super( text );
			setForeground( Color.BLACK );
			setBorder( BorderFactory.createEmptyBorder() );
			setFont( LWTCControlConstants.LABEL_FONT );
		}
		public DarkLabel()
		{
			super();
			setForeground( Color.BLACK );
			setBorder( BorderFactory.createEmptyBorder() );
			setFont( LWTCControlConstants.LABEL_FONT );
		}
	}

	public PerformanceTable()
	{
		perfLabels = new LWTCLabel[InterpolatorType.values().length];

		setOpaque( false );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();

		msh.addLayoutConstraint( "fill" );
//		msh.addLayoutConstraint( "debug" );

		msh.addColumnConstraint( "[grow 0, align right][align right]" );

		setLayout( msh.createMigLayout() );

		int channelNum = 0;
		for( final InterpolatorType it : InterpolatorType.values() )
		{
			final DarkLabel label = new DarkLabel(it.getChannelPrefix() + ":" );
			final DarkLabel perfEntry = new DarkLabel();
			add( label, "" );
			add( perfEntry, "wrap" );
			perfLabels[channelNum] = perfEntry;
			channelNum++;
		}
	}

	@Override
	public void setInterpolatorNanos( final int interpolator,
			final int nanos )
	{
		perfLabels[interpolator].setText( Integer.toString( nanos ) );
	}

}
