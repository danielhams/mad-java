package uk.co.modularaudio.mads.base.scope.ui;

import java.awt.Component;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Base64Utils;

import uk.co.modularaudio.mads.base.scope.mu.ScopeMadDefinition;
import uk.co.modularaudio.mads.base.scope.mu.ScopeMadInstance;
import uk.co.modularaudio.mads.base.scope.ui.display.ScopeAmpLabels;
import uk.co.modularaudio.mads.base.scope.ui.display.ScopeAmpMarks;
import uk.co.modularaudio.mads.base.scope.ui.display.ScopeBottomSignalToggles;
import uk.co.modularaudio.mads.base.scope.ui.display.ScopeEmptyPlot;
import uk.co.modularaudio.mads.base.scope.ui.display.ScopeTimeLabels;
import uk.co.modularaudio.mads.base.scope.ui.display.ScopeTimeMarks;
import uk.co.modularaudio.mads.base.scope.ui.display.ScopeTopTriggerToggle;
import uk.co.modularaudio.mads.base.scope.ui.display.ScopeWaveDisplay;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.colouredtoggle.ToggleReceiver;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.xml.XmlCharacterEncoder;

public class ScopeDisplayUiJComponent extends JPanel
implements IMadUiControlInstance<ScopeMadDefinition, ScopeMadInstance, ScopeMadUiInstance>, ToggleReceiver
{
	private static final long serialVersionUID = 5515402437483693770L;

	private static Log log = LogFactory.getLog( ScopeDisplayUiJComponent.class.getName() );

	public static final int AMP_LABELS_WIDTH = 25;
	public static final int AXIS_MARKS_LENGTH = 6;
	public static final int AMP_DISPLAY_RIGHT_PADDING = 14;
	public static final int AMP_DISPLAY_TOP_PADDING = 24;
	public static final int TIME_LABELS_HEIGHT = 12;
	public static final int TIME_DISPLAY_BOTTOM_PADDING = AMP_DISPLAY_TOP_PADDING;

	private static final int NUM_AMP_MARKS = 7;
	private static final int NUM_TIME_MARKS = 11;

	private final ScopeAmpLabels ampLabels;
	private final ScopeTopTriggerToggle topTriggerToggle;
	private final ScopeAmpMarks ampMarks;
	private final ScopeWaveDisplay waveDisplay;
	private final ScopeEmptyPlot rightEmptyPlot;
	private final ScopeTimeMarks timeMarks;
	private final ScopeTimeLabels timeLabels;
	private final ScopeBottomSignalToggles bottomSignalToggles;

	public ScopeDisplayUiJComponent( final ScopeMadDefinition definition,
			final ScopeMadInstance instance,
			final ScopeMadUiInstance uiInstance,
			final int controlIndex )
	{
		setOpaque( true );
		setBackground( ScopeColours.BACKGROUND_COLOR );
		final MigLayoutStringHelper msh = new MigLayoutStringHelper();

//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "insets 5" );
		msh.addLayoutConstraint( "gap 0" );
		msh.addLayoutConstraint( "fill" );

		msh.addColumnConstraint(
				"[" + AMP_LABELS_WIDTH + "px]" +
				"[" + AXIS_MARKS_LENGTH + "px]" +
				"[]" +
				"[" + AMP_DISPLAY_RIGHT_PADDING + "px]" );

		msh.addRowConstraint(
				"[" + AMP_DISPLAY_TOP_PADDING + "px]" +
				"[]" +
				"[" + AXIS_MARKS_LENGTH + "px]" +
				"[" + TIME_LABELS_HEIGHT + "px]" +
				"[" + TIME_DISPLAY_BOTTOM_PADDING + "px]" );

		setLayout( msh.createMigLayout() );

		ampLabels = new ScopeAmpLabels( uiInstance, NUM_AMP_MARKS );
		topTriggerToggle = new ScopeTopTriggerToggle( this );
		ampMarks = new ScopeAmpMarks( NUM_AMP_MARKS );
		waveDisplay = new ScopeWaveDisplay( uiInstance, NUM_TIME_MARKS, NUM_AMP_MARKS );
		rightEmptyPlot = new ScopeEmptyPlot();
		timeMarks = new ScopeTimeMarks( NUM_TIME_MARKS );
		timeLabels = new ScopeTimeLabels( uiInstance, NUM_TIME_MARKS );
		bottomSignalToggles = new ScopeBottomSignalToggles( this );

		this.add( ampLabels, "cell 0 0, spany 3, growy" );
		this.add( topTriggerToggle, "cell 1 0, spanx 3, center" );
		this.add( ampMarks, "cell 1 1, grow" );
		this.add( waveDisplay, "cell 2 1, grow, push" );
		this.add( rightEmptyPlot, "cell 3 1, grow" );
		this.add( timeMarks, "cell 1 2, spanx 3, growx" );
		this.add( timeLabels, "cell 0 3, spanx 4, growx" );
		this.add( bottomSignalToggles, "cell 0 4, spanx 4, center, growx" );
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return true;
	}

	@Override
	public String getControlValue()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( topTriggerToggle.getControlValue() );
		sb.append( '|' );
		sb.append( bottomSignalToggles.getControlValue( 0 ) );
		sb.append( '|' );
		sb.append( bottomSignalToggles.getControlValue( 1 ) );
		sb.append( '|' );
		sb.append( bottomSignalToggles.getControlValue( 2 ) );
		sb.append( '|' );
		sb.append( bottomSignalToggles.getControlValue( 3 ) );
		return sb.toString();
	}

	@Override
	public void receiveControlValue( final String value )
	{
		final String[] vals = value.split("|");
		if( vals.length == 5 )
		{
			topTriggerToggle.receiveControlValue( vals[0] );

			bottomSignalToggles.receiveControlValue(
					vals[1],
					vals[2],
					vals[3],
					vals[4] );
		}
		else
		{
			log.error("Failed to obtain number of expected init params. Expected 5 got " + vals.length );
		}
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters, final long currentGuiTime )
	{
		waveDisplay.doDisplayProcessing( tempEventStorage, timingParameters, currentGuiTime );
	}

	@Override
	public Component getControl()
	{
		return this;
	}

	@Override
	public void destroy()
	{
	}

	public static int getAdjustedHeightOfDisplay( final int height, final int numAmpMarkers )
	{
		return getAdjustedHeightBetweenMarkers( height, numAmpMarkers ) * (numAmpMarkers-1);
	}

	static public int getAdjustedHeightBetweenMarkers( final int height, final int numAmpMarkers )
	{
		return (int)Math.floor(height / (numAmpMarkers-1));
	}

	static public int getAdjustedWidthOfDisplay( final int width, final int numFreqMarkers )
	{
		return getAdjustedWidthBetweenMarkers( width, numFreqMarkers ) * (numFreqMarkers-1);
	}

	static public int getAdjustedWidthBetweenMarkers( final int width, final int numFreqMarkers )
	{
		return (int)Math.floor(width / (numFreqMarkers-1));
	}

	@Override
	public void receiveToggle( final int toggleId, final boolean active )
	{
		log.trace("Received toggle of " + toggleId + " to " + active );
		waveDisplay.setSignalVisibility( toggleId, active );
	}
}
