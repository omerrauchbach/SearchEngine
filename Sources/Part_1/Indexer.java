package Part_1;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class Indexer {


    static HashMap<String, int[]> termDic = new HashMap<>(); // 0 -df, 1- tf, list of docs ???
    private int currNum = -1;
    private int termNumOfDocs = -1;
    private String currDocName = "";
    private Document currDoc;
    private String currTerm;
    private HashMap<String, int[]> currDocDic = new HashMap<>();
    private static HashMap<String, Integer> allTermsDF = new HashMap<>(); //df !
    private HashMap<String, Queue<String>> allTermsInDocs = new HashMap<>(); //which docs !


    private void indexAll(Queue<Document> q_docs) {

        Queue<String> oldAllDocs = null;
        Queue<String> newAllDocs = null;

        while (q_docs != null && !q_docs.isEmpty()) { //indexes all docs in the queue (CHUNK)
            currDoc = q_docs.poll();
            currDocDic = currDoc.getAllTerms();
            currDocName = currDoc.getId();

            for (String key : currDocDic.keySet()) {
                //int[] values = currDocDic.get(key); // not neccesary.
                if (termDic.containsKey(key)) { //this term already exists
                    termNumOfDocs = allTermsDF.get(key); //old value of # docs for term
                    allTermsDF.replace(key, termNumOfDocs, termNumOfDocs + 1); //adds one more doc to count.
                    oldAllDocs = allTermsInDocs.get(key);
                    newAllDocs = oldAllDocs;
                    newAllDocs.add(currDocName); //adds curr doc to list of docs for this term
                    allTermsInDocs.replace(key, oldAllDocs, newAllDocs); //updates the map
                } else {
                    allTermsDF.put(key, 1); //first doc...
                    Queue<String> firstDocToList = new LinkedList<>() ;
                    firstDocToList.add(currDocName);
                    allTermsInDocs.put(key, firstDocToList);
                }
                //System.out.println("Key = " + key + ", Value = " + values);
            }
        }
    }




    private void addTerm (String term) {

        boolean exists = false;
        try{
            Scanner textScan = new Scanner(new File("/out/dictionary.txt"));

            while (textScan.hasNextLine() && !exists) {
                String str = textScan.nextLine();
                if (str.indexOf(term) != -1) { //word already exists in dic
                    updatePost(term); //////////// ??????????????????? or here ? dont need another functiion ?
                    exists = true;
                }
            }
            if (!exists) //new term needs to be added
                addNewTerm(term);

        } catch (IOException ex) {
            // Report
        }

    }

    private void createFile (String terms) {
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("dictionary.txt"), "utf-8"));
            writer.write(terms);
            ((BufferedWriter) writer).newLine();

        } catch (IOException ex) {
            // Report
        } finally {
            try {writer.close();} catch (Exception ex) {/*ignore*/}
        }
    }


    private void addNewTerm (String terms){
        try {
            Files.write(Paths.get("dictionary.txt"), terms.getBytes(), StandardOpenOption.APPEND);
        }catch (IOException e) {
            //exception handling left as an exercise for the reader
        }

    }

    private void updatePost (String terms){


    }

    public void start(){
        createFile( "my new file :)");
    }

    public void add (){
        addNewTerm("Abba, tf = 2, idf = 3.");
    }

    public void search (){
        addTerm("Abba");
    }

    public void test (){
        Queue<Document> docs = new LinkedList<Document>();
        HashMap<String, int[]> tDic = new HashMap<>();

        Document d1 = new Document();
        Document d2 = new Document();
        Document d3 = new Document();

        StringBuilder sb = new StringBuilder();
        tDic.put("Abba", null);
        tDic.put("Ima", null);

        d1.setId("FB396005");
        d1.setText(sb);
        d1.setTfMax(9);
        d1.setTermDic(tDic);

        StringBuilder sb2 = new StringBuilder();
        tDic.clear();
        tDic.put("Ima" , null);
        tDic.put("Abba", null);
        tDic.put("Tomer", null);

        d2.setId("SW396938");
        d2.setText(sb2);
        d2.setTfMax(5);
        d2.setTermDic(tDic);

        StringBuilder sb3 = new StringBuilder();
        tDic.clear();
        tDic.put("Tomer" , null);
        tDic.put("Tomer", null);
        tDic.put("Tomer", null);
        tDic.put("Abba", null);
        tDic.put("Tomer", null);

        d3.setId("TA834655");
        d3.setText(sb3);
        d3.setTfMax(2);
        d3.setTermDic(tDic);

        docs.add(d1);
        docs.add(d2);
        docs.add(d3);

        indexAll(docs);
    }
}
