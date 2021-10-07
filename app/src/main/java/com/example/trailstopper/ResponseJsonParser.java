package com.example.trailstopper;

import org.json.JSONException;
import org.json.JSONObject;

public class ResponseJsonParser {
    static JSONObject parseIntoJson(String content) {
        String json_piece = content.split("root.App.main =")[1].split("\\(this\\)")[0].split("\\;\\n\\}")[0].trim();
        try {
            JSONObject jObject = new JSONObject(json_piece);
            JSONObject obj = jObject.getJSONObject("context").getJSONObject("dispatcher").getJSONObject("stores").getJSONObject("QuoteSummaryStore");
            return obj;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
