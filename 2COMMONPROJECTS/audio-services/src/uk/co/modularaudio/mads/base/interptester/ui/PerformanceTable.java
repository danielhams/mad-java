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
	private final LWTCLabel lPerf;
	private final LWTCLabel hhPerf;
	private final LWTCLabel sdPerf;
	private final LWTCLabel lpPerf;
	private final LWTCLabel sddPerf;

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

		add( new DarkLabel("Linear:"), "");
		lPerf = new DarkLabel();
		add( lPerf, "wrap");

		add( new DarkLabel("Half Hann:"), "");
		hhPerf = new DarkLabel();
		add( hhPerf, "wrap");

		add( new DarkLabel("Spring Damper:"), "");
		sdPerf = new DarkLabel();
		add( sdPerf, "wrap");

		add( new DarkLabel("Low Pass:"), "");
		lpPerf = new DarkLabel();
		add( lpPerf, "wrap");

		add( new DarkLabel("Spring Damper (D):"), "");
		sddPerf = new DarkLabel();
		add( sddPerf, "wrap");
	}

	@Override
	public void setNoneNanos( final long value )
	{
		nonePerf.setText( Long.toString( value ) );
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
	public void setSDNanos( final long value )
	{
		sdPerf.setText( Long.toString( value ) );
	}

	@Override
	public void setLPNanos( final long value )
	{
		lpPerf.setText( Long.toString( value ) );
	}

	@Override
	public void setSDDNanos( final long value )
	{
		sddPerf.setText( Long.toString( value ) );
	}
}
