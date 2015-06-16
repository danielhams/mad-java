package uk.co.modularaudio.componentdesigner.profiling.tracks;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;

public class TrackBlock extends JTextArea
{
	private static final long serialVersionUID = -6324229180501933446L;

	public TrackBlock( final String blockLabel, final Color blockColor, final String tooltipText )
	{
		super( blockLabel );
		setEditable( false );
		setForeground( Color.black );
		setBackground( blockColor );
		setBorder( BorderFactory.createLineBorder( Color.BLACK ) );
		setOpaque( true );

		setToolTipText( tooltipText );
	}
}
