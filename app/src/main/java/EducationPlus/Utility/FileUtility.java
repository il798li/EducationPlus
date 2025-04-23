package EducationPlus.Utility;

import EducationPlus.Main;

import java.io.*;
import java.util.Scanner;

public class FileUtility {

    public static String readFile (String path) {
        File file = file (path);
        Scanner scanner;
        try {
            scanner = new Scanner (file);
        } catch (final FileNotFoundException fileNotFoundException) {
            return "Exception: FileNotFoundException";
        }
        StringBuilder content = new StringBuilder ();
        while (scanner.hasNextLine ()) {
            content.append (scanner.nextLine ());
        }
        scanner.close ();
        return content.toString ();
    }

    public static File file (String path) {
        path = "app\\src\\main\\java\\EducationPlus\\" + path;
        File file = null;
        try {
            file = new File (path);
        } catch (final NullPointerException nullPointerException) {
            Main.debug ("Could not open \"" + path + "\" due to a NullPointerException.");
        }
        return file;
    }

    public static void writeFile (final String content, final String path) {
        File file = file (path);
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter (file);
            BufferedWriter bufferedWriter = new BufferedWriter (fileWriter);
            bufferedWriter.write (content);
            bufferedWriter.close ();
        } catch (IOException ioException) {
            Main.debug ("Could not write to file \"" + path + "\" due to an IOException.");
        }
    }
}
