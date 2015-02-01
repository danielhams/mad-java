package uk.co.modularaudio.componentdesigner.mainframe.tabbedpane;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.service.gui.ContainerTab;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacButton;

class LabelAndCloseButton extends JPanel
{
	private static final long serialVersionUID = 4749639147954010294L;

	private JLabel titleLabel = null;

	public LabelAndCloseButton( final ContainerTab subrackTab )
	{
		setOpaque( false );
		final MigLayout layout = new MigLayout("insets 0, gap 0");
		this.setLayout( layout );
		titleLabel = new JLabel( subrackTab.getTitle() );
		this.add( titleLabel, "");
		final JButton closeButton = new PacButton()
		{
			private static final long serialVersionUID = 4253160873361081364L;

			@Override
			public void receiveEvent( final ActionEvent e )
			{
				subrackTab.doTabClose();
			}
		};
		final Font f = closeButton.getFont();
		closeButton.setMargin( new Insets( 0, 0, 0, 0 ) );
		closeButton.setFont( f.deriveFont( 9f ) );
		closeButton.setText( "x" );
		final Rectangle bounds = new Rectangle( 0, 0, 40, 12  );
		final Dimension sizeDim = new Dimension( bounds.width, bounds.height );
		closeButton.setMinimumSize( sizeDim );
		closeButton.setMaximumSize( sizeDim );
		this.add( closeButton );
	}

	public void resetTitle( final String newTitle )
	{
		titleLabel.setText( newTitle );
	}
}