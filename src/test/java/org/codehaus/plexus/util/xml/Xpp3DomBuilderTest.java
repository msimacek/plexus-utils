package org.codehaus.plexus.util.xml;

import junit.framework.TestCase;
import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;

/**
 * Test the Xpp3DomBuilder.
 *
 * @author <a href="mailto:brett@codehaus.org">Brett Porter</a>
 * @version $Id$
 */
public class Xpp3DomBuilderTest
    extends TestCase
{
    public void testBuildFromReader()
        throws Exception
    {
        String domString = createDomString();

        Xpp3Dom dom = Xpp3DomBuilder.build( new StringReader( domString ) );

        Xpp3Dom expectedDom = createExpectedDom();

        assertEquals( "check DOMs match", expectedDom, dom );
    }

    public void testBuildTrimming()
        throws Exception
    {
        String domString = createDomString();

        Xpp3Dom dom = Xpp3DomBuilder.build( new StringReader( domString ), true );

        assertEquals( "test with trimming on", "element1", dom.getChild( "el1" ).getValue() );

        dom = Xpp3DomBuilder.build( new StringReader( domString ), false );

        assertEquals( "test with trimming off", " element1\n ", dom.getChild( "el1" ).getValue() );
    }

    public void testBuildFromXpp3Dom()
        throws Exception
    {
        Xpp3Dom expectedDom = createExpectedDom();
        Xpp3Dom dom = null;

        XmlPullParser parser = new MXParser();

        String domString = "<newRoot><configuration>" + createDomString() + "</configuration></newRoot>";
        parser.setInput( new StringReader( domString ) );

        int eventType = parser.getEventType();

        boolean configurationClosed = false;
        boolean newRootClosed = false;
        boolean rootClosed = false;

        while ( eventType != XmlPullParser.END_DOCUMENT )
        {
            if ( eventType == XmlPullParser.START_TAG )
            {
                String rawName = parser.getName();

                if ( rawName.equals( "root" ) )
                {
                    dom = Xpp3DomBuilder.build( parser );
                }
            }
            else if ( eventType == XmlPullParser.END_TAG )
            {
                String rawName = parser.getName();

                if ( rawName.equals( "configuration" ) )
                {
                    configurationClosed = true;
                }
                else if ( rawName.equals( "newRoot" ) )
                {
                    newRootClosed = true;
                }
                else if ( rawName.equals( "root" ) )
                {
                    rootClosed = true;
                }
            }
            eventType = parser.next();
        }

        assertEquals( "Check DOM matches", expectedDom, dom );
        assertFalse( "Check closing root was consumed", rootClosed );
        assertTrue( "Check continued to parse configuration", configurationClosed );
        assertTrue( "Check continued to parse newRoot", newRootClosed );
    }

    /**
     * Test we get an error from the parser, and don't hit the IllegalStateException.
     */
    public void testUnclosedXml()
    {
        String domString = "<newRoot>" + createDomString();
        try
        {
            Xpp3DomBuilder.build( new StringReader( domString ) );
        }
        catch ( XmlPullParserException expected )
        {
            // correct
            assertTrue( true );
        }
        catch ( IOException expected )
        {
            // this will do too
            assertTrue( true );
        }
    }

    //
    // HELPER METHODS
    //

    private static String createDomString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "<root>\n" );
        buf.append( " <el1> element1\n </el1>\n" );
        buf.append( " <el2 att2='attribute2'>\n" );
        buf.append( "  <el3 att3='attribute3'>element3</el3>\n" );
        buf.append( " </el2>\n" );
        buf.append( "</root>\n" );

        String domString = buf.toString();
        return domString;
    }

    private static Xpp3Dom createExpectedDom()
    {
        Xpp3Dom expectedDom = new Xpp3Dom( "root" );
        Xpp3Dom el1 = new Xpp3Dom( "el1" );
        el1.setValue( "element1" );
        expectedDom.addChild( el1 );
        Xpp3Dom el2 = new Xpp3Dom( "el2" );
        el2.setAttribute( "att2", "attribute2" );
        expectedDom.addChild( el2 );
        Xpp3Dom el3 = new Xpp3Dom( "el3" );
        el3.setAttribute( "att3", "attribute3" );
        el3.setValue( "element3" );
        el2.addChild( el3 );
        return expectedDom;
    }

}
