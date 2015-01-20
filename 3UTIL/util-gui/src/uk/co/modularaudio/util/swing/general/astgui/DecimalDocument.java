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

package uk.co.modularaudio.util.swing.general.astgui;

import java.awt.Toolkit;
import java.text.ParseException;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
*
* DecimalDocument extends PlainDocument to so that any associated
* components will only accept valid floating or integer words. The
* actual format of the representation is defined by a ScientificFormat
* object.
*
* @since $Date: 2012/02/09 13:05:27 $
* @since 26-OCT-2000
* @author Peter W. Draper
* @version $Id: DecimalDocument.java,v 1.2 2012/02/09 13:05:27 dan Exp $
*/
public class DecimalDocument extends PlainDocument
{
	private static final long serialVersionUID = -969917261938752269L;
	
	private ScientificFormat format;

   public DecimalDocument( ScientificFormat format )
   {
       this.format = format;
   }

   public ScientificFormat getFormat()
   {
       return format;
   }

   public void insertString(int offs, String str, AttributeSet a)
       throws BadLocationException
   {
       //  Add a trailing zero so that "+", "-" and "." may be
       //  entered as the first character. This is in fact the only
       //  floating point specific part of this class. The real work
       //  is done by the NumberFormat object.
       String currentText = getText( 0, getLength() ) + "0";

       String beforeOffset = currentText.substring(0, offs);
       String afterOffset = currentText.substring(offs, currentText.length());
       String proposedResult = beforeOffset + str + afterOffset;
       try {
           format.parseObject( proposedResult );
           super.insertString( offs, str, a );
       } catch (ParseException e) {
           Toolkit.getDefaultToolkit().beep();
       }
   }

   public void remove( int offs, int len )
       throws BadLocationException
   {
       String currentText = getText( 0, getLength() );
       String beforeOffset = currentText.substring( 0, offs );
       String afterOffset = currentText.substring(
                              len + offs, currentText.length() );
       String proposedResult = beforeOffset + afterOffset;
       try {
           if ( proposedResult.length() > 1 ) { // any single value OK.
               format.parseObject( proposedResult );
           }
           super.remove( offs, len );
       } catch ( ParseException e ) {
           Toolkit.getDefaultToolkit().beep();
       }
   }
}
