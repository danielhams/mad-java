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

package test.uk.co.modularaudio.mads.visualisation.base;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UnsupportedLookAndFeelException;

import junit.framework.TestCase;
import net.miginfocom.swing.MigLayout;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import test.uk.co.modularaudio.mads.visualisation.base.genericsetup.GenericComponentVisualiser;
import uk.co.modularaudio.mads.base.mixer.mu.MixerMadDefinition;
import uk.co.modularaudio.mads.base.mixer.mu.MixerMadInstance;
import uk.co.modularaudio.mads.base.mixer.ui.MixerMadUiInstance;
import uk.co.modularaudio.mads.base.mixer.ui.ChannelLaneMixerPanelUiInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class TestCreateMixerLane extends TestCase
{
	private static Log log = LogFactory.getLog( TestCreateMixerLane.class.getName() );

	private GenericComponentVisualiser gcv;

	public TestCreateMixerLane() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		gcv = new GenericComponentVisualiser();
	}

	@Override
	protected void setUp() throws Exception
	{
		gcv.setUp();
	}

	@Override
	protected void tearDown() throws Exception
	{
		gcv.tearDown();
	}

	public void testAndShowComponent()
			throws Exception
	{
		MixerMadDefinition mixerDef = (MixerMadDefinition)gcv.componentService.findDefinitionById( MixerMadDefinition.DEFINITION_ID );
		Map<MadParameterDefinition, String> parameterValues = new HashMap<MadParameterDefinition, String>();
		String instanceName = "panel_test";
		MixerMadInstance mixerAui = (MixerMadInstance)gcv.componentService.createInstanceFromDefinition( mixerDef, parameterValues, instanceName );

		MixerMadUiInstance mixerUi =
				(MixerMadUiInstance)gcv.componentUiService.createUiInstanceForInstance( mixerAui );

		JFrame testFrame = new JFrame();
		JPanel testPanel = new JPanel();
		MigLayout layout = new MigLayout("insets 0, gap 0, fill");
		testPanel.setLayout( layout );
		testFrame.add( testPanel );
//		Dimension minimumSize = new Dimension(71, 165);
//		testPanel.setMinimumSize( minimumSize );
//		testPanel.setMaximumSize( minimumSize );
//		testPanel.setPreferredSize( minimumSize );

		ChannelLaneMixerPanelUiInstance lanePanel = new ChannelLaneMixerPanelUiInstance( mixerDef, mixerAui, mixerUi, 0 );
		testPanel.add( lanePanel, "grow" );
		testPanel.setBackground( GenericComponentVisualiser.panelBackgroundColor );

		testFrame.addComponentListener( new ComponentListener()
		{

			@Override
			public void componentShown( ComponentEvent e )
			{
			}

			@Override
			public void componentResized( ComponentEvent e )
			{
				Object o = e.getSource();
				JFrame frame = (JFrame)o;
				log.debug("Component resized to be " + frame.getSize() );
			}

			@Override
			public void componentMoved( ComponentEvent e )
			{
			}

			@Override
			public void componentHidden( ComponentEvent e )
			{
			}
		} );

		testPanel.validate();

		testFrame.pack();
		testFrame.setVisible( true );

		// Set some values
		long currentGuiTime = System.nanoTime();
//		long nanosPerPeriod = 0;
		MadTimingParameters timingParameters = new MadTimingParameters( 100, 100, 100, 100, 100 );
		ThreadSpecificTemporaryEventStorage tes = new ThreadSpecificTemporaryEventStorage( 512 );
		lanePanel.receiveMeterReadingLevel( currentGuiTime, 0, 0.8f );
		lanePanel.receiveMeterReadingLevel( currentGuiTime, 1, 0.78f );

		lanePanel.doDisplayProcessing( tes, timingParameters, currentGuiTime );

		while( testFrame.isVisible() )
		{
			Thread.sleep( 100 );
			currentGuiTime += 100;
			lanePanel.receiveMeterReadingLevel( currentGuiTime, 0, 0.8f );
			lanePanel.receiveMeterReadingLevel( currentGuiTime, 1, 0.78f );
			lanePanel.doDisplayProcessing( tes, timingParameters, currentGuiTime );
		}
		testFrame.dispose();
	}

}
