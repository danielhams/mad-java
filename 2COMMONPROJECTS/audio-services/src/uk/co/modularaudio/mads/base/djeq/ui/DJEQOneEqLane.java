package uk.co.modularaudio.mads.base.djeq.ui;

import java.awt.Component;

import uk.co.modularaudio.mads.base.djeq.mu.DJEQMadDefinition;
import uk.co.modularaudio.mads.base.djeq.mu.DJEQMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacPanel;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class DJEQOneEqLane extends PacPanel
	implements IMadUiControlInstance<DJEQMadDefinition,DJEQMadInstance,DJEQMadUiInstance>
{
	private static final long serialVersionUID = -112893137724675530L;

//	private static Log log = LogFactory.getLog( DJEQOneEqLane.class.getName());

	private final OneEqKnob knob;

	private final OneEqKill killToggle;

	public DJEQOneEqLane( final DJEQMadDefinition definition,
			final DJEQMadInstance instance,
			final DJEQMadUiInstance uiInstance,
			final int controlIndex,
			final String eqLabel )
	{
		setOpaque( false );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "fill" );
		msh.addLayoutConstraint( "insets 0" );
		msh.addLayoutConstraint( "gap 5" );

		setLayout( msh.createMigLayout() );

		knob = new OneEqKnob( eqLabel );

		add( knob, "grow, wrap" );

		killToggle = new OneEqKill();

		add( killToggle, "alignx center, aligny bottom, width 75px, height 30px" );
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters, final long currentGuiTime )
	{
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

	protected void receiveToggle( final boolean newValue )
	{
	}

	public OneEqKnob getKnob()
	{
		return knob;
	}

	public OneEqKill getKill()
	{
		return killToggle;
	}
}
