package uk.co.modularaudio.mads.base.scope.ui.display;

import javax.swing.JPanel;

import uk.co.modularaudio.mads.base.scope.ui.ScopeColours;
import uk.co.modularaudio.util.swing.colouredtoggle.ColouredTextToggle;
import uk.co.modularaudio.util.swing.colouredtoggle.ToggleReceiver;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class ScopeBottomSignalToggles extends JPanel
{
	private static final long serialVersionUID = 7694531221972166980L;

//	private static Log log = LogFactory.getLog( ScopeBottomSignalToggles.class.getName() );

	private final ColouredTextToggle signal0Toggle;
	private final ColouredTextToggle signal1Toggle;
	private final ColouredTextToggle signal2Toggle;
	private final ColouredTextToggle signal3Toggle;

	public ScopeBottomSignalToggles( final ToggleReceiver toggleReceiver )
	{
		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "fill" );
		msh.addLayoutConstraint( "gap 10" );
		msh.addLayoutConstraint( "insets 0" );

		setLayout( msh.createMigLayout() );

		setBackground( ScopeColours.BACKGROUND_COLOR );
		setOpaque( true );

		signal0Toggle = new ColouredTextToggle(
				"Signal 1",
				"Click to toggle display of signal 1",
				ScopeColours.BACKGROUND_COLOR,
				ScopeWaveDisplay.VIS_COLOURS[1],
				true,
				toggleReceiver,
				1 );
		signal1Toggle = new ColouredTextToggle(
				"Signal 2",
				"Click to toggle display of signal 2",
				ScopeColours.BACKGROUND_COLOR,
				ScopeWaveDisplay.VIS_COLOURS[2],
				true,
				toggleReceiver,
				2 );
		signal2Toggle = new ColouredTextToggle(
				"Signal 3",
				"Click to toggle display of signal 3",
				ScopeColours.BACKGROUND_COLOR,
				ScopeWaveDisplay.VIS_COLOURS[3],
				true,
				toggleReceiver,
				3 );
		signal3Toggle = new ColouredTextToggle(
				"Signal 4",
				"Click to toggle display of signal 4",
				ScopeColours.BACKGROUND_COLOR,
				ScopeWaveDisplay.VIS_COLOURS[4],
				true,
				toggleReceiver,
				4 );

		this.add( signal0Toggle, "grow, shrink 0" );
		this.add( signal1Toggle, "grow, shrink 0" );
		this.add( signal2Toggle, "grow, shrink 0" );
		this.add( signal3Toggle, "grow, shrink 0" );
	}

}
