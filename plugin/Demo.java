// SPDX-FileCopyrightText: 2020 Andreas Bauer
//
// SPDX-License-Identifier: MIT

package plugin;

import org.json.simple.JSONObject;
import scout.*;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class Demo {

    private static final Font textBoxFont = new Font("Arial", Font.PLAIN, 18);
    private static final Color deepOrange = new Color(255, 87, 34, 200);
    private static final int textBoxWidth = 400;
    private static final String LOG_FILE = "demo-plugin-log.txt";
    private static final SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss");
    private static final Map<String, Integer> totalActionsCounter = new HashMap<String, Integer>() {{
        put(LeftClickAction.class.getSimpleName(), 0);
        put(RightClickAction.class.getSimpleName(), 0);
    }};

    private static int paintCounter = 0;
    private static int globalStateCounter = 0;
    private static List<Widget> globalIssues = new ArrayList<Widget>();
    private static List<Widget> currentIssues = new ArrayList<Widget>();
    private static List<Widget> currentVisibleActions = new ArrayList<Widget>();

    private int lineOffset = 0;
    private final int lineOffsetSpace = 10;

    public String hello() {
        String msg = Say.sayHello();
        return msg;
    }

    /**
     * Called when the plugin is enabled
     */
    public void enablePlugin() {
        String msg = "Demo Plugin Enabled";
        StateController.displayMessage(msg);
        log(msg);
    }

    /**
     * Called when the plugin is disabled
     */
    public void disablePlugin() {
        String msg = "Demo Plugin Disabled";
        StateController.displayMessage(msg);
        log(msg);
    }

    /**
     * Called when the session begins
     */
    public void startSession() {
        log("*** NEW SESSION ***");
        StateController.displayMessage("Session Started");
        crawlInformation();
    }

    /**
     * Called when the session is stopped
     */
    public void stopSession() {
        String msg = "Session Stopped";
        StateController.displayMessage(msg);
        log(msg);
    }

    /**
     * Called when the session is paused
     */
    public void pauseSession() {
        String msg = "Session Paused";
        StateController.displayMessage(msg);
        log(msg);
    }

    /**
     * Called when the session is resumed after being paused
     */
    public void resumeSession() {
        String msg = "Session Resumed";
        StateController.displayMessage(msg);
        log(msg);

        crawlInformation();
    }

    /**
     * Called when the state changes
     */
    public void changeState() {
        crawlInformation();

        AppState state = StateController.getCurrentState();
        String stateId = state.getId();
        state.getMetadataKeys().stream()
                .forEach(k -> log("State ID: " + stateId + " Key: " + k + " Value: " + state.getMetadata(k)));

        state.getVisibleActions().stream().forEach(a -> {
            log("State ID: " + stateId + " Action: " + a.getWidgetType());
            a.getMetadataKeys().stream().forEach(k -> log("\t Metadata Key: " + k + " Value: " + a.getMetadata(k).toString()));
        });
    }

    private void crawlInformation() {
        AppState globalState = StateController.getStateTree();
        AppState state = StateController.getCurrentState();

        globalStateCounter = globalState.getStateCount();

        globalIssues = globalState.getAllIssues();
        currentIssues = state.getAllIssues();
        currentVisibleActions = state.getVisibleActions();
    }

    /**
     * Called when an action is performed
     */
    public void performAction(Action action) {
        if (!StateController.isRunningSession()) {
            return;
        }

        String actionName = action.getClass().getSimpleName();

        if (!totalActionsCounter.containsKey(actionName)) {
            totalActionsCounter.put(actionName, 0);
        }

        int counter = totalActionsCounter.get(actionName) + 1;
        totalActionsCounter.put(actionName, counter);
    }

    public void paintCaptureForeground(Graphics g) {
        if (!StateController.isOngoingSession()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        paintCounter++;

        int x = 50;
        int y = 100;
        int dotXOffset = 25;

        drawTextLine(g2, "< GLOBAL STATE >", x, y);
        drawTextLine(g2, "Product: " + StateController.getProduct(), x, y);

        int leftClicks = totalActionsCounter.get(LeftClickAction.class.getSimpleName());
        int rightClicks = totalActionsCounter.get(RightClickAction.class.getSimpleName());
        drawTextLine(g2, "Total Clicks: L= " + leftClicks + " R= " + rightClicks, x, y);

        drawTextLine(g2, "Paint Counter: " + paintCounter, x, y);

        drawTextLine(g2, "Number of Nodes: " + globalStateCounter, x, y);

        drawTextLine(g2, "Total Issues: " + globalIssues.size(), x, y);
        drawDot(g2, x - dotXOffset, getDotYWithOffset(y), getIssuesColor(globalIssues));

        drawTextLine(g2, "──────────────────────────────", x, y);
        drawTextLine(g2, "< CURRENT STATE >", x, y);

        drawTextLine(g2, "Issues: " + currentIssues.size(), x, y);
        drawDot(g2, x - dotXOffset, getDotYWithOffset(y), getIssuesColor(currentIssues));

        currentIssues.stream().forEach(i -> drawTextLine(g2, "├ Reported: " + i.getReportedText(), x, y));

        drawTextLine(g2, "Visible Actions: " + currentVisibleActions.size(), x, y);
        for (Widget w : currentVisibleActions) {
            drawTextLine(g2, "├ Status: " + w.getWidgetStatus(), x, y);
            drawTextLine(g2, "└ Type: " + w.getWidgetSubtype(), x, y);
        }
    }

    private Color getIssuesColor(List<Widget> issues) {
        if (issues.size() != 0) {
            return Color.RED;
        }

        return Color.GREEN;
    }

    private int getDotYWithOffset(int y) {
        return y + lineOffset - (textBoxFont.getSize() * 2) - (textBoxFont.getSize() / 2) + lineOffsetSpace / 2;
    }

    private void drawTextLine(Graphics2D g2, String text, int x, int baseY) {
        int y = baseY + lineOffset;

        drawTextLineBackground(g2, x, y);

        g2.setFont(textBoxFont);
        g2.setColor(Color.white);
        g2.drawString(text, x, y);

        lineOffset += textBoxFont.getSize() + lineOffsetSpace;
    }

    private void drawTextLineBackground(Graphics2D g2, int x, int y) {
        int height = textBoxFont.getSize() + lineOffsetSpace;
        int margin = 5;

        g2.setColor(deepOrange);
        g2.fillRect(x - margin, y - height + margin, textBoxWidth, height);
    }

    private void drawDot(Graphics2D g2, int x, int y, Color color) {
        int size = g2.getFont().getSize();

        g2.setColor(Color.black);
        g2.fillOval(x, y, size, size);

        g2.setColor(color);
        g2.fillOval(x + 2, y + 2, size - 4, size - 4);
    }

    /**
     * Called when a report should be generated
     * Adding this method will display this plugin in the Reports drop-down
     */
    public void generateReport() {
        String reportFolderPath = "reports/" + StateController.getProduct();
        createFolder(reportFolderPath);

        String report = getJSONReport();
        try {
            String filename = reportFolderPath + "/demo-plugin-report.json";
            FileWriter fileWriter = new FileWriter(filename);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.print(report);
            printWriter.close();
            StateController.displayMessage("Generating report: " + filename);
        } catch (Exception e) {
            String msg = "Unable to generate report: " + e.getMessage();
            log(msg);
            StateController.displayMessage(msg);
        }
    }

    @SuppressWarnings("unchecked")
    private String getJSONReport() {
        JSONObject report = new JSONObject();
        report.put("product", StateController.getProduct());
        report.put("total-actions", totalActionsCounter);

        List<String> issueTexts = globalIssues.stream()
                .map(i -> i.getReportedText())
                .collect(Collectors.toList());
        report.put("issues", issueTexts);

        int coveredPercent=StateController.getStateTree().coveredPercent(StateController.getProductVersion());
        report.put("coverageInPercent", coveredPercent);

        return report.toJSONString();
    }

    private void createFolder(String pathname) {
        File file = new File(pathname);
        file.mkdirs();
    }

    private void log(String message) {
        try {
            FileWriter fileWriter = new FileWriter(LOG_FILE, true);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            doWriteLog(printWriter, message);
            printWriter.close();
        } catch (Exception e) {
            // do nothing
        }
    }

    private void doWriteLog(PrintWriter writer, String message) {
        String now = formatter.format(new Date());
        writer.printf("[%s] %s \n", now, message);
    }
}
