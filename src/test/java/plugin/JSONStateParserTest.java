package plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static plugin.JSONStateParser.locationAreaAsJSONObject;

import org.json.simple.JSONObject;
import org.junit.Test;

public class JSONStateParserTest {
    
    @Test
    public void testAppStateAsJSONObject() {
        // TODO: Implement
    }
    
    @Test
    public void testStateTreeAsJSONObject() {
        // TODO: Implement
    }

    @Test
    public void testPathAsJSONObject() {
        // TODO: Implement
    }

    @Test
    public void testWidgetAsJSONObject() {
        // TODO: Implement
    }

    @Test
    public void testLocationAreaAsJSONObject() {
        java.awt.Rectangle locRec = new java.awt.Rectangle(10, 20, 100, 200);
        
        JSONObject result = locationAreaAsJSONObject(locRec);
        
        assertNotNull(result);
        assertEquals("10", result.get("x"));
        assertEquals("20", result.get("y"));
        assertEquals("100", result.get("width"));
        assertEquals("200", result.get("height"));
    }

    @Test
    public void testLocationAreaAsJSONObject_Null() {
        JSONObject result = locationAreaAsJSONObject(null);
        assertNull(result); 
    }
    
    @Test
    public void testMetadataAsJSONObject() {
        // TODO: Implement
    }

    @Test
    public void testStateWidgetAsSimpleJSONObject() {
        // TODO: Implement
    }

    @Test
    public void testParseCompleteAppState() {
        // TODO: Implement
    }

    @Test
    public void testParseState() {
        // TODO: Implement
    }

    @Test
    public void testParseWidget() {
        // TODO: Implement
    }

}
