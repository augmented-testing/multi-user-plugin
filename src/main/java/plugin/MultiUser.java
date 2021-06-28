// SPDX-FileCopyrightText: 2021 Andreas Bauer
//
// SPDX-License-Identifier: MIT

package plugin;

import static plugin.JSONStateParser.appStateAsJSONObject;
import static plugin.JSONStateParser.parseCompleteAppState;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import scout.AppState;
import scout.StateController;
import scout.Widget;

public class MultiUser {

    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private static final String DATA_FILEPATH = "data";
    private static final String MODEL_FILENAME = "state.json";
    private static final String PRODUCT_PROPERTIES_FILE = "product.properties";

    protected static final String META_DATA_DIFF = "multi-user-diff-widgets";
    protected static final String DELETED_AT = "multi-user-merge-deleted-at";

    protected enum DiffType {
        CREATED, DELETED, CHANGED, NO_CHANGES
    }

    public MultiUser() {
        StateController.setProducts(getFolders(DATA_FILEPATH));
    }

    public MultiUser(List<String> products) {
        StateController.setProducts(products);
    }

    /**
     * Load state tree for for the current product or create a new home state if not found.
     * @return A state tree
     */
    public AppState loadState() {
        String product = StateController.getProduct();
        String filePath = getFilePathForProduct(product);

        Properties properties = loadProductProperties(product, filePath);
        StateController.setProductProperties(properties);

        String modelFilePath = filePath + "/" + MODEL_FILENAME;
        JSONObject jsonModel = loadJSONModel(modelFilePath);
        if (jsonModel == null) {
            return new AppState("0", "Home");
        }

        return parseCompleteAppState(jsonModel);
    }

    private String getFilePathForProduct(String product) {
        if (product.isEmpty()) {
            return DATA_FILEPATH;
        }

        return DATA_FILEPATH + "/" + product;
    }

    private Properties loadProductProperties(String product, String projectRootPath) {
        try {
            Properties productProperties = new Properties();
            String filePath = projectRootPath + "/" + PRODUCT_PROPERTIES_FILE;
            FileInputStream in = new FileInputStream(filePath);
            productProperties.load(in);
            in.close();
            return productProperties;
        } catch (Exception e) {
            return new Properties();
        }
    }

    private JSONObject loadJSONModel(String filePath) {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonState = null;
        try {
            FileReader reader = new FileReader(filePath);			
            jsonState = (JSONObject) jsonParser.parse(reader);
            reader.close();
        } catch(FileNotFoundException nfe) {
            System.out.println("State model file not found at location '"+ filePath+"'. Start with empty model.");
            return null;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }

        return jsonState;
    }

    /**
     * Save the state tree for the current product.
     * @return true if done
     */
    public Boolean saveState() {
        AppState stateTree=StateController.getStateTree();
        String product=StateController.getProduct();

        String productFilePath = getFilePathForProduct(product);
        
        createFolderIfNotExist(productFilePath);
        
        String modelFilePath= productFilePath + "/" + MODEL_FILENAME;

        // Save state tree
        if(!saveStateModel(modelFilePath, stateTree)) {
            return false;
        }
        
        // Get weekday number
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        
        // Save a backup
        modelFilePath += dayOfWeek;
        if(!saveStateModel(modelFilePath, stateTree)) {
            return false;
        }

        String propertiesFilePath = productFilePath + "/" + PRODUCT_PROPERTIES_FILE;
        saveProductProperties(propertiesFilePath);
        
        // Update products
        StateController.setProducts(getFolders(DATA_FILEPATH));
        
        return true;
    }

    protected void createFolderIfNotExist(String filePath) {
        File file=new File(filePath);
        file.mkdirs();
    }

    protected boolean saveProductProperties(String filePath) {
        try {
            FileWriter fileWriter = new FileWriter(filePath);
            StateController.getProductProperties().store(fileWriter, null);
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } 

        return true;
    }

