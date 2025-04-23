package EducationPlus.Utility;

import EducationPlus.Main;
import org.json.JSONObject;

public class JSONUtility {

    public static void save (JSONObject jsonObject, JSONFile jsonFile) {
        final String jsonString = jsonObject.toString (4);
        final String jsonFileString = "JSON\\" + jsonFile.toString () + ".json";
        FileUtility.writeFile (jsonString, jsonFileString);
    }

    public static JSONObject load (final JSONFile jsonFile) {
        final String jsonFileString = "JSON\\" + jsonFile + ".json";
        final String jsonString = FileUtility.readFile (jsonFileString);
        Main.debug (jsonString);
        return new JSONObject (jsonString);
    }

    public enum JSONFile {
        Blacklisted,
        Settings,
        Lessons,
        Tests
    }
}
