package plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static plugin.JSONStateParser.locationAreaAsJSONObject;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.BeforeClass;
import org.junit.Test;

import scout.Widget;

public class JSONStateParserTest {
   
    private static JSONObject modelJSON;

    @BeforeClass
    public static void setup() throws Exception {
        String filePath = JSONStateParser.class.getClassLoader().getResource("scenario_10/state.json").getPath();
        modelJSON = loadJSONModel(filePath);
    }

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
        JSONArray allWidgetsJSON = (JSONArray)modelJSON.get("all-widgets");
        JSONObject firstWidget = (JSONObject)allWidgetsJSON.iterator().next();

        Widget result = JSONStateParser.parseWidget(firstWidget);

        assertNotNull(result);
        assertEquals("162124546053328", result.getId());
    }

    private static JSONObject loadJSONModel(String filepath) throws FileNotFoundException, IOException, ParseException{
        JSONParser jsonParser = new JSONParser();
        FileReader reader = new FileReader(filepath);			
        
        JSONObject jsonModel = (JSONObject) jsonParser.parse(reader);
        reader.close();
        
        return jsonModel;
    }

}
