package Main;


import Part_1.Document;
import Part_1.Parse;
import Part_1.ReadFile;

import javafx.scene.control.Alert;

import java.io.*;
import java.util.LinkedList;
import java.util.Queue;

public class Main {

    private static String pathDir;
    private static StringBuilder allLinesInDoc;
    private static Queue<Document> documentsSet = new LinkedList<>();


    public static void main(String[] args) {
        String regaxExp = "(?!,[0-9])[, ?@!:;+)_(\"\\r\\n]+";
        //String path = "C:\\Users\\Tali\\Desktop\\BGU\\searchEngine\\corpus\\FB396001\\FB396001";
        String path = "C:\\Users\\Tali\\Desktop\\BGU\\searchEngine\\new1.txt";

        Parse.parseDocs(); // ?????????????????????????


        File textFile = new File(path);
        if (textFile != null) {
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(textFile)));
                StringBuilder allLinesInDoc = new StringBuilder();
                for (String currLine; (currLine = bufferedReader.readLine()) != null; )
                    allLinesInDoc.append(currLine + System.lineSeparator());
                bufferedReader.close();
                String[] tokensPrint = allLinesInDoc.toString().split(regaxExp);
       //         for (String token : tokensPrint)
        //            System.out.println(token);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error in folder path");
            alert.show();
        }

    }
}