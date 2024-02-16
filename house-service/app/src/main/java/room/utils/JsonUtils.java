package room.utils;

import com.google.gson.JsonParser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonUtils {

	public static String getJsonWithTimestamp(String json) throws Exception {
		JsonElement jsonEl = JsonParser.parseString(json);
		if (jsonEl.isJsonObject()) {
			JsonObject jsonObj = jsonEl.getAsJsonObject();
			jsonObj.addProperty("timestamp", System.currentTimeMillis());
			return jsonObj.toString();
		}
		return null;
	}

	public static boolean isFromArduino(String json) {
		try {
			JsonObject jsonObj = JsonParser.parseString(json).getAsJsonObject();
			jsonObj.addProperty("timestamp", System.currentTimeMillis());
			return jsonObj.get("name").getAsString() != "photo_resistor"
					|| jsonObj.get("name").getAsString() != "pir_sensor";
		} catch (Exception e) {
			return false;
		}
	}

}
