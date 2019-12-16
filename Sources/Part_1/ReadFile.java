package Part_1;

import javafx.scene.control.Alert;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

/**
 * This class reads all the files in the directory's path
 */

public class ReadFile extends Thread {

    private String pathDir;
    private StringBuilder allLinesInDoc;
    public static boolean stopParser = false;



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
        System.out.println("ReadFile");
        if (allDirectories != null) {
            allLinesInDoc = new StringBuilder();
            for (File file : allDirectories) {
                File[] current = file.listFiles(); // gets the file itself, inside the corpus directory
                if (null != current) {
                    for (File txtfile : current) {
                        try {
                            BufferedReader myBufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(txtfile)));
                            for (String currLine; (currLine = myBufferedReader.readLine()) != null; )
                                allLinesInDoc.append(currLine + System.lineSeparator());
                            createDoc();
                            allLinesInDoc = new StringBuilder();
                            myBufferedReader.close();


                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }


        }
        else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error in folder path");
            alert.show();
        }

        stopParser = true;
    }

    private void createDoc(){

        String text;
        String id = "";
        int startInd = allLinesInDoc.indexOf("<DOC>");
        while (startInd != -1) {

            Document newDoc = new Document();
            int endInd = allLinesInDoc.indexOf("</DOC>", startInd); //searches for "</DOC>" from starts index
            String currDoc = allLinesInDoc.substring(startInd, endInd);

            //set Id <DOCNO>, </DOCNO>"
            int startIndexId =  currDoc.indexOf("<DOCNO>");
            int endIndexId = currDoc.indexOf("</DOCNO>" );
            if(startIndexId == -1 || endIndexId == -1)
                newDoc.setId("");
            else {
                id = currDoc.substring(startIndexId + 7, endIndexId).trim();
                newDoc.setId(id);
            }

            //set Title <TI>,</TI>
            int startIndexTitle =  currDoc.indexOf("<TI>",startInd);
            int endIndexTitle = currDoc.indexOf("</TI>" ,startInd);
            if(startIndexTitle == -1 || endIndexTitle == -1)
                newDoc.setTitle("");
            else {
                String title = currDoc.substring(startIndexTitle + 7, endIndexTitle).trim();
                newDoc.setTitle(title);
            }


            // gets the document's <TEXT></TEXT> tags
            if (currDoc.contains("<TEXT>")) {
                int startOfText;
                int addStart =6;
                if (currDoc.contains("<F P=106>") || currDoc.contains("<F P=105>") ) {
                    startOfText = currDoc.indexOf("[Text]");
                    if(currDoc.contains("[Excerpt]")) {
                        startOfText = currDoc.indexOf("[Excerpt]");
                        addStart = 9;
                    }
                    else if(currDoc.contains("[Excerpts]")){
                        startOfText = currDoc.indexOf("[Excerpts]");
                        addStart = 10;
                    }

                }else
                    startOfText = currDoc.indexOf("<TEXT>");
                int endOfText = currDoc.indexOf("</TEXT>");
                String docText = currDoc.substring(startOfText + addStart, endOfText).trim();
                if (docText.length() > 0)
                    newDoc.setText(docText);
            }
            try {// adds the specific document

                Parse.documentsSet.put(newDoc);
                System.out.println(newDoc.getId() + ":ReadFile");
            }
            catch(IllegalStateException e){
                //Queue full
                e.printStackTrace();
            }
            catch (IllegalMonitorStateException e) {
                e.printStackTrace();
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }

            startInd = allLinesInDoc.indexOf("<DOC>", endInd); //continues to the next doc in file
        }


    }

    private String getDocId(String doc){

        int startIndexId =  doc.indexOf("<DOCNO>");
        int endIndexId = doc.indexOf("</DOCNO>");
        if(startIndexId == -1 || endIndexId == -1)
            return "";
        else
            return (doc.substring(startIndexId+7 , endIndexId)).trim();
    }

    public void run(){
        readInsideAllFiles();
    }

    public static void restart(){
        stopParser = false;
    }


}