    private boolean saveStateModel(String filePath, AppState appState) {
        String jsonState = "";
        try {
            jsonState = appStateAsJSONObject(appState).toJSONString();
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
        try {
            FileWriter fileWriter = new FileWriter(filePath);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.print(jsonState);
            printWriter.close();
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;	
    }

    private List<String> getFolders(String dirPath)	{
        try {
            return Files.list(Paths.get(dirPath))
                .filter(path -> Files.isDirectory(path))
                .map(Path::getFileName)
                .map(Path::toString)
                .collect(Collectors.toList());
        } catch (Exception e) {
            return new LinkedList<>();
        } 
    }
    
    protected boolean isSameWidget(Widget widget, Widget other) {
        if (widget == null || other == null) {
            return false;
        }

        boolean isSameType = widget.getWidgetType().equals(other.getWidgetType());
        boolean isSameSubType = widget.getWidgetSubtype().equals(other.getWidgetSubtype());
        boolean isSameVisibility = widget.getWidgetVisibility().equals(other.getWidgetVisibility());

        boolean isSameHref = hasEqualMetaData("href", widget, other);
        boolean isSameXpath = hasEqualMetaData("xpath", widget, other);
        boolean isSameText = hasEqualMetaData("text", widget, other);
        boolean isSameTag = hasEqualMetaData("tag", widget, other);
        boolean isSameClass = hasEqualMetaData("class", widget, other);
        
        return isSameType
            && isSameSubType
            && isSameVisibility
            && isSameHref 
            && isSameXpath
            && isSameText
            && isSameTag
            && isSameClass;
    }

    protected AppState mergeInitialAppStates(AppState state, AppState other) {
        if (state == null && other == null) {
            throw new IllegalArgumentException("Expected AppState arguments to be not null at the same time");
        }

        if (state == null) {
            return other;
        }

        if (other == null) {
            return state;
        }

        Map<Widget,Widget> sameWidgets = new HashMap<>();
        List<Widget> diffWidgets = new LinkedList<>();
        
        for (Widget widget : state.getVisibleActions()) {
            boolean foundMatch = false;
            for (Widget otherWidget : other.getVisibleActions() ) {
                if (isSameWidget(widget, otherWidget)) {
                    sameWidgets.put(otherWidget, widget);
                    foundMatch = true;
                }
            }
            if (!foundMatch) {
                diffWidgets.add(widget);
            }
        }
        
        diffWidgets.addAll(other.getVisibleActions().stream()
            .filter(o -> sameWidgets.get(o) == null)
            .collect(Collectors.toList())
        );                  
        
        String bookmark = chooseStrValue(state.getBookmark(), other.getBookmark());
        AppState resultState = new AppState(state.getId(), bookmark);
        diffWidgets.forEach(w -> resultState.addWidget(w));
        
        for (Widget otherWidget: sameWidgets.keySet()) {
            Widget widget = sameWidgets.get(otherWidget);
            Widget merged = mergeSameWidgets(widget, otherWidget);

            if (widget.getNextState() != null || otherWidget.getNextState() != null) {
                AppState nextState = mergeInitialAppStates(widget.getNextState(), otherWidget.getNextState());
                merged.setNextState(nextState);
            }

            resultState.addWidget(merged);
        }

        return resultState;
    }

    protected String chooseStrValue(String value, String otherValue) {
        if (value == null && otherValue == null) {
            return null;
        }

        if (value == null || value.isEmpty()) {
            return otherValue;
        } 

        return value;
    }

    protected Widget mergeSameWidgets(Widget widget, Widget other) {
        log("Merge widgets with ID '"+widget.getId()+"' and '"+other.getId()+"'" );
        
        Widget result = new Widget(widget);
        result.setId(widget.getId());

        other.getMetadataKeys().stream()
            .filter(key -> widget.getMetadata(key) == null)
            .forEach(key -> result.putMetadata(key, other.getMetadata(key)));

        result.setNextState(null);
        return result;
    }

    /**
     * Add annotations as meta-data to track the changes of 
     * widgets in each state after a session.
     * These annotations support the merging process with the shared state model.
     * @param before app state from session start
     * @param after changed app state that shall be annotated
     */
    protected void annotateDiffsInStates(AppState before, AppState after) {        
        if (before == null && after == null) {
            return;
        }
        
        List<Widget> remainingBeforeWidgets = new LinkedList<>();
        List<Widget> afterWidgets = new LinkedList<>();

        if (before != null) {
            remainingBeforeWidgets = new LinkedList<>(before.getVisibleActions()); 
        }

        if (after != null) {
            afterWidgets = new LinkedList<>(after.getVisibleActions());
        }

        Map<String, DiffType> widgetDiff = new HashMap<>();

        for (Widget afterWidget : afterWidgets) {                
            int foundIndex = indexOfSameWidget(afterWidget, remainingBeforeWidgets);
            boolean isPresent = foundIndex >= 0;

            DiffType diffType = DiffType.CREATED;
            AppState nextStateFromWidgetBefore = null;
            if (isPresent) {
                diffType = DiffType.NO_CHANGES;
                nextStateFromWidgetBefore = remainingBeforeWidgets.get(foundIndex).getNextState();
                remainingBeforeWidgets.remove(foundIndex); 
            }
            
            widgetDiff.put(afterWidget.getId(), diffType);
            annotateDiffsInStates(nextStateFromWidgetBefore, afterWidget.getNextState());
        }

        remainingBeforeWidgets.forEach(deletedWidget -> widgetDiff.put(deletedWidget.getId(), DiffType.DELETED));
        
        after.putMetadata(META_DATA_DIFF, widgetDiff);
    }

    /**
     * Merges changes of the session app state into the app state from the shared model. 
     * The method {@link #annotateDiffsInStates(AppState, AppState)} must be called on 
     * the session state before merging.
     * 
     * @param sharedState app state from the shared model 
     * @param sessionState app state from the current session with changes
     * @return a copy of the shared state with changes merged from the session state. 
     */
    protected AppState mergeStateChanges(AppState sharedState, AppState sessionState) {
        AppState result = deepCopy(sharedState);

        doMergeStateChangesIntoShared(result, sessionState);
         
        return result;
    }

    private void doMergeStateChangesIntoShared(AppState sharedState, AppState sessionState) {
        if (sharedState == null && sessionState == null) {
            return;
        }

        if (sessionState == null) {
            return;
        }

        if (sharedState == null) {
            log("Unable to merge session state into NULL shared state. Caused by state with id: " + sessionState.getId());
            return;
        }
        
        sessionState.getMetadataKeys().stream()
            .filter(key -> sharedState.getMetadata(key) == null)
            .filter(key -> !key.equalsIgnoreCase(META_DATA_DIFF))
            .forEach(key -> sharedState.putMetadata(key, sessionState.getMetadata(key)));
        
        Map<String, DiffType> diffMap = getDiffMetaDataFromState(sessionState);
        if (diffMap.isEmpty()) {
            log("Session state with id " + sessionState.getId() + " doesn't have any diff annotations to proceed with merge.");
            return;
        }
        for (Entry<String, DiffType> diffItem : diffMap.entrySet()) {
            String widgetId = diffItem.getKey();

            switch (diffItem.getValue()) {
                case DELETED:
                    handleMergeDeletion(sharedState.getWidget(widgetId));    
                    break;
                case CREATED:
                    handleMergeCreation(sharedState, sessionState, widgetId);
                    break;
                case NO_CHANGES:
                    handleMergeNoChange(sharedState, sessionState, widgetId);
                    break;
                default:
                    log("[Merge] DiffType '" +diffItem.getValue()+ "' does not have a merging strategy");
                    break;
            }
                             
        }
    }

    protected void handleMergeDeletion(Widget widget) {
        markAsDeleted(widget);

        AppState nextState = widget.getNextState();
        if (nextState == null) {
            return;
        }

        nextState.getAllIncludingChildWidgets().forEach(w -> markAsDeleted(w));
    }

    protected void handleMergeCreation(AppState sharedState, AppState sessionState, String widgetId) {
        Widget createdWidget = sessionState.getWidget(widgetId);
        int foundIndex = indexOfSameWidget(createdWidget, sharedState.getVisibleActions());
        boolean isPresentInSharedState = foundIndex >= 0;
        
        if (isPresentInSharedState) {
            String widgetIdShared = sharedState.getVisibleActions().get(foundIndex).getId();
            handleMergeChange(sharedState, sessionState, widgetIdShared, widgetId);
            return;
        }

        sharedState.addWidget(createdWidget);
    }

    protected void handleMergeNoChange(AppState sharedState, AppState sessionState, String widgetId) {
        Widget originalWidget = sharedState.getWidget(widgetId);
        Widget otherWidget = sessionState.getWidget(widgetId);
        
        AppState nextStateFromShared = null;
        AppState nextStateFromSession = null;
        
        if (originalWidget != null) {
            nextStateFromShared = originalWidget.getNextState();
        }
        if (otherWidget != null) {
            nextStateFromSession = otherWidget.getNextState();
        }

        doMergeStateChangesIntoShared(nextStateFromShared, nextStateFromSession);
    }

    protected void handleMergeChange(AppState sharedState, AppState sessionState, String widgetIdShared ,String widgetId) {
        Widget widgetFromShared = sharedState.getWidget(widgetIdShared);
        Widget widgetFromSession = sessionState.getWidget(widgetId);
        Widget mergedWidget = mergeSameWidgets(widgetFromShared, widgetFromSession);
        mergedWidget.setNextState(widgetFromSession.getNextState());
        doMergeStateChangesIntoShared(widgetFromShared.getNextState(), widgetFromSession.getNextState());
    }

    protected List<Widget> getAllVisibleActionsRecursive(Widget widget) {
        List<Widget> allWidgets = new LinkedList<>();

        if (widget == null || widget.getNextState() == null) {
            return allWidgets;
        }

        AppState nextState = widget.getNextState();
        List<Widget> widgets = nextState.getVisibleActions();
        allWidgets.addAll(widgets);

        for (Widget item : widgets) {
            allWidgets.addAll(getAllVisibleActionsRecursive(item));
        }

        return allWidgets;
    }

    @SuppressWarnings("unchecked")
    protected Map<String, DiffType> getDiffMetaDataFromState(AppState state) {
        try {
            Map<String, DiffType> diff = (Map<String, DiffType>)state.getMetadata(META_DATA_DIFF);
            return Optional.ofNullable(diff).orElseGet(() -> new HashMap<String, DiffType>());
        } catch (ClassCastException e) {
            log("Unable to cast meta-data object as Map<String, DiffType> in state with id " + state.getId());
            return new HashMap<>();
        } 
    }

    protected int indexOfSameWidget(Widget widget, List<Widget> list ) {
       for (int i = 0; i < list.size(); i++) {
            if (isSameWidget(widget, list.get(i))) {
                return i;
            }   
       }
        
        return -1;
    }

    protected boolean hasEqualMetaData(String key, Widget widget, Widget other) {
        return String.valueOf(widget.getMetadata(key)).equals(String.valueOf(other.getMetadata(key)));
    }

    protected void markAsDeleted(Widget widget) {
        if (widget == null) {
            return;
        }

        widget.putMetadata(DELETED_AT, Instant.now().toEpochMilli());
    }

    protected boolean isMarkedAsDeleted(Widget widget) {
        try {
            long epochMilli = (long)widget.getMetadata(DELETED_AT);
            return epochMilli > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private void log(String message) {
        String now = formatter.format(new Date());
        System.out.printf("[%s] %s \n", now, message);
    }

    @SuppressWarnings("unchecked")
    protected <T extends Serializable> T deepCopy(T original) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(original);
            
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream in = new ObjectInputStream(bis);
            return (T) in.readObject();
        } catch (Exception e) {
            log("Unable to create a deep copy of an object: " + e.getMessage());
            return null;
        }
    }
}
