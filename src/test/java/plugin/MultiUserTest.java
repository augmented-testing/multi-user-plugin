// SPDX-FileCopyrightText: 2021 Andreas Bauer
//
// SPDX-License-Identifier: MIT

package plugin;

import java.util.ArrayList;


import java.awt.Rectangle;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MultiUserTest extends MultiUser {

    public MultiUserTest() {
        super(new ArrayList<String>());
    }

    @Test
    public void testisSameWidget() {
        Widget widget = createWidget("1234");

        Widget other = createWidget("5678");
        other.setCreatedBy("Second tester"); 

        assertTrue(isSameWidget(widget, other));
    }

    @Test
    public void testIsSameWidget_ShouldFail() {
        Widget widget = createWidget("1234");
        
        assertFalse(isSameWidget(widget, null));
        assertFalse(isSameWidget(widget, new Widget()));
        
        Widget other = null;
        other = createWidget("1234");
        other.setWidgetVisibility(WidgetVisibility.HIDDEN);
        assertFalse(isSameWidget(widget, other));

        other = createWidget("1234");
        other.setWidgetType(WidgetType.ISSUE);
        assertFalse(isSameWidget(widget, other));
        
        other = createWidget("1234");
        other.setWidgetSubtype(WidgetSubtype.RIGHT_CLICK_ACTION);
        assertFalse(isSameWidget(widget, other));
    }

    @Test
    public void testIsSameWidget_ShouldFailMetaData() {
        Widget widget = createWidget("1234");
        Widget other = null;

        other = createWidget("1234");
        other.putMetadata("xpath", "/html[1]/body[1]");
        assertFalse(isSameWidget(widget, other));
        
        other = createWidget("1234");
        other.putMetadata("href", "https://othersite.de/logout");
        assertFalse(isSameWidget(widget, other));
        
        other = createWidget("1234");
        other.putMetadata("text", "Logout");
        assertFalse(isSameWidget(widget, other));
        
        other = createWidget("1234");
        other.putMetadata("tag", "Button");
        assertFalse(isSameWidget(widget, other));
        
        other = createWidget("1234");
        other.putMetadata("class", "btn"); 
        assertFalse(isSameWidget(widget, other));
    }

    @Test
    public void testHasEqualMetaData() {
        Widget widget = createWidget("1");
        Widget other = createWidget("1");

        assertTrue(hasEqualMetaData("xpath", widget, other));
        assertTrue(hasEqualMetaData("href", widget, other));
        assertTrue(hasEqualMetaData("text", widget, other));
        assertTrue(hasEqualMetaData("tag", widget, other));
        assertTrue(hasEqualMetaData("class", widget, other));
        
        widget.putMetadata("answer", "42");
        other.putMetadata("answer", 42);
        assertTrue(hasEqualMetaData("answer", widget, other));
    }

    @Test
    public void testHasEqualMetaData_Fail() {
        Widget widget = createWidget("1");
        Widget other = createWidget("1");

        other.removeMetadata("text");

        assertFalse(hasEqualMetaData("text", widget, other));
        assertFalse(hasEqualMetaData("text", other, widget));
    }
    
    private Widget createWidget(String id) {
        Widget widget = new Widget();
        widget.setId(id);
        widget.setWidgetStatus(WidgetStatus.LOCATED);
        widget.setCreatedBy("Mr. Tester"); 
        widget.setWidgetSubtype(WidgetSubtype.LEFT_CLICK_ACTION);
        widget.setWidgetVisibility(WidgetVisibility.VISIBLE);
        widget.setLocationArea(new Rectangle(970,117,14,36));
        widget.putMetadata("xpath", "/html[1]/body[1]/div[1]/div[1]/header[1]/div[1]/a[1]");
        widget.putMetadata("href", "https://mydomain.de/login");
        widget.putMetadata("text", "Login");
        widget.putMetadata("tag", "A");
        widget.putMetadata("class", "v-btn v-btn--flat v-btn--router v-btn--text theme--dark v-size--default"); 
        return widget;
    }

    private AppState loadJSONModel(String filepath) throws FileNotFoundException, IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        FileReader reader = new FileReader(filepath);			
        
        JSONObject jsonModel = (JSONObject) jsonParser.parse(reader);
        reader.close();
        
        return JSONStateParser.parseCompleteAppState(jsonModel);
    }
}
