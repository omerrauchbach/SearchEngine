package Part_1;

import javafx.scene.control.Alert;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

/**
 * This class reads all the files in the directory's path
 */

public class ReadFile {

    private String pathDir;
    private StringBuilder allLinesInDoc;
    //private HashMap<String,Integer> notCities = new HashMap<>();

    /**
     * the constructor of the class
     *
     * @param pathDir - the path of the directory the corpus is found in
     */
    public ReadFile(String pathDir) {
        this.pathDir = pathDir;

    }

    // reads all the files inside the corpus directory
    public void readInsideAllFiles() {

        File rootDirectory = new File(pathDir + "\\corpus_1");
        File[] allDirectories = rootDirectory.listFiles();

        if (allDirectories != null) {
            allLinesInDoc = new StringBuilder();
            for (File file : allDirectories) {
                File[] current = file.listFiles(); // gets the file itself, inside the corpus directory
                if (null != current) {
                    try {
                        BufferedReader myBufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(current[0])));
                        for ( String currLine ; (currLine = myBufferedReader.readLine()) != null; )
                            allLinesInDoc.append( currLine + System.lineSeparator() );
                        createDoc();

                        myBufferedReader.close();


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error in folder path");
            alert.show();
        }
    }

    private void createDoc(){
        String docId;
        String text;
        int startInd = allLinesInDoc.indexOf("<DOC>");
        while (startInd != -1) {

            Document newDoc = new Document();
            int endInd = allLinesInDoc.indexOf("</DOC>", startInd); //searches for "</DOC>" from starts index
            String currDoc = allLinesInDoc.substring(startInd, endInd);
            //set Id

            int startNumTag =  allLinesInDoc.indexOf("<DOCNO>",startInd);
            int endNumTag = allLinesInDoc.indexOf("</DOCNO>" ,startInd);
            if(startNumTag == -1 || endNumTag == -1)
                newDoc.setId("");
            else {
                String id = allLinesInDoc.substring(startNumTag + 7, endNumTag).trim();
                newDoc.setId(id);
            }


            // gets the document's <TEXT></TEXT> tags
            if (currDoc.contains("<TEXT>")) {
                int startOfText = currDoc.indexOf("<TEXT>");
                while (startOfText != -1) {
                    int endOfText = currDoc.indexOf("</TEXT>");
                    String docText = currDoc.substring(startOfText + 6, endOfText).trim();
                    if (docText.length() > 0)
                        newDoc.addText(docText);
                    startOfText = currDoc.indexOf("<TEXT>", endOfText);
                }
            }
            Parse.documentsSet.add(newDoc);    // adds the specific document
            startInd = allLinesInDoc.indexOf("<DOC>", endInd); //continues to the next doc in file
        }


    }

    private String getDocId(String doc){

        int startNumTag =  doc.indexOf("<DOCNO>");
        int endNumTag = doc.indexOf("</DOCNO>");
        if(startNumTag == -1 || endNumTag == -1)
            return "";
        else
            return (doc.substring(startNumTag+7 , endNumTag)).trim();
    }


}