package uk.co.modularaudio.util.swing.colouredtoggle;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCTextField;

public class ColouredTextToggle extends JPanel
{
	private static final long serialVersionUID = -5831939172181474089L;

	private static Log log = LogFactory.getLog( ColouredTextToggle.class.getName() );

	private static final int TEXTFIELD_BORDER_WIDTH = 1;
	private static final int CLICKABLE_BOX_WIDTH = 20;

	private final Color backgroundColour;
	private final Color surroundColour;

	private final LWTCTextField textField;

	private boolean active = false;

	private final ToggleReceiver toggleReceiver;
	private final int toggleId;

	public ColouredTextToggle( final String defaultText,
			final String tooltipText,
			final Color backgroundColour,
			final Color surroundColour,
			final boolean isActive,
			final ToggleReceiver toggleReceiver,
			final int toggleId )
	{
		this.backgroundColour = backgroundColour;
		this.surroundColour = surroundColour;
		this.active = isActive;

		this.toggleReceiver = toggleReceiver;
		this.toggleId = toggleId;

		// We set the background to the surround colour as it means
		// we only have to paint the on off bits of the clickable area
		this.setBackground( surroundColour );

		this.setToolTipText( tooltipText );

		textField = new LWTCTextField( LWTCControlConstants.STD_TEXTFIELD_COLOURS );
		textField.setText( defaultText );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "fill" );
		msh.addLayoutConstraint( "gap 0" );
		// top, left, bottom, right
		msh.addLayoutConstraint( "insets " +
				TEXTFIELD_BORDER_WIDTH + " " +
				CLICKABLE_BOX_WIDTH + " " +
				TEXTFIELD_BORDER_WIDTH + " " +
				TEXTFIELD_BORDER_WIDTH );


		setLayout( msh.createMigLayout() );

		add( textField, "growx" );

		this.addMouseListener( new ColouredTextToggleMouseListener( this ) );
	}

	@Override
	public void paint( final Graphics g )
	{
		super.paint( g );
		g.setColor( backgroundColour );

		final int yOffset = Math.round( (getHeight() / 2.0f) - (CLICKABLE_BOX_WIDTH / 2.0f));

		g.fillRect( 4, 4 + yOffset, CLICKABLE_BOX_WIDTH-8, CLICKABLE_BOX_WIDTH-8 );

		if( active )
		{
			// Draw an X inside the box
			g.setColor( surroundColour );
			g.drawLine( 5, 5 + yOffset, CLICKABLE_BOX_WIDTH-6, CLICKABLE_BOX_WIDTH-6 + yOffset);
			g.drawLine( CLICKABLE_BOX_WIDTH - 6, 5 + yOffset, 5, CLICKABLE_BOX_WIDTH-6 + yOffset );
		}
	}

	public void receiveClick()
	{
		active = !active;
		toggleReceiver.receiveToggle( toggleId, active );
		repaint();
	}

	public boolean isActive()
	{
		return active;
	}

	public String getControlValue()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( Boolean.toString( active ) );
		sb.append( "\t" );
		sb.append( textField.getText() );
		return sb.toString();
	}

	public void receiveControlValue( final String value )
	{
		final String[] parts = value.split( "\t" );
		if( parts.length == 2 )
		{
			active = Boolean.parseBoolean( parts[0] );
			toggleReceiver.receiveToggle( toggleId, active );
			textField.setText( parts[1] );
		}
		else
		{
			log.error("Failed to find two parts to value to parse");
		}
	}
}
