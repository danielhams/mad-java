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

package uk.co.modularaudio.mads.base.pattern_sequencer.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JComponent;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.pattern_sequencer.mu.PatternSequencerMadDefinition;
import uk.co.modularaudio.mads.base.pattern_sequencer.mu.PatternSequencerMadInstance;
import uk.co.modularaudio.mads.base.pattern_sequencer.mu.PatternSequencerRuntimePattern;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacPanel;
import uk.co.modularaudio.util.audio.gui.patternsequencer.PatternSequencer;
import uk.co.modularaudio.util.audio.gui.patternsequencer.model.PatternSequenceModel;
import uk.co.modularaudio.util.audio.gui.patternsequencer.model.PatternSequenceModelListener;
import uk.co.modularaudio.util.audio.gui.patternsequencer.model.PatternSequenceStep;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class PatternSequencerDisplayUiJComponent extends PacPanel
	implements IMadUiControlInstance<PatternSequencerMadDefinition, PatternSequencerMadInstance, PatternSequencerMadUiInstance>,
		PatternSequenceModelListener
{
	private static final long serialVersionUID = 3104323903382855613L;


	private static Log log = LogFactory.getLog(  PatternSequencerDisplayUiJComponent.class.getName( ) );


	private final PatternSequencerMadInstance instance;

	// Stuff for our processing
	private final PatternSequencer patternSequencer;
	private final PatternSequenceModel dataModel;

	private int curPattern;
	private final PatternSequencerRuntimePattern[] runtimePatterns = new PatternSequencerRuntimePattern[2];

	public PatternSequencerDisplayUiJComponent(
			final PatternSequencerMadDefinition definition,
			final PatternSequencerMadInstance instance,
			final PatternSequencerMadUiInstance uiInstance,
			final int controlIndex )
	{
		final MigLayout migLayout = new MigLayout("insets 0, fill");
		this.setLayout( migLayout );
		this.setOpaque( true );
		this.instance = instance;

		dataModel = instance.getPatternDataModel();

		dataModel.addListener( this );
		final Dimension blockDimensions = new Dimension( 15, 8 );

		final float backLevel = 0.3f;
		final Color backgroundColour = new Color( backLevel, backLevel, backLevel );
		final Color gridColour = new Color( 0.5f, backLevel, backLevel );
		final Color blockColour = new Color( 0.95f, 0.2f, 0.2f );

		patternSequencer = new PatternSequencer( dataModel, blockDimensions, backgroundColour, gridColour, blockColour );
		this.add( patternSequencer, "grow" );

		runtimePatterns[ 0 ] = new PatternSequencerRuntimePattern( dataModel.getNumSteps() );
		runtimePatterns[ 1 ] = new PatternSequencerRuntimePattern( dataModel.getNumSteps() );
		log.debug("Done with pattern sequencer display component creation");
	}

	@Override
	public void doDisplayProcessing(final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		if( isShowing() )
		{
		}
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	@Override
	public String getControlValue()
	{
		// Turn the current sequence into a string for persisting
		return dataModel.toPersistenceString();
	}

	@Override
	public void receiveControlValue( final String value )
	{
		// Pass the string to the pattern for use.
		dataModel.initFromPersistenceString( value );
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

	private void sendPatternUpdate()
	{
//		log.debug( "Received update to pattern" );
		final AtomicReference<PatternSequencerRuntimePattern> atomicRuntimePattern = instance.getAtomicRuntimePattern();
		final PatternSequencerRuntimePattern curInstancePattern = atomicRuntimePattern.get();
		if( curInstancePattern != null )
		{
//			log.debug("Atomic is " + curInstancePattern.toString() );
		}
		else
		{
//			log.debug("Atomic is currently null");
		}
		// Wait for the current pattern to become used
		final long startTime = System.currentTimeMillis();
		boolean done = false;
		while( !done )
		{
			if( curInstancePattern != null && curInstancePattern.used == true )
			{
				done = true;
			}
			else
			{
				try
				{
//					log.debug("Waiting for pattern to become used");
					Thread.sleep( 10, 0 );
				}
				catch (final InterruptedException e)
				{
				}
				final long curTime = System.currentTimeMillis();
				if( curTime >= startTime + 20)
				{
					done = true;
				}
			}
		}
		if( curInstancePattern == null || curInstancePattern.used == true )
		{
			// It's already been used copy in a new one
			final PatternSequencerRuntimePattern patternToPush = runtimePatterns[ curPattern ];
//			log.debug("Will push " + curPattern + " " + patternToPush.toString());
			patternToPush.used = false;
			final int numSteps = dataModel.getNumSteps();
			if( patternToPush.numSteps != numSteps )
			{
				patternToPush.stepNotes = new PatternSequenceStep[ numSteps ];
				patternToPush.numSteps = numSteps;
			}

			patternToPush.copyFrom( dataModel );

			atomicRuntimePattern.set( patternToPush );

			curPattern = ( curPattern == 0 ? 1 : 0 );
//			log.debug("Now set curPattern to " + curPattern );
		}
	}

	@Override
	public void receiveStepNoteAndAmpChange( final int firstStep, final int lastStep )
	{
		sendPatternUpdate();
	}

	@Override
	public void receiveStepnoteChange( final int firstStep, final int lastStep )
	{
		sendPatternUpdate();
	}

	@Override
	public void receiveStepAmpChange( final int firstStep, final int lastStep )
	{
		sendPatternUpdate();
	}
}
