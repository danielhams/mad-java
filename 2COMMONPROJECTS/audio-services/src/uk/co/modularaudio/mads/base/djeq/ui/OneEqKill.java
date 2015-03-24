package uk.co.modularaudio.mads.base.djeq.ui;

import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacToggleButton;

public class OneEqKill extends PacToggleButton
{
	private static final long serialVersionUID = -5705961814474040293L;

	private ToggleListener toggleListener;

	public interface ToggleListener
	{
		void receiveToggleChange( boolean previousValue, boolean newValue );
	};

	public OneEqKill( )
	{
		super( false );
		setText( "Kill" );
	}

	@Override
	public void receiveUpdateEvent( final boolean previousValue, final boolean newValue )
	{
		toggleListener.receiveToggleChange( previousValue, newValue );
	}

	public void setToggleListener( final ToggleListener tl )
	{
		this.toggleListener = tl;
	}

}
