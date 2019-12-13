package Part_1;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;

public class Indexer extends Thread {

    public static HashMap<String, int[]> termDic = new HashMap<>(); // 0 - #docs, 1- #showsTotal, 2- line in posting

    public static HashMap<String, int[]> allDocuments = new HashMap<>(); // 0 - maxTF, 1- #uniqueTerms, 2- length of doc


    private HashMap<String, String> ChunkTermDicDocs = new HashMap<>(); //
    private String currDocName = "";
    private Document currDoc;
    private HashMap<String, int[]> currDocDic = new HashMap<>();
    private String FILE_PATH = "";
    public static String filePath = ""; //get it from parse. !

    private int indexPosting;
    private int[] termInfo;
    private int[] updateTermInfo;
    public static BlockingQueue<Document> currChunk = new LinkedBlockingQueue<>(20);

    private TreeMap<String, int[]> littleDic;
    private int currPostingFileIndex = 1;
    private int counterPostingFiles;
    String allPostingPath = "";
    public static boolean stopIndexer = false;
    private Path path = null;
    private boolean finishChunk = false;

    public Indexer(String postingPath){
        FILE_PATH = postingPath;
    }

    private void indexAll() {

        while (!stopIndexer) {
            indexPosting = 1; // line number in posting file
            counterPostingFiles = 1; // counter for posting files.
            //FILE_PATH = "C:\\Users\\Tali\\IdeaProjects\\SearchEngine\\out\\posting" + currPostingFileIndex + ".txt";
            FILE_PATH = filePath + "\\out\\posting" + currPostingFileIndex + ".txt";

            allPostingPath = filePath + "\\out";
            path = Paths.get(FILE_PATH);

            String termToAdd = "";
            String allInfoOfTermForPosting = "";
            String allPlacesInDoc = "";
            littleDic = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

            int[] docInfo;
            while (!finishChunk) {
                while (currChunk != null && !currChunk.isEmpty()) { //indexes all docs in the queue (CHUNK)

                    finishChunk = false; //still has docs to index.

                    currDoc = currChunk.poll();
                    currDocDic = currDoc.getAllTerms();
                    currDocName = currDoc.getId();

                    docInfo = new int[3];
                    docInfo[0] = currDoc.getTfMax();
                    docInfo[1] = currDocDic.keySet().size(); //how many unique terms.
                    docInfo[2] = currDoc.getLength();
                    allDocuments.put(currDocName, docInfo); //adds current doc to docs dic.

                    for (String key : currDocDic.keySet()) {

                        if (littleDic.containsKey(key)) {

                            updateTermInfo = new int[4];
                            updateTermInfo[0] = littleDic.get(key)[0] + 1; // adds 1 to curr # of docs
                            updateTermInfo[1] = littleDic.get(key)[1] + currDocDic.get(key)[0]; //#shows total == adds num of appearences in specific doc. !!!
                            updateTermInfo[2] = littleDic.get(key)[2]; //same line of old term in doc.
                            littleDic.replace(key, updateTermInfo); //replaces values in little dic

                            allPlacesInDoc = currDoc.termPlacesInDoc.get(key);
                            allInfoOfTermForPosting = ChunkTermDicDocs.get(key) + "|" + currDocName + ":" + termInfo[1] + ";" + allPlacesInDoc;
                            ChunkTermDicDocs.replace(key, allInfoOfTermForPosting); // updates info for posting.

                        } else { //first time of this term in chunk.
                            termInfo = new int[4];
                            termInfo[0] = 1; // first doc in list
                            termInfo[1] = currDocDic.get(key)[0]; //num of appearances in specific doc. !!!

                            littleDic.put(key, termInfo); //first doc in list, for posting!

                            allPlacesInDoc = currDoc.termPlacesInDoc.get(key);
                            allInfoOfTermForPosting = currDocName + ":" + termInfo[1] + ";" + allPlacesInDoc;
                            ChunkTermDicDocs.put(key, allInfoOfTermForPosting); //info for posting.
                        }
                    }
                }
                currChunk = null;
                finishChunk = true; ////only if there was actually a new chunk docs. not if just waiting.
            }

            currPostingFileIndex++;

            //Q is empty, now moves to big doc: //// write to postinggggggg !!!!!!!!!!!
            boolean newFilePosting = true;
            for (String key : littleDic.keySet()) { //adds all curr Chunk's dic to big dic.

                //try { // write to posting file.
                termToAdd = key + "|" + ChunkTermDicDocs.get(key) + "\n"; //string .... // doc1:tf;1,46,89|doc5:tf;6,72
                FILE_PATH = filePath + "\\out\\posting" + currPostingFileIndex + ".txt";
                if (newFilePosting == true) {
                    if (createFile(termToAdd) == true) {
                        //Files.write(path, termToAdd.getBytes(), StandardOpenOption.APPEND); /// אמור להיות בדיוק בשורה שהיא האינדקס :O
                        //indexPosting++; // updates curr line in posting.
                        newFilePosting = false;
                    }
                } else {
                    try {
                        path = Paths.get(FILE_PATH);
                        Files.write(path, termToAdd.getBytes(), StandardOpenOption.APPEND);
                    } catch (IOException e) {
                    }
                }
            }
            littleDic = null;
            counterPostingFiles = new File(allPostingPath).listFiles().length - 1; //"production" always there.

            File f = new File(allPostingPath);
            if(f.exists() && !f.isDirectory()) {

                if (counterPostingFiles == 2) { //now merge !
                    mergePosting();

                }
            }


        } //nothing left to index. // one merged posting file::

        termDic = null; //initialize.
        //FILE_PATH = "C:\\Users\\Tali\\IdeaProjects\\SearchEngine\\out\\postingFile.txt";

/*     File[] lastFile = new File("C:\\Users\\Tali\\IdeaProjects\\SearchEngine\\out").listFiles();
    if (lastFile != null && lastFile.length > 0) {
        File oldMerged = new File("C:\\Users\\Tali\\IdeaProjects\\SearchEngine\\out\\posting" + currPostingFileIndex + ".txt");
        File lastOne = files[0];
        merged.renameTo(oldMerged); //only file that left.
        createDic(); //most important part.
    }*/

        createDic(); //most important part.
    }


