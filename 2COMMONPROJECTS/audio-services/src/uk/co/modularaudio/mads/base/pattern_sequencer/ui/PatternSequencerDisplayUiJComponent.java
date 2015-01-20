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
import uk.co.modularaudio.util.audio.gui.paccontrols.PacPanel;
import uk.co.modularaudio.util.audio.gui.patternsequencer.PatternSequencer;
import uk.co.modularaudio.util.audio.gui.patternsequencer.model.PatternSequenceModel;
import uk.co.modularaudio.util.audio.gui.patternsequencer.model.PatternSequenceModelListener;
import uk.co.modularaudio.util.audio.gui.patternsequencer.model.PatternSequenceStep;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.thread.NanoTuple;

public class PatternSequencerDisplayUiJComponent extends PacPanel
	implements IMadUiControlInstance<PatternSequencerMadDefinition, PatternSequencerMadInstance, PatternSequencerMadUiInstance>,
		PatternSequenceModelListener
{
	private static final long serialVersionUID = 3104323903382855613L;


	private static Log log = LogFactory.getLog(  PatternSequencerDisplayUiJComponent.class.getName( ) );
	
	
	private PatternSequencerMadInstance instance = null;
	private PatternSequencerMadUiInstance uiInstance = null;

	// Stuff for our processing
	private PatternSequencer patternSequencer = null;
	private PatternSequenceModel dataModel = null;
	
	private int curPattern = 0;
	private PatternSequencerRuntimePattern[] runtimePatterns = new PatternSequencerRuntimePattern[2];
	
	public PatternSequencerDisplayUiJComponent(
			PatternSequencerMadDefinition definition,
			PatternSequencerMadInstance instance,
			PatternSequencerMadUiInstance uiInstance,
			int controlIndex )
	{
		this.uiInstance = uiInstance;
		MigLayout migLayout = new MigLayout("insets 0, fill");
		this.setLayout( migLayout );
		this.setOpaque( true );
		this.instance = instance;
		this.uiInstance = uiInstance;
		
		dataModel = instance.getPatternDataModel();
		
		dataModel.addListener( this );
		Dimension blockDimensions = new Dimension( 15, 8 );

		float backLevel = 0.3f;
		Color backgroundColour = new Color( backLevel, backLevel, backLevel );
		Color gridColour = new Color( 0.5f, backLevel, backLevel );
		Color blockColour = new Color( 0.95f, 0.2f, 0.2f );
		
		patternSequencer = new PatternSequencer( dataModel, blockDimensions, backgroundColour, gridColour, blockColour );
		this.add( patternSequencer, "grow" );
		
		runtimePatterns[ 0 ] = new PatternSequencerRuntimePattern( dataModel.getNumSteps() );
		runtimePatterns[ 1 ] = new PatternSequencerRuntimePattern( dataModel.getNumSteps() );
		log.debug("Done with pattern sequencer display component creation");
	}

	@Override
	public void doDisplayProcessing(ThreadSpecificTemporaryEventStorage tempEventStorage,
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
	public void receiveControlValue( String value )
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
		AtomicReference<PatternSequencerRuntimePattern> atomicRuntimePattern = instance.getAtomicRuntimePattern();
		PatternSequencerRuntimePattern curInstancePattern = atomicRuntimePattern.get();
		if( curInstancePattern != null )
		{
//			log.debug("Atomic is " + curInstancePattern.toString() );
		}
		else
		{
//			log.debug("Atomic is currently null");
		}
		// Wait for the current pattern to become used
		long startTime = System.currentTimeMillis();
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
					NanoTuple nt = new NanoTuple( "PatternUpdateNanoTuple" );
					nt.resetToCurrent();
					nt.addNanos( uiInstance.knownAudioIOLatencyNanos );
					nt.nanoSleepIfNotPassed();
				}
				catch (InterruptedException e)
				{
				}
				long curTime = System.currentTimeMillis();
				if( curTime >= startTime + 20)
				{
					done = true;
				}
			}
		}
		if( curInstancePattern == null || curInstancePattern.used == true )
		{
			// It's already been used copy in a new one
			PatternSequencerRuntimePattern patternToPush = runtimePatterns[ curPattern ];
//			log.debug("Will push " + curPattern + " " + patternToPush.toString());
			patternToPush.used = false;
			int numSteps = dataModel.getNumSteps();
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
	public void receiveStepNoteAndAmpChange( int firstStep, int lastStep )
	{
		sendPatternUpdate();
	}

	@Override
	public void receiveStepnoteChange( int firstStep, int lastStep )
	{
		sendPatternUpdate();
	}

	@Override
	public void receiveStepAmpChange( int firstStep, int lastStep )
	{
		sendPatternUpdate();
	}
}
