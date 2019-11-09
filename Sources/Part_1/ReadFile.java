package Part_1;

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
    private void readInsideAllFiles() {

        File directory = new File(pathDir + "/corpus");
        File[] allFiles = directory.listFiles();
        if (allFiles != null) {
            for (File file : allFiles) {
                File[] current = file.listFiles(); // gets the file itself, inside the corpus directory
                if (current != null) {
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(current[0])));
                        String currLine;
                        StringBuilder allLinesInDoc = new StringBuilder();
                        currLine = bufferedReader.readLine();
                        while (currLine != null) {
                            allLinesInDoc.append(currLine);
                            //allLinesInDoc.append(System.lineSeparator());
                            allLinesInDoc.append("\n");
                            currLine = bufferedReader.readLine();
                        }
                        String allTextInFile = allLinesInDoc.toString();

                        bufferedReader.close();
                        int startOfDoc = allLinesInDoc.indexOf("<DOC>");
                        parsingTheDoc(startOfDoc);
                    } catch (IOException e) {
                        ///// ???
                    }
//                }
//        for (File document : allFiles) {
//            if (document.isFile()) {
//                // System.out.println(document.getName()); //////// ?????
//            }
//        }
                }
            }
        }
    }

    private void parsingTheDoc (int startInd){

        while (startInd != -1){
            int endInd = allLinesInDoc.indexOf("</DOC>", startInd); //searches for "</DOC>" from starts index
            String currDocText = allLinesInDoc.substring(startInd, endInd);
            // gets the document's <TEXT></TEXT> tags
            if (currDocText.contains("<TEXT>")) {
                int startOfText = currDocText.indexOf("<TEXT>");
                while (startOfText != -1) {
                    int endOfText = currDocText.indexOf("</TEXT>");
                    String docText = currDocText.substring(startOfText+6, endOfText);

                    startOfText = currDocText.indexOf("<TEXT>", endOfText); //searches for the next TEXT part in doc

                   // if (!docText.equals(""))
                   //     newDoc.addDocText(docText);

                }
            }
            startInd = allLinesInDoc.indexOf("<DOC>", endInd); //continues to the next doc in file
        }
    }
}