    private void mergePosting () {

        counterPostingFiles = new File(allPostingPath).listFiles().length - 1; //"production" always there.
        if (counterPostingFiles == 2) { //now merge ! /////// change to 2 !!!!!!!!!!!!!!!!!!

            String output = "";
            String term1 = "";
            String term2 = "";

            try {
                int oldPostingIndex = currPostingFileIndex-1;
                Scanner sc1 = new Scanner((new File(filePath + "\\out\\posting" + currPostingFileIndex + ".txt")));
                Scanner sc2 = new Scanner((new File(filePath + "\\out\\posting" + oldPostingIndex + ".txt")));
                String line1 = sc1.next();
                String line2 = sc2.next();
                term1 = line1.substring(0, line1.indexOf("|")); // only term itself, with no other data.
                term2 = line2.substring(0, line2.indexOf("|")); // only term itself, with no other data.

                while (sc1.hasNext() && sc2.hasNext()) {

                    if (term1.compareTo(term2) < 0) { //term1 is first in dic.
                        output = output + line1 + "\n";
                        line1 = sc1.next();
                        term1 = line1.substring(0, line1.indexOf("|")); // only term itself, with no other data.

                    } else if (term1.compareTo(term2) > 0) {
                        output = output + line2 + "\n";
                        line2 = sc2.next();
                        term2 = line2.substring(0, line2.indexOf("|")); // only term itself, with no other data.

                    }
                    else { //same term ! // adds both list of docs and data.
                        output = output + term1 + line1.substring(line1.indexOf("|") + 1) + line2.substring(line2.indexOf("|") + 1) + "\n";
                        line1 = sc1.next();
                        line2 = sc2.next();
                        term1 = line1.substring(0, line1.indexOf("|")); // only term itself, with no other data.
                        term2 = line2.substring(0, line2.indexOf("|")); // only term itself, with no other data.
                    }
                }
                while (sc1.hasNext()) { //adds only terms from 1.
                    line1 = sc1.next();
                    output = output + line1 + "\n";
                }
                while (sc2.hasNext()) { //adds only terms from 2.
                    line2 = sc2.next();
                    output = output + line2 + "\n";
                }
                sc1.close();
                sc2.close();

            } catch (IOException ex) {
                // Report
            }

            currPostingFileIndex++;
            FILE_PATH = filePath + "\\out\\posting" + currPostingFileIndex+ ".txt"; //new file, number

            if (createFile(output) == true) { //"posting+1.txt"
                deleteTwoTempPosting("posting" + currPostingFileIndex + ".txt");
            }
        }
    }

