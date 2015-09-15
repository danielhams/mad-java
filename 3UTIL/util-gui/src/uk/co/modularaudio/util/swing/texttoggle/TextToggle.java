package uk.co.modularaudio.util.swing.texttoggle;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCLabel;
import uk.co.modularaudio.util.swing.toggle.ToggleReceiver;

public class TextToggle extends JPanel
{
	private static final long serialVersionUID = -7644175161110078571L;

//	private static Log log = LogFactory.getLog( TextToggle.class.getName() );

	private final Color activeTextColor;
	private final Color inactiveTextColor;

	private final LWTCLabel selectedLabel;
	private final LWTCLabel unselectedLabel;

	private final TextToggleMouseListener mouseListener;

	private final ToggleReceiver toggleReceiver;

	private final int toggleId;
	private boolean isSelected;

	public TextToggle( final String selectedText,
			final String unselectedText,
			final Color activeTextColor,
			final Color inactiveTextColor,
			final Color backgroundColor,
			final Color borderColor,
			final boolean startSelected,
			final boolean isOpaque,
			final ToggleReceiver toggleReceiver,
			final int toggleId)
	{
		this.activeTextColor = activeTextColor;
		this.inactiveTextColor = inactiveTextColor;

		this.isSelected = startSelected;

		this.toggleReceiver = toggleReceiver;
		this.toggleId = toggleId;

		this.setOpaque( isOpaque );

		this.setBorder( BorderFactory.createLineBorder( borderColor, 1, false ) );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "fill" );
		msh.addLayoutConstraint( "gap 0" );
		msh.addLayoutConstraint( "insets 0" );

		this.setLayout( msh.createMigLayout() );

		selectedLabel = new LWTCLabel( selectedText );
		selectedLabel.setFont( LWTCControlConstants.LABEL_FONT );
		selectedLabel.setBackground( backgroundColor );
		selectedLabel.setOpaque( isOpaque );
		selectedLabel.setHorizontalAlignment( JLabel.CENTER );

		this.add( selectedLabel, "grow" );

		unselectedLabel = new LWTCLabel( unselectedText );
		unselectedLabel.setFont( LWTCControlConstants.LABEL_FONT );
		unselectedLabel.setBackground( backgroundColor );
		unselectedLabel.setOpaque( isOpaque );
		unselectedLabel.setHorizontalAlignment( JLabel.CENTER );

		this.add( unselectedLabel, "grow" );

		setTextColors();

		mouseListener = new TextToggleMouseListener( this );

		this.addMouseListener( mouseListener );


	}

	private final void setTextColors()
	{
		if( isSelected )
		{
			selectedLabel.setForeground( activeTextColor );
			unselectedLabel.setForeground( inactiveTextColor );
		}
		else
		{
			selectedLabel.setForeground( inactiveTextColor );
			unselectedLabel.setForeground( activeTextColor );
		}
	}

	public void receiveClick()
	{
		isSelected = !isSelected;
		setTextColors();
		toggleReceiver.receiveToggle( toggleId, isSelected );
		repaint();
	}

	public String getControlValue()
	{
		return Boolean.toString( isSelected );
	}

	public void receiveControlValue( final String value )
	{
		isSelected = Boolean.parseBoolean( value );
		setTextColors();
		toggleReceiver.receiveToggle( toggleId, isSelected );
		repaint();
	}
}
