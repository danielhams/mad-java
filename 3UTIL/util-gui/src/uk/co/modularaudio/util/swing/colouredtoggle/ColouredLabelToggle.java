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

package uk.co.modularaudio.util.swing.colouredtoggle;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCLabel;

public class ColouredLabelToggle extends JPanel
{
	private static final long serialVersionUID = -5831939172181474089L;

//	private static Log log = LogFactory.getLog( ColouredLabelToggle.class.getName() );

	private static final int LABEL_BORDER_WIDTH = 1;
	private static final int CLICKABLE_BOX_WIDTH = 20;

	private final Color backgroundColour;
	private final Color surroundColour;

	private final LWTCLabel label;

	private boolean active = false;

	private final ToggleReceiver toggleReceiver;
	private final int toggleId;

	public ColouredLabelToggle( final String defaultText,
			final String tooltipText,
			final Color backgroundColour,
			final Color foregroundColour,
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

		label = new LWTCLabel( defaultText );
		final Dimension origMinSize = label.getMinimumSize();
		final Dimension newMinSize = new Dimension( origMinSize.width, origMinSize.height + 1);
		label.setBorder( BorderFactory.createEmptyBorder( 0, 2, 0, 2 ) );
		label.setMinimumSize( newMinSize );
		label.setOpaque( true );
		label.setBackground( backgroundColour );
		label.setForeground( foregroundColour );
		label.setFont( LWTCControlConstants.LABEL_FONT );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "fill" );
		msh.addLayoutConstraint( "gap 0" );
		// top, left, bottom, right
		msh.addLayoutConstraint( "insets " +
				LABEL_BORDER_WIDTH + " " +
				CLICKABLE_BOX_WIDTH + " " +
				LABEL_BORDER_WIDTH + " " +
				LABEL_BORDER_WIDTH );


		setLayout( msh.createMigLayout() );

		add( label, "growx" );

		this.addMouseListener( new ColouredLabelToggleMouseListener( this ) );
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
		return Boolean.toString( active );
	}

	public void receiveControlValue( final String value )
	{
		active = Boolean.parseBoolean( value );
		toggleReceiver.receiveToggle( toggleId, active );
	}
}
