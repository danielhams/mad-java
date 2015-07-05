package uk.co.modularaudio.mads.base.spectralamp.ui;

import java.awt.Component;

import javax.swing.JPanel;

import uk.co.modularaudio.mads.base.spectralamp.mu.SpectralAmpMadDefinition;
import uk.co.modularaudio.mads.base.spectralamp.mu.SpectralAmpMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class NewPeakAndScalesDisplay extends JPanel
	implements IMadUiControlInstance<SpectralAmpMadDefinition, SpectralAmpMadInstance, SpectralAmpMadUiInstance>
{
	private static final long serialVersionUID = -734000347965308505L;

	private final NewAmpScaleLabels ampScaleLabels;
	private final NewEmptyPlot topEmptyPlot;
	private final NewAmpAxisMarks ampAxisMarks;
	private final NewSpectralDisplay spectralDispaly;
	private final NewEmptyPlot rightEmptyPlot;
	private final NewFreqAxisMarks freqAxisMarks;
	private final NewFreqScaleLabels freqScaleLabels;

	public static final int AXIS_MARKS_LENGTH = 8;
	public static final int AMP_LABELS_WIDTH = 36;
	public static final int FREQ_LABELS_HEIGHT = 16;
	public static final int SPECTRAL_DISPLAY_RIGHT_PADDING = 16;
	public static final int SPECTRAL_DISPLAY_TOP_PADDING = 8;

	public static final int NUM_FREQ_MARKERS = 9;
	public static final int NUM_AMP_MARKERS = 5;

	public NewPeakAndScalesDisplay( final SpectralAmpMadDefinition definition,
			final SpectralAmpMadInstance instance,
			final SpectralAmpMadUiInstance uiInstance,
			final int controlIndex )
	{
		setOpaque( true );
		setBackground( SpectralAmpColours.BACKGROUND_COLOR );
		final MigLayoutStringHelper msh = new MigLayoutStringHelper();

//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "insets 0" );
		msh.addLayoutConstraint( "gap 0" );
		msh.addLayoutConstraint( "fill" );

		msh.addColumnConstraint(
				"[" + AMP_LABELS_WIDTH + "px]" +
				"[" + AXIS_MARKS_LENGTH + "px]" +
				"[]" +
				"[" + SPECTRAL_DISPLAY_RIGHT_PADDING + "px]" );

		msh.addRowConstraint(
				"[" + SPECTRAL_DISPLAY_TOP_PADDING + "px]" +
				"[]" +
				"[" + AXIS_MARKS_LENGTH + "px]" +
				"[" + FREQ_LABELS_HEIGHT + "px]" );

		setLayout( msh.createMigLayout() );


		ampScaleLabels = new NewAmpScaleLabels( uiInstance );
		topEmptyPlot = new NewEmptyPlot();
		ampAxisMarks = new NewAmpAxisMarks();
		spectralDispaly = new NewSpectralDisplay( uiInstance );
		rightEmptyPlot = new NewEmptyPlot();
		freqScaleLabels = new NewFreqScaleLabels( uiInstance );
		freqAxisMarks = new NewFreqAxisMarks();

		// Sizing set using row/column constraints
		this.add( ampScaleLabels, "cell 0 0, spany 3, growy" );
		this.add( topEmptyPlot, "cell 1 0, spanx 3, growx" );
		this.add( ampAxisMarks, "cell 1 1, grow" );
		this.add( spectralDispaly, "cell 2 1, grow, push" );
		this.add( rightEmptyPlot, "cell 3 1, grow" );
		this.add( freqAxisMarks, "cell 1 2, spanx 3, growx" );
		this.add( freqScaleLabels, "cell 0 3, spanx 4, growx" );
	}

	@Override
	public Component getControl()
	{
		return this;
	}

	@Override
	public String getControlValue()
	{
		return "";
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return true;
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime )
	{
		spectralDispaly.doDisplayProcessing( tempEventStorage, timingParameters, currentGuiTime );
	}

	@Override
	public void receiveControlValue( final String value )
	{
	}

	static public int getAdjustedWidthOfDisplay( final int actualWidth )
	{
		return getAdjustedWidthBetweenMarkers( actualWidth ) * (NUM_FREQ_MARKERS-1);
	}

	static public int getAdjustedWidthBetweenMarkers( final int actualWidth )
	{
		return (int)Math.floor(actualWidth / (NUM_FREQ_MARKERS-1));
	}

	static public int getAdjustedHeightOfDisplay( final int actualHeight )
	{
		return getAdjustedHeightBetweenMarkers( actualHeight ) * (NUM_AMP_MARKERS-1);
	}

	static public int getAdjustedHeightBetweenMarkers( final int actualHeight )
	{
		return (int)Math.floor(actualHeight / (NUM_AMP_MARKERS-1));
	}
}
