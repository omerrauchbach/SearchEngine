package Main;


import Part_1.Parse;
import javafx.scene.control.Alert;

import java.io.*;

public class Main {


    public static void main(String[] args){
        String regaxExp =  ("(?!/[0-9])(?!,[0-9])[?!:,#`@^~&+{*}|\"<=>\\s;()_\\\\\\[\\]]+");
        File rootDirectory = new File(pathDir + "/corpus");
        File[] allDirectorys = rootDirectory.listFiles();
        if (allDirectorys != null) {
            for (File file : allDirectorys) {
                File[] current = file.listFiles(); // gets the file itself, inside the corpus directory
                if (current != null) {
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(current[0])));
                        allLinesInDoc = new StringBuilder();
                        for ( String currLine ; (currLine = bufferedReader.readLine()) != null; )
                            allLinesInDoc.append( currLine + System.lineSeparator() );
                        bufferedReader.close();
                        // add the document
                        Parse.documentsSet.add(createDoc());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

        else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error in folder path");
            alert.show();
        }

}

