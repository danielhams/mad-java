package uk.co.modularaudio.util.swing.colouredtoggle;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;

import uk.co.modularaudio.util.lang.StringUtils;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCLabel;
import uk.co.modularaudio.util.swing.lwtc.LWTCSpeedyUpdateLabel;
import uk.co.modularaudio.util.swing.toggle.ToggleReceiver;

public class ColouredLabelToggleAndText extends ColouredLabelToggle
{
	private static final long serialVersionUID = 4219921137151418787L;

//	private final static Log LOG = LogFactory.getLog( ColouredLabelToggleAndText.class );

//	private final LWTCLabel textLabel;
	private final LWTCSpeedyUpdateLabel textLabel;
	private LWTCLabel suffixLabel = null;

	public ColouredLabelToggleAndText( final String defaultText,
			final String tooltipText,
			final Color backgroundColour,
			final Color foregroundColour,
			final Color surroundColour,
			final boolean isActive,
			final ToggleReceiver toggleReceiver,
			final int toggleId,
			final String suffixText,
			final int minLabelWidth )
	{
		super( defaultText, tooltipText, backgroundColour, foregroundColour, surroundColour, isActive, toggleReceiver,
				toggleId );

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

		msh.addColumnConstraint( "[grow][grow 0][grow 0]" );

		this.removeAll();

		setLayout( msh.createMigLayout() );

		this.add( label, "grow" );

//		textLabel = new LWTCLabel()
//		{
//			@Override
//			public void setText( final String text )
//			{
//		        String oldAccessibleName = null;
//		        if (accessibleContext != null) {
//		            oldAccessibleName = accessibleContext.getAccessibleName();
//		        }
//
//		        final String oldValue = this.text;
//		        this.text = text;
//		        firePropertyChange("text", oldValue, text);
//
//		        if (text == null || oldValue == null || !text.equals(oldValue)) {
//		            revalidate();
//		            repaint();
//		        }
//			}
//	    };
//		textLabel.setBackground( backgroundColour );
//		textLabel.setForeground( foregroundColour );
//		textLabel.setOpaque( true );
//		textLabel.setFont( LWTCControlConstants.LABEL_FONT );
//		textLabel.setBorder( BorderFactory.createEmptyBorder() );
//		textLabel.setHorizontalAlignment( SwingConstants.RIGHT );
//		textLabel.setMaximumSize( new Dimension(minLabelWidth,2048) );

		textLabel = new LWTCSpeedyUpdateLabel();
		textLabel.setBackground( backgroundColour );
		textLabel.setForeground( foregroundColour );
		textLabel.setOpaque( true );
		textLabel.setFont( LWTCControlConstants.LABEL_FONT );
		textLabel.setBorder( BorderFactory.createEmptyBorder() );
//		textLabel.setHorizontalAlignment( SwingConstants.RIGHT );
		textLabel.setMaximumSize( new Dimension(minLabelWidth,2048) );

		this.add( textLabel, "width " + minLabelWidth + ":" + minLabelWidth + ":" + minLabelWidth + ", grow 0, shrink 0" );

		if( !StringUtils.isEmpty(suffixText) )
		{
			suffixLabel = new LWTCLabel( suffixText );
			suffixLabel.setBackground( backgroundColour );
			suffixLabel.setForeground( foregroundColour );
			suffixLabel.setOpaque( true );
			suffixLabel.setFont( LWTCControlConstants.LABEL_FONT );
			suffixLabel.setBorder( BorderFactory.createEmptyBorder(0,1,0,1) );
			suffixLabel.setText( suffixText );

			this.add( suffixLabel, "grow 0" );
		}
	}

	public void setLabelText( final String text )
	{
		textLabel.setText( text );
	}

	@Override
	public void setBounds( final int x, final int y, final int width, final int height )
	{
//		LOG.info("setBounds");
		super.setBounds( x, y, width, height );
	}
}
