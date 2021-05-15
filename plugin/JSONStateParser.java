// SPDX-FileCopyrightText: 2021 Andreas Bauer
//
// SPDX-License-Identifier: MIT

package plugin;

import java.util.*;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import scout.AppState;
import scout.Neighbor;
import scout.Path;
import scout.StateController;
import scout.Widget;
import scout.Widget.WidgetSubtype;
import scout.Widget.WidgetType;
import scout.Widget.WidgetVisibility;

public class JSONStateParser {
    
    // ********************************************
    // * Parse Scout objects to JSONObjects       *
    // ********************************************
     
    @SuppressWarnings("unchecked")
	public static JSONObject appStateAsJSONObject(AppState state) {
		JSONObject json = new JSONObject();

		json.put("product", StateController.getProduct());

		List<JSONObject> paths = state.getPaths().stream()
			.map(p -> pathAsJSONObject(p))
			.collect(Collectors.toList());
		json.put("paths", paths);
		
		Map<String, Widget> allUsedWidgets = new HashMap<>();
		
		json.put("state", stateTreeAsJSONObject(state, allUsedWidgets));

		List<JSONObject> issues = state.getAllIssues().stream()
			.map(i -> widgetAsJSONObject(i))
			.collect(Collectors.toList());
		json.put("issues", issues);

		List<JSONObject> allUsedWidgetsAsJSON = allUsedWidgets.values().stream()
			.map(w -> widgetAsJSONObject(w))
			.collect(Collectors.toList());
			
		List<JSONObject> matchingWidgetsAsJSON = allUsedWidgets.values().stream()
			.filter(w -> w.hasMetadata("matching_widget"))
			.map(w -> (Widget)w.getMetadata("matching_widget"))
			.map(w -> widgetAsJSONObject(w))
			.collect(Collectors.toList());

			allUsedWidgetsAsJSON.addAll(matchingWidgetsAsJSON);
			json.put("all-widgets", allUsedWidgetsAsJSON);
		return json;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject stateTreeAsJSONObject(AppState state, Map<String, Widget> allUsedWidgets) {
		JSONObject json = new JSONObject();
		json.put("id", state.getId());
		json.put("product-version", state.getProductVersions());
		json.put("bookmarks", state.getBookmark());
		
		List<Widget> visibleWidgets= state.getVisibleWidgets();
		
		json.put("visible-widgets", visibleWidgets.stream().map(w -> stateWidgetAsSimpleJSONObject(w, allUsedWidgets)).collect(Collectors.toList()));
		
		visibleWidgets.forEach(w -> allUsedWidgets.put(w.getId(), w));

		return json;
	}
	
	@SuppressWarnings("unchecked")
	public static JSONObject pathAsJSONObject(Path path) {
		JSONObject json = new JSONObject();
		json.put("id", path.getId());
		json.put("product-version", path.getProductVersion());
		json.put("session-id", path.getSessionId());
		json.put("session-duration", path.getSessionDuration());
		json.put("created-at", path.getCreatedDate().toString());
		json.put("tester", path.getTester());
		
		List<String> widgetIDs = path.getWidgets().stream()
			.map(w -> w.getId())
			.collect(Collectors.toList());

		json.put("widgets", widgetIDs);
		
		return json;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject widgetAsJSONObject(Widget widget) {
		JSONObject json = new JSONObject();
		json.put("id", widget.getId());
		json.put("text", widget.getText());
		json.put("weight",widget.getWeight());
		json.put("type", widget.getWidgetType().name());
		json.put("subtype", widget.getWidgetSubtype().toString());
		json.put("status", widget.getWidgetStatus().toString());
		json.put("created-by",widget.getCreatedBy());
		json.put("created-by-plugin", widget.getCreatedByPlugin());
		json.put("comment", widget.getComment());
		json.put("reported-text", widget.getReportedText());
		json.put("reported-by", widget.getReportedBy());
		json.put("reported-data", widget.getReportedDate());
		json.put("meta-data", metadataAsJSONObject(widget));
		json.put("visibility", widget.getWidgetVisibility().name());
        json.put("location", locationAreaAsJSONObject(widget.getLocationArea()));
				
		return json;
	}

	@SuppressWarnings("unchecked")
    public static JSONObject locationAreaAsJSONObject(java.awt.Rectangle locRec) {
        if (locRec == null) {
            return null;
        }

        JSONObject jsonLoc = new JSONObject();
        jsonLoc.put("x", String.valueOf(locRec.x));
		jsonLoc.put("y", String.valueOf(locRec.y));
		jsonLoc.put("width", String.valueOf(locRec.width));
		jsonLoc.put("height", String.valueOf(locRec.height));
        return jsonLoc;
    }

	@SuppressWarnings("unchecked")
	public static JSONObject metadataAsJSONObject(Widget widget) {
		JSONObject json = new JSONObject();
		if (widget.hasMetadata("matching_widget")){
			String widgetId = ((Widget)widget.getMetadata("matching_widget")).getId();
			json.put("matching_widget", widgetId);
		}

		widget.getMetadataKeys().stream()
		.filter(k -> !k.equals("matching_widget"))
		.filter(k -> !k.equals("neighbors"))
		.forEach(k -> json.put(k, String.valueOf(widget.getMetadata(k))));	

		return json;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject stateWidgetAsSimpleJSONObject(Widget widget, Map<String,Widget> allUsedWidgets) {
		JSONObject json = new JSONObject();
		json.put("id", widget.getId());

		AppState nexState = widget.getNextState();
		if (nexState == null || nexState.isHome()){
			json.put("next-state", null);
			return json;
		}

		json.put("next-state", stateTreeAsJSONObject(widget.getNextState(), allUsedWidgets));

		return json;
	}

    // ********************************************
    // * Parse JSONObjects to Scout objects       *
    // ********************************************
}
