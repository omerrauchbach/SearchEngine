package Part_1;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.concurrent.LinkedBlockingQueue;


public class Indexer extends Thread {

    public static HashMap<String, int[]> termDic = new HashMap<>(); // 0 - #docs, 1- #showsTotal, 2- line in posting
    public static HashMap<String, int[]> allDocuments = new HashMap<>(); // 0 - maxTF, 1- #uniqueTerms, 2- length of doc

    private HashMap<String, int[]> currDocDic = new HashMap<>();
    private String FILE_PATH = "";
    public static String filePath = ""; //get it from parse. !



    private TreeMap<String, int[]> littleDic;
    private int currPostingFileIndex = 1;
    private int counterPostingFiles = 1;
    String allPostingPath = "";
    private Path path = null;
    private String userFilePath;
    HashMap<String , String> changeUpperCase;

    public Indexer(boolean stemming ,String postingPath){
        userFilePath = postingPath;

        if(stemming)
            FILE_PATH = postingPath+"\\stemming.txt";
        else
            FILE_PATH = postingPath+"\\nonStemming.txt";


        if(Files.exists(Paths.get(FILE_PATH))) {
            try {
                PrintWriter pw = new PrintWriter(FILE_PATH);
                pw.close();
            }
            catch (FileNotFoundException e){
                e.printStackTrace();
                createFile();
            }

        }else{
            createFile();
        }
    }

