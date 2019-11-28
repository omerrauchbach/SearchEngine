package Part_1;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Scanner;

public class Indexer {


    static HashMap<String , int[]> termDic = new HashMap<>(); // 0 -df, 1- tf, list of docs ???


    private void addTerm (String term) {

        boolean exists = false;
        try{
            Scanner textScan = new Scanner(new File("dictionary.txt"));

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
}
