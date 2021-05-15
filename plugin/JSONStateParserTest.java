package plugin;

import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import apple.laf.JRSUIConstants.Widget;
import scout.AppState;
import scout.Widget.WidgetVisibility;

import static org.junit.Assert.*;
import static plugin.JSONStateParser.*;

import java.util.HashMap;

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

}
