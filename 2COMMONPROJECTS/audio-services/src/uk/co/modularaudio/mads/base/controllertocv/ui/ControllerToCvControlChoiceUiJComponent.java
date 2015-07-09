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
		ALL( "All Controls", -1 ),
		CONTROL_0("Control 0", 0),
		CONTROL_1("Control 1", 1),
		CONTROL_2("Control 2", 2),
		CONTROL_3("Control 3", 3),
		CONTROL_4("Control 4", 4),
		CONTROL_5("Control 5", 5),
		CONTROL_6("Control 6", 6),
		CONTROL_7("Control 7", 7),
		CONTROL_8("Control 8", 8),
		CONTROL_9("Control 9", 9),
		CONTROL_10("Control 10", 10),
		CONTROL_11("Control 11", 11),
		CONTROL_12("Control 12", 12),
		CONTROL_13("Control 13", 13),
		CONTROL_14("Control 14", 14),
		CONTROL_15("Control 15", 15),
		CONTROL_16("Control 16", 16),
		CONTROL_17("Control 17", 17),
		CONTROL_18("Control 18", 18),
		CONTROL_19("Control 19", 19),
		CONTROL_20("Control 20", 20),
		CONTROL_21("Control 21", 21),
		CONTROL_22("Control 22", 22),
		CONTROL_23("Control 23", 23),
		CONTROL_24("Control 24", 24),
		CONTROL_25("Control 25", 25),
		CONTROL_26("Control 26", 26),
		CONTROL_27("Control 27", 27),
		CONTROL_28("Control 28", 28),
		CONTROL_29("Control 29", 29),
		CONTROL_30("Control 30", 30),
		CONTROL_31("Control 31", 31),
		CONTROL_32("Control 32", 32),
		CONTROL_33("Control 33", 33),
		CONTROL_34("Control 34", 34),
		CONTROL_35("Control 35", 35),
		CONTROL_36("Control 36", 36),
		CONTROL_37("Control 37", 37),
		CONTROL_38("Control 38", 38),
		CONTROL_39("Control 39", 39),
		CONTROL_40("Control 40", 40),
		CONTROL_41("Control 41", 41),
		CONTROL_42("Control 42", 42),
		CONTROL_43("Control 43", 43),
		CONTROL_44("Control 44", 44),
		CONTROL_45("Control 45", 45),
		CONTROL_46("Control 46", 46),
		CONTROL_47("Control 47", 47),
		CONTROL_48("Control 48", 48),
		CONTROL_49("Control 49", 49),
		CONTROL_50("Control 50", 50),
		CONTROL_51("Control 51", 51),
		CONTROL_52("Control 52", 52),
		CONTROL_53("Control 53", 53),
		CONTROL_54("Control 54", 54),
		CONTROL_55("Control 55", 55),
		CONTROL_56("Control 56", 56),
		CONTROL_57("Control 57", 57),
		CONTROL_58("Control 58", 58),
		CONTROL_59("Control 59", 59),
		CONTROL_60("Control 60", 60),
		CONTROL_61("Control 61", 61),
		CONTROL_62("Control 62", 62),
		CONTROL_63("Control 63", 63),
		CONTROL_64("Control 64", 64),
		CONTROL_65("Control 65", 65),
		CONTROL_66("Control 66", 66),
		CONTROL_67("Control 67", 67),
		CONTROL_68("Control 68", 68),
		CONTROL_69("Control 69", 69),
		CONTROL_70("Control 70", 70),
		CONTROL_71("Control 71", 71),
		CONTROL_72("Control 72", 72),
		CONTROL_73("Control 73", 73),
		CONTROL_74("Control 74", 74),
		CONTROL_75("Control 75", 75),
		CONTROL_76("Control 76", 76),
		CONTROL_77("Control 77", 77),
		CONTROL_78("Control 78", 78),
		CONTROL_79("Control 79", 79),
		CONTROL_80("Control 80", 80),
		CONTROL_81("Control 81", 81),
		CONTROL_82("Control 82", 82),
		CONTROL_83("Control 83", 83),
		CONTROL_84("Control 84", 84),
		CONTROL_85("Control 85", 85),
		CONTROL_86("Control 86", 86),
		CONTROL_87("Control 87", 87),
		CONTROL_88("Control 88", 88),
		CONTROL_89("Control 89", 89),
		CONTROL_90("Control 90", 90),
		CONTROL_91("Control 91", 91),
		CONTROL_92("Control 92", 92),
		CONTROL_93("Control 93", 93),
		CONTROL_94("Control 94", 94),
		CONTROL_95("Control 95", 95),
		CONTROL_96("Control 96", 96),
		CONTROL_97("Control 97", 97),
		CONTROL_98("Control 98", 98),
		CONTROL_99("Control 99", 99),
		CONTROL_100("Control 100", 100),
		CONTROL_101("Control 101", 101),
		CONTROL_102("Control 102", 102),
		CONTROL_103("Control 103", 103),
		CONTROL_104("Control 104", 104),
		CONTROL_105("Control 105", 105),
		CONTROL_106("Control 106", 106),
		CONTROL_107("Control 107", 107),
		CONTROL_108("Control 108", 108),
		CONTROL_109("Control 109", 109),
		CONTROL_110("Control 110", 110),
		CONTROL_111("Control 111", 111),
		CONTROL_112("Control 112", 112),
		CONTROL_113("Control 113", 113),
		CONTROL_114("Control 114", 114),
		CONTROL_115("Control 115", 115),
		CONTROL_116("Control 116", 116),
		CONTROL_117("Control 117", 117),
		CONTROL_118("Control 118", 118),
		CONTROL_119("Control 119", 119),
		CONTROL_120("Control 120", 120),
		CONTROL_121("Control 121", 121),
		CONTROL_122("Control 122", 122),
		CONTROL_123("Control 123", 123),
		CONTROL_124("Control 124", 124),
		CONTROL_125("Control 125", 125),
		CONTROL_126("Control 126", 126),
		CONTROL_127("Control 127", 127);

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
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters, final long currentGuiTime )
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
