package uk.co.modularaudio.mads.base.interptester.ui;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;

import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class PerformanceTable extends JPanel implements PerfDataReceiver
{
	private static final long serialVersionUID = -6944278603929566358L;

	private final JLabel nonePerf;
	private final JLabel lPerf;
	private final JLabel hhPerf;
	private final JLabel sdPerf;
	private final JLabel lpPerf;
	private final JLabel sddPerf;

	private class DarkLabel extends JLabel
	{
		private static final long serialVersionUID = 1744316889663557010L;
		public DarkLabel( final String text )
		{
			super( text );
			setForeground( Color.BLACK );
		}
		public DarkLabel()
		{
			super();
			setForeground( Color.BLACK );
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
