// SPDX-FileCopyrightText: 2021 Andreas Bauer
//
// SPDX-License-Identifier: MIT

package plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import scout.AppState;
import scout.StateController;

import static plugin.JSONStateParser.*;

public class MultiUser {

    private static final String DATA_FILEPATH = "data";
	private static final String MODEL_FILENAME = "state.json";
	private static final String PRODUCT_PROPERTIES_FILE = "product.properties";

    public MultiUser() {
        StateController.setProducts(getFolders(DATA_FILEPATH));
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
			String filepath = projectRootPath + "/" + PRODUCT_PROPERTIES_FILE;
			FileInputStream in = new FileInputStream(filepath);
			productProperties.load(in);
			in.close();
			return productProperties;
		} catch (Exception e) {
			return new Properties();
		}
	}

	private JSONObject loadJSONModel(String filepath) {
		JSONParser jsonParser = new JSONParser();
		JSONObject jsonState = null;
		try
		{
			FileReader reader = new FileReader(filepath);			
			jsonState = (JSONObject) jsonParser.parse(reader);
			reader.close();
		}
		catch (Exception e)
		{
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

		String filePath = getFilePathForProduct(product);
		
		// Make sure that folders exist
		File file=new File(filePath);
		file.mkdirs();
		
		// Add filename
		filePath+="/"+MODEL_FILENAME;

		// Save state tree
		if(!saveObject(filePath, stateTree))
		{
			return false;
		}
		
		// Get weekday number
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
		
		// Save a backup
		filePath+=dayOfWeek;
		if(!saveObject(filePath, stateTree))
		{
			return false;
		}

		// Update products
		StateController.setProducts(getFolders(DATA_FILEPATH));
		
		return true;
	}

	private boolean saveObject(String filepath, AppState appState) {
		String jsonState = "";
		try {
			jsonState = appStateAsJSONObject(appState).toJSONString();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
            FileWriter fileWriter = new FileWriter(filepath);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.print(jsonState);
            printWriter.close();
        } catch (Exception e) {
			e.printStackTrace();
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
    
}
