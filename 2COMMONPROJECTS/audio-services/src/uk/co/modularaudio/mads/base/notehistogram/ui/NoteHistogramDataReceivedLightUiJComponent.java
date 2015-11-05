package uk.co.modularaudio.mads.base.notehistogram.ui;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import uk.co.modularaudio.mads.base.notehistogram.mu.NoteHistogramMadDefinition;
import uk.co.modularaudio.mads.base.notehistogram.mu.NoteHistogramMadInstance;
import uk.co.modularaudio.mads.base.notehistogram.util.NoteReceivedLight;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCLabel;

public class NoteHistogramDataReceivedLightUiJComponent
	implements IMadUiControlInstance<NoteHistogramMadDefinition, NoteHistogramMadInstance, NoteHistogramMadUiInstance>
{
	private final JPanel lightPanel = new JPanel();
	private final NoteReceivedLight receivedLight = new NoteReceivedLight();

	public NoteHistogramDataReceivedLightUiJComponent(
			final NoteHistogramMadDefinition definition,
			final NoteHistogramMadInstance instance,
			final NoteHistogramMadUiInstance uiInstance,
			final int controlIndex )
	{
		uiInstance.addNoteReceivedListener( receivedLight );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "gap 4" );
		msh.addLayoutConstraint( "insets 2" );
		msh.addLayoutConstraint( "fill" );

		lightPanel.setLayout( msh.createMigLayout() );
		lightPanel.setOpaque( true );
		lightPanel.setBackground( LWTCControlConstants.CONTROL_FLAT_BACKGROUND );

		lightPanel.add( receivedLight, "center" );

		final LWTCLabel label = new LWTCLabel( LWTCControlConstants.STD_LABEL_COLOURS, "Control Moved" );
		label.setFont( LWTCControlConstants.LABEL_FONT );
		label.setBorder( BorderFactory.createEmptyBorder() );
		lightPanel.add( label, "grow" );
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return true;
	}

	@Override
	public String getControlValue()
	{
		return "";
	}

	@Override
	public void receiveControlValue( final String value )
	{
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime )
	{
		receivedLight.doDisplayProcessing( currentGuiTime );
	}

	@Override
	public Component getControl()
	{
		return lightPanel;
	}

	@Override
	public void destroy()
	{
	}
}
