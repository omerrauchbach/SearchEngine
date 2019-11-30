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

public class Indexer {

    // static HashMap<String, int[]> termDic = new HashMap<>(); // 0 -df, 1- tf, list of docs ???
    private String currDocName = "";
    private Document currDoc;
    private HashMap<String, int[]> currDocDic = new HashMap<>();
    private String FILE_PATH = "";

    private void indexAll(Queue<Document> q_docs) {

        FILE_PATH = "C:\\Users\\Tali\\IdeaProjects\\SearchEngine\\out\\dictionary.txt";
        Path path = Paths.get(FILE_PATH);

        while (q_docs != null && !q_docs.isEmpty()) { //indexes all docs in the queue (CHUNK)
            currDoc = q_docs.poll();
            currDocDic = currDoc.getAllTerms();
            currDocName = currDoc.getId();

            for (String key : currDocDic.keySet()) {

                boolean exists = false;
                try{
                    Scanner textScan = new Scanner(new File(FILE_PATH));

                    while (textScan.hasNextLine() && !exists) {
                        String str = textScan.nextLine();
                        if (str.indexOf(key) != -1) { //word already exists in dic (posting, txt file)

                            int currTF = currDocDic.get(key)[0]; //num of appearences in specific doc. !!!!!!!!!!!
                            String docToAdd = str + ", " + currDocName + "-" + currTF; //// d2-7, (times in doc)

                            List<String> fileContent = new ArrayList<>(Files.readAllLines(path, StandardCharsets.UTF_8));

                            for (int i = 0; i < fileContent.size(); i++) {
                                if (fileContent.get(i).equals(str)) {
                                    fileContent.set(i,docToAdd);
                                    break;
                                }
                            }
                            Files.write(path, fileContent, StandardCharsets.UTF_8);
                            exists = true;
                        }
                    }
                    if (!exists) //new term needs to be added
                        try {
                            int currTF = currDocDic.get(key)[0]; //num of appearences in specific doc. !!!!!!!!!!!
                            String docToAdd = key + ", 1, " + currDocName + "-" + currTF; ////Abba - 1, d2-7, (times in doc)
                            Files.write(path, docToAdd.getBytes(), StandardOpenOption.APPEND);
                        }catch (IOException e) {
                            //exception handling left as an exercise for the reader
                        }
                } catch (IOException ex) {
                    // Report
                }
            }
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

    public void test (){
        Queue<Document> docs = new LinkedList<Document>();
        HashMap<String, int[]> tDic = new HashMap<>();

        Document d1 = new Document();
        Document d2 = new Document();
        Document d3 = new Document();

        int[] test = new int[3];
        test[0] = 1;
        test[1] = 0;
        test[2] = 2;

        StringBuilder sb = new StringBuilder();
        tDic.put("Abba", test);

        int[] test2 = new int[3];
        test2[0] = 4;
        test2[1] = 0;
        test2[2] = 2;

        tDic.put("Ima",test2);

        d1.setId("FB396005");
        d1.setText(sb);
        d1.setTfMax(9);
        d1.setTermDic(tDic);

        StringBuilder sb2 = new StringBuilder();
        HashMap<String, int[]> tDic2 = new HashMap<>();

        tDic2.put("Ima" , test);
        tDic2.put("Abba", test);
        tDic2.put("Tomer", test);

        d2.setId("SW396938");
        d2.setText(sb2);
        d2.setTfMax(5);
        d2.setTermDic(tDic2);

        StringBuilder sb3 = new StringBuilder();
        HashMap<String, int[]> tDic3 = new HashMap<>();

        tDic3.put("Tomer" , test);
        tDic3.put("Abba", test);
        tDic3.put("Tomer", test);

        d3.setId("TA834655");
        d3.setText(sb3);
        d3.setTfMax(2);
        d3.setTermDic(tDic3);

        docs.add(d1);
        docs.add(d2);
        docs.add(d3);



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



        indexAll(docs);
    }
}
