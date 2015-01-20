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

package uk.co.modularaudio.util.swing.general;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class EditableJLabel extends JPanel
{
	private static final long serialVersionUID = -8513383282955466324L;

	/** The user is editing the component (JTextField is shown at the moment) */
	private boolean editing;

	/** The confirm edition key is Return */
	private static final int confirmKeyCode = KeyEvent.VK_ENTER;

	/** The cancel edition key is Return */
	private static final int cancelKeyCode = KeyEvent.VK_ESCAPE;

	/** The value which holds this component */
	private String value;

	// graphical components

	private CardLayout cl;

	private JPanel pnlCards;

	private static final String TEXT_FIELD = "text field";

	private JTextField textField;

	private static final String LABEL = "label";

	private JLabel label;

	public EditableJLabel()
	{
		cl = new CardLayout();
		pnlCards = new JPanel(cl);
		textField = new JTextField();
		label = new JLabel();
		label.setBackground( Color.magenta );
		pnlCards.add(textField, TEXT_FIELD);
		pnlCards.add(label, LABEL);
		cl.show(pnlCards, LABEL);
		add(pnlCards);

		// register the listeners
		label.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				// if double click, set edition mode
				if (e.getClickCount() == 2)
				{
					startEdition();
				}
			}
		});

		textField.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyReleased(KeyEvent e)
			{
				if (e.getKeyCode() == confirmKeyCode)
				{
					/*
					 * confirmation key pressed, so changing to non-edition and
					 * confirm changes
					 */
					confirmEdition();
				}
				else if (e.getKeyCode() == cancelKeyCode)
				{
					/*
					 * cancel key pressed, so changing to non edition and cancel
					 * the changes
					 */
					cancelEdition();
				}
			}
		});
	}

	private void startEdition()
	{
		cl.show(pnlCards, TEXT_FIELD);
		textField.setText(value);
		textField.requestFocus();
		textField.selectAll();
	}

	private void cancelEdition()
	{
		textField.setText(value);
		cl.show(pnlCards, LABEL);
	}

	private void confirmEdition()
	{
		value = textField.getText();
		label.setText(value);
		cl.show(pnlCards, LABEL);
	}

	/**
	 * @return the value
	 */
	public String getValue()
	{
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value)
	{
		this.value = value;
	}

	/**
	 * @return returns true if the component is currently showing the text field
	 *         or false otherwise.
	 */
	public boolean isEditing()
	{
		return editing;
	}
	
	public void setText( String text )
	{
		value = text;
		textField.setText( text );
		label.setText( text );
	}
	
	@Override
	public void setFont( Font font )
	{
		super.setFont( font );
		if( textField != null )
		{
			textField.setFont( font );
		}
		if( label != null )
		{
			label.setFont( font );
		}
	}
	
	public Font getFont()
	{
		if( label != null )
		{
			return label.getFont();
		}
		else
		{
			return super.getFont();
		}
	}

}
