package plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static plugin.JSONStateParser.locationAreaAsJSONObject;
import static plugin.JSONStateParser.metadataAsJSONObject;
import static plugin.JSONStateParser.widgetAsJSONObject;

import java.awt.Rectangle;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.BeforeClass;
import org.junit.Test;

import scout.AppState;
import scout.Widget;
import scout.Widget.WidgetStatus;
import scout.Widget.WidgetSubtype;
import scout.Widget.WidgetType;
import scout.Widget.WidgetVisibility;

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
    public void testWidgetAsJSONObject() throws Exception {
        Widget widget = new Widget();
        widget.setId("1313");
        widget.setText("very nice widget");
        widget.setWeight(0.5);
        widget.setWidgetType(WidgetType.ACTION);
        widget.setWidgetSubtype(WidgetSubtype.LEFT_CLICK_ACTION);
        widget.setWidgetStatus(WidgetStatus.LOCATED);
        widget.setCreatedDate(new Date(1623332401000L));
        widget.setResolvedDate(new Date(1623340200000L));
        widget.setCreatedBy("Mr. Tester"); 
        widget.setCreatedByPlugin("Blockchain-5G-AI-plugin");
        widget.setComment("looks strange");
        widget.setReportedText("Label is missing");
        widget.setReportedBy("Tester 5");
        widget.setReportedDate(new Date(1623332467000L));
        widget.putMetadata("abc", "def");
        widget.setWidgetVisibility(WidgetVisibility.VISIBLE);
        widget.setLocationArea(new Rectangle(970,117,14,36));

        JSONObject result = widgetAsJSONObject(widget);

        assertNotNull(result);

        assertEquals("1313", result.get("id"));
        assertEquals("very nice widget", result.get("text"));
        assertEquals(0.5, result.get("weight"));
        assertEquals(WidgetType.ACTION.name(), (String) result.get("type"));
        assertEquals(WidgetSubtype.LEFT_CLICK_ACTION.name(), (String) result.get("subtype"));
        assertEquals(WidgetStatus.LOCATED.name(), (String) result.get("status"));
        assertEquals(1623332401000L, result.get("created-date-ms"));
        assertEquals(1623340200000L, result.get("resolved-data-ms"));
        assertEquals("Mr. Tester", result.get("created-by"));
        assertEquals("Blockchain-5G-AI-plugin", result.get("created-by-plugin"));
        assertEquals("looks strange", result.get("comment"));
        assertEquals("Label is missing", result.get("reported-text"));
        assertEquals("Tester 5", result.get("reported-by"));
        assertEquals(1623332467000L, result.get("reported-data-ms"));
        assertNotNull(result.get("meta-data"));
        assertEquals(WidgetVisibility.VISIBLE.name(), (String) result.get("visibility"));
        assertNotNull(result.get("location"));
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
        Widget widget = new Widget();
        widget.putMetadata("a", null);
        widget.putMetadata("b", 42);
        widget.putMetadata("c", "42");

        JSONObject result = metadataAsJSONObject(widget);
        
        assertNull(result.get("a"));
        assertEquals("42", result.get("b"));
        assertEquals("42", result.get("c"));
    }

    @Test
    public void testMetadataAsJSONObject_Widget() {
        Widget widget = new Widget();
        Widget matchingWidget = new Widget();
        matchingWidget.setId("1234");
        widget.putMetadata("matching_widget", matchingWidget);

        JSONObject result = metadataAsJSONObject(widget);
        
        assertEquals("1234", result.get("matching_widget"));
    }

    @Test
    public void testMetadataAsJSONObject_Filter() {
        Widget widget = new Widget();
        widget.putMetadata("neighbors", "12");

        JSONObject result = metadataAsJSONObject(widget);
        
        assertNull(result.get("neighbors"));
    }

    @Test
    public void testStateWidgetAsSimpleJSONObject() {
        // TODO: Implement
    }

    @Test
    public void testParseCompleteAppState() throws Exception {
        String filePath = JSONStateParser.class.getClassLoader().getResource("scenario_20/state_initial.json").getPath(); 
        JSONObject jsonState = loadJSONModel(filePath);

        AppState result = JSONStateParser.parseCompleteAppState(jsonState);

        assertNotNull(result);
        assertEquals("0", result.getId());
        assertEquals("Home", result.getBookmark());
        assertEquals(1, result.getVisibleActions().size());
        
        Widget w1 = result.getVisibleActions().get(0);
        assertEquals("162272541342971", w1.getId());
        
        AppState level2 = w1.getNextState();
        assertNotNull(level2);
        assertEquals(2, level2.getVisibleActions().size());
        
        Widget w2 = level2.getVisibleActions().get(0);
        assertEquals("162272541697749", w2.getId());

        Widget w3 = level2.getVisibleActions().get(1);
        assertEquals("162272543838154", w3.getId());
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
