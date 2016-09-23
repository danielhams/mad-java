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

package uk.co.modularaudio.mads.base.controllertocv.ui;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import uk.co.modularaudio.mads.base.controllertocv.mu.ControllerToCvMadDefinition;
import uk.co.modularaudio.mads.base.controllertocv.mu.ControllerToCvMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCRotaryChoice;

public class ControllerToCvControlChoiceUiJComponent
implements IMadUiControlInstance<ControllerToCvMadDefinition, ControllerToCvMadInstance, ControllerToCvMadUiInstance>
{
//	private static Log log = LogFactory.getLog( ControllerToCvControlChoiceUiJComponent.class.getName() );

	private final DefaultComboBoxModel<String> model;
	private final LWTCRotaryChoice rotaryChoice;

	public enum NoteControl
	{
		ALL( "All Cntls", -1 ),
		CONTROL_0("Cntl 0", 0),
		CONTROL_1("Cntl 1", 1),
		CONTROL_2("Cntl 2", 2),
		CONTROL_3("Cntl 3", 3),
		CONTROL_4("Cntl 4", 4),
		CONTROL_5("Cntl 5", 5),
		CONTROL_6("Cntl 6", 6),
		CONTROL_7("Cntl 7", 7),
		CONTROL_8("Cntl 8", 8),
		CONTROL_9("Cntl 9", 9),
		CONTROL_10("Cntl 10", 10),
		CONTROL_11("Cntl 11", 11),
		CONTROL_12("Cntl 12", 12),
		CONTROL_13("Cntl 13", 13),
		CONTROL_14("Cntl 14", 14),
		CONTROL_15("Cntl 15", 15),
		CONTROL_16("Cntl 16", 16),
		CONTROL_17("Cntl 17", 17),
		CONTROL_18("Cntl 18", 18),
		CONTROL_19("Cntl 19", 19),
		CONTROL_20("Cntl 20", 20),
		CONTROL_21("Cntl 21", 21),
		CONTROL_22("Cntl 22", 22),
		CONTROL_23("Cntl 23", 23),
		CONTROL_24("Cntl 24", 24),
		CONTROL_25("Cntl 25", 25),
		CONTROL_26("Cntl 26", 26),
		CONTROL_27("Cntl 27", 27),
		CONTROL_28("Cntl 28", 28),
		CONTROL_29("Cntl 29", 29),
		CONTROL_30("Cntl 30", 30),
		CONTROL_31("Cntl 31", 31),
		CONTROL_32("Cntl 32", 32),
		CONTROL_33("Cntl 33", 33),
		CONTROL_34("Cntl 34", 34),
		CONTROL_35("Cntl 35", 35),
		CONTROL_36("Cntl 36", 36),
		CONTROL_37("Cntl 37", 37),
		CONTROL_38("Cntl 38", 38),
		CONTROL_39("Cntl 39", 39),
		CONTROL_40("Cntl 40", 40),
		CONTROL_41("Cntl 41", 41),
		CONTROL_42("Cntl 42", 42),
		CONTROL_43("Cntl 43", 43),
		CONTROL_44("Cntl 44", 44),
		CONTROL_45("Cntl 45", 45),
		CONTROL_46("Cntl 46", 46),
		CONTROL_47("Cntl 47", 47),
		CONTROL_48("Cntl 48", 48),
		CONTROL_49("Cntl 49", 49),
		CONTROL_50("Cntl 50", 50),
		CONTROL_51("Cntl 51", 51),
		CONTROL_52("Cntl 52", 52),
		CONTROL_53("Cntl 53", 53),
		CONTROL_54("Cntl 54", 54),
		CONTROL_55("Cntl 55", 55),
		CONTROL_56("Cntl 56", 56),
		CONTROL_57("Cntl 57", 57),
		CONTROL_58("Cntl 58", 58),
		CONTROL_59("Cntl 59", 59),
		CONTROL_60("Cntl 60", 60),
		CONTROL_61("Cntl 61", 61),
		CONTROL_62("Cntl 62", 62),
		CONTROL_63("Cntl 63", 63),
		CONTROL_64("Cntl 64", 64),
		CONTROL_65("Cntl 65", 65),
		CONTROL_66("Cntl 66", 66),
		CONTROL_67("Cntl 67", 67),
		CONTROL_68("Cntl 68", 68),
		CONTROL_69("Cntl 69", 69),
		CONTROL_70("Cntl 70", 70),
		CONTROL_71("Cntl 71", 71),
		CONTROL_72("Cntl 72", 72),
		CONTROL_73("Cntl 73", 73),
		CONTROL_74("Cntl 74", 74),
		CONTROL_75("Cntl 75", 75),
		CONTROL_76("Cntl 76", 76),
		CONTROL_77("Cntl 77", 77),
		CONTROL_78("Cntl 78", 78),
		CONTROL_79("Cntl 79", 79),
		CONTROL_80("Cntl 80", 80),
		CONTROL_81("Cntl 81", 81),
		CONTROL_82("Cntl 82", 82),
		CONTROL_83("Cntl 83", 83),
		CONTROL_84("Cntl 84", 84),
		CONTROL_85("Cntl 85", 85),
		CONTROL_86("Cntl 86", 86),
		CONTROL_87("Cntl 87", 87),
		CONTROL_88("Cntl 88", 88),
		CONTROL_89("Cntl 89", 89),
		CONTROL_90("Cntl 90", 90),
		CONTROL_91("Cntl 91", 91),
		CONTROL_92("Cntl 92", 92),
		CONTROL_93("Cntl 93", 93),
		CONTROL_94("Cntl 94", 94),
		CONTROL_95("Cntl 95", 95),
		CONTROL_96("Cntl 96", 96),
		CONTROL_97("Cntl 97", 97),
		CONTROL_98("Cntl 98", 98),
		CONTROL_99("Cntl 99", 99),
		CONTROL_100("Cntl 100", 100),
		CONTROL_101("Cntl 101", 101),
		CONTROL_102("Cntl 102", 102),
		CONTROL_103("Cntl 103", 103),
		CONTROL_104("Cntl 104", 104),
		CONTROL_105("Cntl 105", 105),
		CONTROL_106("Cntl 106", 106),
		CONTROL_107("Cntl 107", 107),
		CONTROL_108("Cntl 108", 108),
		CONTROL_109("Cntl 109", 109),
		CONTROL_110("Cntl 110", 110),
		CONTROL_111("Cntl 111", 111),
		CONTROL_112("Cntl 112", 112),
		CONTROL_113("Cntl 113", 113),
		CONTROL_114("Cntl 114", 114),
		CONTROL_115("Cntl 115", 115),
		CONTROL_116("Cntl 116", 116),
		CONTROL_117("Cntl 117", 117),
		CONTROL_118("Cntl 118", 118),
		CONTROL_119("Cntl 119", 119),
		CONTROL_120("Cntl 120", 120),
		CONTROL_121("Cntl 121", 121),
		CONTROL_122("Cntl 122", 122),
		CONTROL_123("Cntl 123", 123),
		CONTROL_124("Cntl 124", 124),
		CONTROL_125("Cntl 125", 125),
		CONTROL_126("Cntl 126", 126),
		CONTROL_127("Cntl 127", 127);

		private String label;
		private int controlNum;

		private NoteControl( final String label, final int controlNum )
		{
			this.label = label;
			this.controlNum = controlNum;
		}

		public String getLabel()
		{
			return label;
		}

		public int getControlNum()
		{
			return controlNum;
		}
	};

	public final static Map<String, NoteControl> NOTE_CONTROL_LABEL_TO_ENUM = buildLabelToEnumMap();

	private static Map<String, NoteControl> buildLabelToEnumMap()
	{
		final Map<String, NoteControl> retVal = new HashMap<>();
		for( final NoteControl nc : NoteControl.values() )
		{
			retVal.put( nc.getLabel(), nc );
		}
		return retVal;
	}

	public final static NoteControl DEFAULT_CONTROL = NoteControl.ALL;

	public ControllerToCvControlChoiceUiJComponent(
			final ControllerToCvMadDefinition definition,
			final ControllerToCvMadInstance instance,
			final ControllerToCvMadUiInstance uiInstance,
			final int controlIndex )
	{

		model = new DefaultComboBoxModel<>();
		for( final NoteControl nc : NoteControl.values() )
		{
			model.addElement( nc.getLabel() );
		}

		model.setSelectedItem( DEFAULT_CONTROL.getLabel() );

		rotaryChoice = new LWTCRotaryChoice( LWTCControlConstants.STD_ROTARY_CHOICE_COLOURS, model, false );

		model.addListDataListener( new ListDataListener()
		{

			@Override
			public void intervalRemoved( final ListDataEvent e )
			{
			}

			@Override
			public void intervalAdded( final ListDataEvent e )
			{
			}

			@Override
			public void contentsChanged( final ListDataEvent e )
			{
				final String val = (String)model.getSelectedItem();
				final NoteControl nc = NOTE_CONTROL_LABEL_TO_ENUM.get( val );
				if( nc != null )
				{
					uiInstance.sendSelectedController( nc.getControlNum() );
				}
			}
		} );

		uiInstance.addLearnListener( new ControllerToCvLearnListener()
		{

			@Override
			public void receiveLearntController( final int channel, final int controller )
			{
				final String controllerElement = model.getElementAt( controller + 1 );
				model.setSelectedItem( controllerElement );
			}
		} );
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}

	@Override
	public String getControlValue()
	{
		return (String)model.getSelectedItem();
	}

	@Override
	public void receiveControlValue( final String value )
	{
		model.setSelectedItem( value );
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage ,
			final MadTimingParameters timingParameters ,
			final int U_currentGuiTime , int framesSinceLastTick  )
	{
	}

	@Override
	public Component getControl()
	{
		return rotaryChoice;
	}

	@Override
	public void destroy()
	{
	}
}
