
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
 *
 * This software is licensed under the KSU Open Academic License.
 * You should have received a copy of the license with the distribution.
 * A copy can be found at
 *     http://www.cis.ksu.edu/santos/license.html
 * or you can contact the lab at:
 *     SAnToS Laboratory
 *     234 Nichols Hall
 *     Manhattan, KS 66506, USA
 */

package edu.ksu.cis.indus.xmlizer;

import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.znerd.xmlenc.LineBreak;
import org.znerd.xmlenc.XMLOutputter;


/**
 * This is a custom xml outputter class with preconfigured formatting infor.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class CustomXMLOutputter
  extends XMLOutputter {
	/**
	 * @see org.znerd.xmlenc.XMLOutputter#XMLOutputter(Writer,String)
	 */
	public CustomXMLOutputter(final Writer writer, final String encoding)
	  throws IllegalStateException, IllegalArgumentException, UnsupportedEncodingException {
		super(writer, encoding);
		setEscaping(true);
		setIndentation("  ");
		setLineBreak(LineBreak.UNIX);
	}
}

/*
   ChangeLog:
   $Log$
 */