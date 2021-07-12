package plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static plugin.JSONStateParser.locationAreaAsJSONObject;
import static plugin.JSONStateParser.metadataAsJSONObject;
import static plugin.JSONStateParser.widgetAsJSONObject;
import static plugin.JSONStateParser.stateWidgetAsSimpleJSONObject;

import java.awt.Rectangle;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import scout.AppState;
import scout.Widget;
import scout.Widget.WidgetStatus;
import scout.Widget.WidgetSubtype;
import scout.Widget.WidgetType;
import scout.Widget.WidgetVisibility;

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
        assertEquals(1623340200000L, result.get("resolved-date-ms"));
        assertEquals("Mr. Tester", result.get("created-by"));
        assertEquals("Blockchain-5G-AI-plugin", result.get("created-by-plugin"));
        assertEquals("looks strange", result.get("comment"));
        assertEquals("Label is missing", result.get("reported-text"));
        assertEquals("Tester 5", result.get("reported-by"));
        assertEquals(1623332467000L, result.get("reported-date-ms"));
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
        Widget widget = new Widget();
        widget.setId("4839");
        AppState nextState = new AppState("100", "next state");
        nextState.addProductVersion("1.0");
        widget.setNextState(nextState);
        
        Map<String,Widget> allUsedWidgets = new HashMap<>();
        JSONObject result = stateWidgetAsSimpleJSONObject(widget, allUsedWidgets);
        
        assertNotNull(result);
        assertTrue(allUsedWidgets.isEmpty());
        assertEquals(2, result.entrySet().size());

        assertEquals("4839", result.get("id"));
        assertNotNull(result.get("next-state"));
        
        JSONObject jsonNextState = (JSONObject)result.get("next-state");
        assertEquals("100", jsonNextState.get("state-id"));
        List<String> productVersions = (List<String>)jsonNextState.get("product-version"); 
        assertEquals("1.0", productVersions.get(0));
        assertEquals("next state", jsonNextState.get("bookmarks"));  
    }

    @Test
    public void testParseCompleteAppState() throws Exception {
        String filePath = JSONStateParser.class.getClassLoader().getResource("scenario_20/state_initial.json").getPath(); 
        JSONObject jsonState = loadJSONModel(filePath);

        AppState result = JSONStateParser.parseCompleteAppState(jsonState);

        Widget btnToMac = result.getWidget("btnToMac");
        assertNotNull(btnToMac);
        AppState stateMac = btnToMac.getNextState();
        assertNotNull(stateMac);

        Widget btnToAir = stateMac.getWidget("btnToAir");
        assertNotNull(btnToAir);
        AppState stateAir = btnToAir.getNextState();
        assertNotNull(stateAir);
        Widget btnToAirTechInfo = stateAir.getWidget("btnToAirTechInfo");
        assertNotNull(btnToAirTechInfo);
        AppState stateAirTechInfo = btnToAirTechInfo.getNextState();
        assertNotNull(stateAirTechInfo);

        Widget btnToPro13 = stateMac.getWidget("btnToPro13");
        assertNotNull(btnToPro13);
        AppState statePro13 = btnToPro13.getNextState();
        assertNotNull(statePro13);
        Widget btnToPro13TechInfo = statePro13.getWidget("btnToPro13TechInfo");
        assertNotNull(btnToPro13TechInfo);
        AppState statePro13TechInfo = btnToPro13TechInfo.getNextState();
        assertNotNull(statePro13TechInfo);
    }

    @Test
    public void testParseState() {
        // TODO: Implement
    }

    @Test
    public void testParseWidget() throws Exception {
        String filePath = JSONStateParser.class.getClassLoader().getResource("widget.json").getPath(); 
        JSONObject jsonWidget = loadJSONModel(filePath);
        
        Widget result = JSONStateParser.parseWidget(jsonWidget);

        assertNotNull(result);
        assertEquals("100100100", result.getId());
        assertEquals("very nice widget", result.getText());
        assertEquals(0.5, result.getWeight(), 0.0001);
        assertEquals(WidgetType.ACTION, result.getWidgetType());
        assertEquals(WidgetSubtype.LEFT_CLICK_ACTION, result.getWidgetSubtype());
        assertEquals(WidgetStatus.LOCATED, result.getWidgetStatus());
        
        assertEquals(1623332401000L, result.getCreatedDate().getTime());
        assertEquals(1623340200000L, result.getResolvedDate().getTime());
        assertEquals(1623332467000L, result.getReportedDate().getTime());
        
        assertEquals("Mr. Tester", result.getCreatedBy());
        assertEquals("Blockchain-5G-AI-plugin", result.getCreatedByPlugin());
        assertEquals("looks strange", result.getComment());
        assertEquals("Label is missing", result.getReportedText());
        assertEquals("Tester 5", result.getReportedBy());

        assertEquals(WidgetVisibility.VISIBLE, result.getWidgetVisibility());
        assertEquals(new Rectangle(970,117,14,36), result.getLocationArea()); 
        
        assertEquals("div", result.getMetadata("class"));
    }

    private static JSONObject loadJSONModel(String filepath) throws FileNotFoundException, IOException, ParseException{
        JSONParser jsonParser = new JSONParser();
        FileReader reader = new FileReader(filepath);			
        
        JSONObject jsonModel = (JSONObject) jsonParser.parse(reader);
        reader.close();
        
        return jsonModel;
    }

}
