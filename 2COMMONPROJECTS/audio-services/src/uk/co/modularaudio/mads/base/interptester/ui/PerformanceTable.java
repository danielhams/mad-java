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

import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCLabel;

public class PerformanceTable extends JPanel implements PerfDataReceiver
{
	private static final long serialVersionUID = -6944278603929566358L;

	private final LWTCLabel nonePerf;
	private final LWTCLabel sorPerf;
	private final LWTCLabel lPerf;
	private final LWTCLabel hhPerf;
	private final LWTCLabel cdLp24Perf;
	private final LWTCLabel cdSddPerf;
	private final LWTCLabel cdScLp24Perf;

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
		setOpaque( false );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();

		msh.addLayoutConstraint( "fill" );
//		msh.addLayoutConstraint( "debug" );

		msh.addColumnConstraint( "[grow 0, align right][align right]" );

		setLayout( msh.createMigLayout() );

		add( new DarkLabel("None:"), "");
		nonePerf = new DarkLabel();
		add( nonePerf, "wrap");

		add( new DarkLabel("Sum Of Ratios:"), "");
		sorPerf = new DarkLabel();
		add( sorPerf, "wrap");

		add( new DarkLabel("Linear:"), "");
		lPerf = new DarkLabel();
		add( lPerf, "wrap");

		add( new DarkLabel("Half Hann:"), "");
		hhPerf = new DarkLabel();
		add( hhPerf, "wrap");

		add( new DarkLabel("CD Low Pass 24:"), "");
		cdLp24Perf = new DarkLabel();
		add( cdLp24Perf, "wrap");

		add( new DarkLabel("CD Spring Damper (D):"), "");
		cdSddPerf = new DarkLabel();
		add( cdSddPerf, "wrap");

		add( new DarkLabel("CD SC Low Pass 24:"), "");
		cdScLp24Perf = new DarkLabel();
		add( cdScLp24Perf, "wrap");

	}

	@Override
	public void setNoneNanos( final long value )
	{
		nonePerf.setText( Long.toString( value ) );
	}

	@Override
	public void setSorNanos( final long value )
	{
		sorPerf.setText( Long.toString( value ) );
	}

	@Override
	public void setLNanos( final long value )
	{
		lPerf.setText( Long.toString( value ) );
	}

	@Override
	public void setHHNanos( final long value )
	{
		hhPerf.setText( Long.toString( value ) );
	}

	@Override
	public void setCdLp24Nanos( final long value )
	{
		cdLp24Perf.setText( Long.toString( value ) );
	}

	@Override
	public void setCdSddNanos( final long value )
	{
		cdSddPerf.setText( Long.toString( value ) );
	}

	@Override
	public void setCdScLp24Nanos( final long value )
	{
		cdScLp24Perf.setText( Long.toString( value ) );
	}

}