    private boolean createFile (String terms) {
        Writer writer = null;
        boolean isCreated = false;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FILE_PATH), "utf-8")); //new file number
            writer.write(terms);
            //((BufferedWriter)writer).newLine();
            isCreated = true;

        } catch (IOException ex) {
            // Report
        } finally {
            try {
                writer.close();
            } catch (Exception ex) {
                //ignore/}
            }
            return isCreated;
        }
    }

    private void deleteTwoTempPosting (String postingPath){ //which file not to delete!!
        String allPosting = filePath + "\\out\\posting";

        File mainDir = new File(allPosting);
        File[] files = mainDir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file != null && !(file.getName().equals(postingPath)) && !file.isDirectory()) { //deletes 2 files but not the curr merged one.!
                    file.delete();
                }
            }
            currPostingFileIndex++; //now 4 ?
            //File oldMerged = new File("C:\\Users\\Tali\\IdeaProjects\\SearchEngine\\out\\posting" + currPostingFileIndex + ".txt");
            //File merged = files[0];
            //merged.renameTo(oldMerged); //only file that left.
            counterPostingFiles = 1; //only one file left.
        }
    }


    private void createDic (){ //create the final dic. at the end.

        termDic = new HashMap<>();
        int[] valuesForTerm = new int [3];
        FILE_PATH = filePath + "\\out\\posting" + currPostingFileIndex + ".txt"; // final file.
        try{
            Scanner textScan = new Scanner(new File(FILE_PATH)); //only one merged posting file ! with all terms !
            int lineIndex = 0;
            int df ;
            int totalShows;
            String[] allData;
            String term = "";
            boolean newTerm;

            while (textScan.hasNextLine()) {
                newTerm = true;
                lineIndex++;
                String str = textScan.nextLine();
                df = 0;
                totalShows = 0;

                while (str != null && str.length()>0) {
                    //alldata = new String[str.length()]; /////////// ????????????????????
                    allData = str.split(":|\\;|\\|");

                    if (newTerm) { //only start of reading line. removes term itself ! (index 0 )
                        term = allData[0];
                        str = str.substring(str.indexOf("|") + 1);
                        newTerm = false;
                    }
                    while (str != null) { //one more doc to add.
                        allData = str.split(":|\\;|\\|"); //line without term itself.
                        df++;
                        totalShows = totalShows + Integer.parseInt(allData[1]); //adds #appearences in each doc.

                        if (str.indexOf("|") == -1) {
                            str = null;
                            break;
                        }
                        str = str.substring(str.indexOf("|") + 1); //shortens the line, reads next doc info.

                    }
                } //end of line.
                valuesForTerm[0] = df;
                valuesForTerm[1] = totalShows;
                valuesForTerm[2] = lineIndex;

                termDic.put(term,valuesForTerm); //adds term and values to big dic.
            }
        } catch (IOException ex) {
            // Report
        }
    }

    public void test (){
//        /*Queue<Document> docs = new LinkedList<Document>();
//        HashMap<String, int[]> tDic = new HashMap<>();
//
//        Document d1 = new Document();
//        Document d2 = new Document();
//        Document d3 = new Document();
//
//        int[] test = new int[3];
//        test[0] = 1;
//        test[1] = 0;
//        test[2] = 2;
//
//        StringBuilder sb = new StringBuilder();
//        tDic.put("Abba", test);
//
//        int[] test2 = new int[3];
//        test2[0] = 4;
//        test2[1] = 0;
//        test2[2] = 2;
//
//        tDic.put("Ima",test2);
//
//        d1.setId("FB396005");
//        d1.setText(sb);
//        d1.setTfMax(9);
//        d1.setTermDic(tDic);
//
//        StringBuilder sb2 = new StringBuilder();
//        HashMap<String, int[]> tDic2 = new HashMap<>();
//
//        tDic2.put("Ima" , test);
//        tDic2.put("Abba", test);
//        tDic2.put("Tomer", test);
//
//        d2.setId("SW396938");
//        d2.setText(sb2);
//        d2.setTfMax(5);
//        d2.setTermDic(tDic2);
//
//        StringBuilder sb3 = new StringBuilder();
//        HashMap<String, int[]> tDic3 = new HashMap<>();
//
//        tDic3.put("Tomer" , test);
//        tDic3.put("Abba", test);
//        tDic3.put("Tomer", test);
//
//        d3.setId("TA834655");
//        d3.setText(sb3);
//        d3.setTfMax(2);
//        d3.setTermDic(tDic3);
//
//        docs.add(d1);
//        docs.add(d2);
//        docs.add(d3);
//
//

        //   termDic.put("Abba", test);
        //   termDic.put("Ima", test);
        //  termDic.put("tomer", test);
        // termDic.put("Tomer", test);

        //allTermsDF.put("Ima", 3);
        // allTermsDF.put("Abba", 4);
        // allTermsDF.put("Tomer", 1);

        Queue<String> allDocs = new LinkedList<>();
        ((LinkedList<String>) allDocs).add(0, "d1");
        ((LinkedList<String>) allDocs).add(1, "d2");
        ((LinkedList<String>) allDocs).add(2, "d3");

        //   allTermsInDocs.put("Ima", allDocs);
        //   Queue<String> allDocs2 = new LinkedList<>();
        //   ((LinkedList<String>) allDocs2).add(0, "d1");
        //  ((LinkedList<String>) allDocs2).add(1, "d2");

        // ((LinkedList<String>) allDocs2).add(2, "d3");
        //  ((LinkedList<String>) allDocs2).add(3, "d4");
        //  allTermsInDocs.put("Abba", allDocs2);



        //indexAll(docs);
    }

    public void start (){ indexAll();}


    public static void restart(){
        Queue<Document> currChunk = new LinkedList<>();
    }


    public void run(){
        indexAll();
    }

    public void test33 (){ mergePosting();}
    public void test44 () {createDic();}


}