    private void createFile(){

        try {
            File file = new File(FILE_PATH);
            if (file.createNewFile()) {
                System.out.println("File is created!");
            } else {
                System.out.println("File already exists.");
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void indexAll() {

        while(!Parse.stopIndexer || (Parse.stopIndexer && !Parse.currChunk.isEmpty())){

            if(!Parse.currChunk.isEmpty() && (Parse.currChunk.size()>= 2000|| Parse.stopIndexer)) {

                Queue<Document> queueOfDoc = new LinkedList<>();
                HashMap<String, int[]> tmpDicDoc = new HashMap<>();
                filePath = userFilePath + "\\posting" + currPostingFileIndex + ".txt";
                synchronized (Parse.currChunk){
                    Parse.currChunk.drainTo(queueOfDoc, 3000);
                    Parse.currChunk.notify();
                }



                try {
                    File file = new File(filePath);
                    file.createNewFile();
                }
                catch (IOException e){
                    e.printStackTrace();
                }
                allPostingPath = filePath + "\\out";
                path = Paths.get(filePath);
                String allInfoOfTermForPosting = "";
                littleDic = newTree();
                HashMap<String, String> ChunkTermDicDocs = new HashMap<>();
                changeUpperCase = new HashMap<>();

                while (!queueOfDoc.isEmpty()) {

                    Document currDoc = queueOfDoc.poll();
                    System.out.println(currDoc.getId() +":Indexer");
                    int[] docInfo = new int[3];

                    /////hundle tmpDicDoc /////////////
                    docInfo[0] = currDoc.getTfMax();
                    docInfo[1] = currDoc.uniqueTerm(); //how many unique terms.
                    docInfo[2] = currDoc.getLength();
                    tmpDicDoc.put(currDoc.getId(), docInfo); //adds current doc to docs dic.
                    allDocuments.put(currDoc.getId(), docInfo); //adds current doc to docs dic.

                    for (Map.Entry<String, int[]> entry : currDoc.termDic.entrySet()) {


                        int[] currTermInfo = entry.getValue();
                        String key = entry.getKey();
                        boolean notNum = !key.matches(".*\\d.*");

                        if(notNum&&!key.chars().anyMatch(Character::isUpperCase) && littleDic.containsKey(key.toUpperCase())){
                                littleDic.put(key,littleDic.remove(key.toUpperCase()));
                        }

                        if (littleDic.containsKey(key)) {

                            int[] savedTermData = littleDic.get(key);
                            int[] updateTermInfo = new int[2];
                            updateTermInfo[0] = savedTermData[0] + 1; // adds 1 to curr # of docs
                            updateTermInfo[1] = savedTermData[1] + currTermInfo[0]; //#shows total == adds num of appearences in specific doc. !!!
                            littleDic.replace(key, updateTermInfo); //replaces values in little dic

                            allInfoOfTermForPosting = ChunkTermDicDocs.get(key) + "|" + currDoc.getId() + ":" + currTermInfo[0] + ";" + currDoc.getPlaces(key);
                            ChunkTermDicDocs.replace(key, allInfoOfTermForPosting); // updates info for posting.

                        } else { //first time of this term in chunk.

                            int[] termInfo = new int[2];
                            termInfo[0] = 1; // first doc in list
                            termInfo[1] = currTermInfo[0]; //num of appearances in specific doc. !!!
                            littleDic.put(key, termInfo); //first doc in list, for posting!
                            allInfoOfTermForPosting = currDoc.getId() + ":" + termInfo[1] + ";" + currDoc.getPlaces(key);
                            ChunkTermDicDocs.put(key, allInfoOfTermForPosting); //info for posting.
                        }
                    }

                    if (queueOfDoc.isEmpty()) {
                        updateTermDic();
                        createPostingFile(filePath , ChunkTermDicDocs);
                        currPostingFileIndex++;
                        if(currPostingFileIndex > 2)
                            mergePosting();

                    }
                }
            }else{
                try {
                    sleep(10);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        } //nothing left to index. // one merged posting file::
    }

    private void updateTermDic(){

        for(Map.Entry<String, int[]> entry : littleDic.entrySet()){

            String key = entry.getKey();
            int[] value = entry.getValue();
            if(termDic.containsKey(key)){
                int[] savedTermData = termDic.get(key);
                int[] updateTermInfo = new int[3];
                updateTermInfo[0] = savedTermData[0] +value[0]; // adds 1 to curr # of docs
                updateTermInfo[1] = savedTermData[1] + value[1]; //#shows total == adds num of appearences in specific doc. !!!
                termDic.replace(key,updateTermInfo);

            }else{
                termDic.put(key ,value);
            }
        }
    }

    private void createPostingFile(String path , HashMap<String,String> ChunkTermDicDocs){

        try {
            File newPostFile = new File(path);
            PrintWriter out = new PrintWriter(newPostFile);
            for(String key : littleDic.keySet())
                out.append(key + "|" + ChunkTermDicDocs.get(key) + "\n");

            out.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private void mergePosting () {


        String line1 = "";
        String line2 = "";
        String term1 ="";
        String term2 = "";
        try {
            int oldPostingIndex = currPostingFileIndex-1;
            int olderPostingIndex = currPostingFileIndex-2;

            Scanner sc1 = new Scanner((new File(userFilePath+"\\posting" + oldPostingIndex + ".txt")));
            Scanner sc2 = new Scanner((new File(userFilePath+ "\\posting" + olderPostingIndex + ".txt")));
            File newPostFile;
            if (Parse.stopIndexer && !Parse.currChunk.isEmpty())
                newPostFile = new File(FILE_PATH);
            else //still has more things to do.
                newPostFile = new File(userFilePath+"\\posting" + currPostingFileIndex + ".txt");
            PrintWriter out = new PrintWriter(new FileWriter(newPostFile, true));
            line1 = sc1.nextLine();
            line2 = sc2.nextLine();
            term1 =line1.substring(0, line1.indexOf("|")); // only term itself, with no other data.
            term2 = line2.substring(0, line2.indexOf("|")); // only term itself, with no other data.

            while (sc1.hasNextLine() && sc2.hasNextLine()) {
                int compare = compareTwoString(term1,term2);
                if (compare < 0) { //term1 is first in dic.
                    out.append(line1+ "\n");
                    line1 = sc1.nextLine();
                    term1 =line1.substring(0, line1.indexOf("|")); // only term itself, with no other data.

                } else if (compare > 0) {
                    out.append(line2+"\n");
                    line2 = sc2.nextLine();
                    term2 = line2.substring(0, line2.indexOf("|")); // only term itself, with no other data.

                }
                else { //same term ! // adds both list of docs and data. compare == 0

                    int compareNotUpperCase = term1.compareTo(term2);
                    if(term1.matches(".*\\d.*") || compareNotUpperCase == 0 || compareNotUpperCase < 0) { ///same word && term1 lowerCase and term2 upperCase
                        out.append(term1 + line1.substring(line1.indexOf("|")) + line2.substring(line2.indexOf("|")) + "\n");
                        if(compareNotUpperCase < 0){
                            termDic.put(term1 ,termDic.remove(term2));
                        }
                    }else{ //term2 lowerCase and term1 upperCase
                        out.append(term2 + line1.substring(line1.indexOf("|")) + line2.substring(line2.indexOf("|")) + "\n");
                    }

                    line1 = sc1.nextLine();
                    line2 = sc2.nextLine();
                    term1 =  line1.substring(0, line1.indexOf("|")); // only term itself, with no other data.
                    term2 = line2.substring(0, line2.indexOf("|")); // only term itself, with no other data.

                }
            }
            while (sc1.hasNextLine()) { //adds only terms from 1.
                line1 = sc1.nextLine();
                out.append(line1 + "\n");
            }
            while (sc2.hasNextLine()) { //adds only terms from 2.
                line2 = sc2.nextLine();
                out.append(line2 + "\n");
            }
            sc1.close();
            sc2.close();
            out.close();

        } catch (IOException ex) {
            // Report
        } catch (StringIndexOutOfBoundsException e){

        }

        currPostingFileIndex++;
        deleteTwoTempPosting(currPostingFileIndex-2 ,currPostingFileIndex-3);

    }

    private String changeToUpperCase(String key){

        if(changeUpperCase.containsKey(key))
            return changeUpperCase.get(key);
        else
            return key;
    }

    private void deleteTwoTempPosting (int firstFile ,int secondFile){ //which file not to delete!!

        String firsPath =  "posting" + firstFile+ ".txt" ;
        String secondPath = "posting" + secondFile+ ".txt" ;

        File mainDir = new File(userFilePath);
        File[] files = mainDir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file != null && (file.getName().equals(firsPath) || file.getName().equals(secondPath)) ) { //deletes 2 files but not the curr merged one.!
                    file.delete();
                }
            }

        }
    }

    private int compareTwoString(String s1 , String s2){

        int result = s1.compareToIgnoreCase(s2);
        if( result == 0 )
            result = s1.compareTo(s2);
        return result;
    }

    private TreeMap<String , int[]> newTree(){

        return new TreeMap<>(new Comparator<String>(){

            @Override
            public int compare(String s1, String s2) {
                int result = s1.compareToIgnoreCase(s2);
                if( result == 0 )
                    result = s1.compareTo(s2);
                return result;
            }
        });
    }

    public void start (){ indexAll();}


    public static void reset(){
        Parse.currChunk = new LinkedBlockingQueue<>();
        termDic = new HashMap<>();
        allDocuments = new HashMap<>();
    }


    public void run(){
        indexAll();
    }
}
