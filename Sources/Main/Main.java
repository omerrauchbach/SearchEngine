package Main;


import Part_1.Indexer;
import Part_1.Parse;
import Part_1.ReadFile;
import Part_1.Document;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;


public class Main extends Application {



    public static void main(String[] args) {
      launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {


        Parent mainWindow = FXMLLoader.load(getClass().getResource("/Main.fxml"));
        primaryStage.setScene(new Scene(mainWindow , 600, 400));
        primaryStage.show();
    }
}


/*
public class Main{



    public static void main(String[] args) {
        ReadFile rf = new ReadFile("C:\\Users\\omer\\Desktop");
        rf.readInsideAllFiles();
        Parse parse_test = new Parse(false, null);
        parse_test.start();

    }


}
*/


/*
 Double test_d = 1234.567890;
        DecimalFormat df = new DecimalFormat("#.###");
        df.setRoundingMode(RoundingMode.CEILING);
        System.out.println(df.format(test_d));

String test_d = "1234567890";

        System.out.println(new BigDecimal(test_d).movePointRight(2));

                ReadFile rf = new ReadFile("C:\\Users\\omer\\Desktop");
                rf.readInsideAllFiles();
                Parse parse_test = new Parse(false, null);
                parse_test.start();


 */

/*
 String path = "C:\\Users\\omer\\Desktop\\corpus_1";
        File textFile = new File(path);
        if (textFile != null) {
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(textFile)));
                StringBuilder allLinesInDoc = new StringBuilder();
                for (String currLine; (currLine = bufferedReader.readLine()) != null; )
                    allLinesInDoc.append(currLine + System.lineSeparator());
                bufferedReader.close();
                String[] allTokens = allLinesInDoc.toString().split(" (?!,[0-9])[, ?@!:;#+)_(\"]+");
                for (String i : allTokens)
                    System.out.println(allTokens);
                boolean print = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


 */

/*
  ReadFile rf = new ReadFile("C:\\Users\\omer\\Desktop");
        rf.readInsideAllFiles();
        Parse parse_test = new Parse();
        parse_test.start();

        for(Map.Entry<String, int[]> entry : parse_test.termDic.entrySet()) {
            String key = entry.getKey();
            int[] value = entry.getValue();
            System.out.println(key+","+value[0]);

        }
 */


/*
 *         String regaxExp_1 = "(?!,[0-9])[, ?@!:;+)_(\"\\r\\n]+";
          String regaxExp_2 = "(?!/[0-9])(?!,[0-9])[?!:,#`@^~&+{*}|\"<=>\\s;()_\\\\\\[\\]]+";
         String path = "C:\\Users\\omer\\Desktop\\corpus";
          File textFile = new File(path);
          if (textFile != null){
              try {
                  BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(textFile)));
                  StringBuilder allLinesInDoc = new StringBuilder();
                 for (String currLine; (currLine = bufferedReader.readLine()) != null; )
                     allLinesInDoc.append(currLine + System.lineSeparator());
                  bufferedReader.close();
                  String[] tokensPrint_1 = allLinesInDoc.toString().split(regaxExp_1);
                  String[] tokensPrint_2 = allLinesInDoc.toString().split(regaxExp_2);
                  boolean print = true;
                  int index_1 = 0;
 *                 int index_2 = 0;
 *
 *                 while (print) {
 *                     if (index_1 < tokensPrint_1.length && index_2 < tokensPrint_2.length) {
 *                         System.out.println(tokensPrint_1[index_1] + ";;" + tokensPrint_2[index_2]);
 *                         index_1++;
 *                         index_2++;
 *                     } else if (index_1 < tokensPrint_1.length) {
 *                         System.out.println(tokensPrint_1[index_1]);
 *                         index_1++;
 *                     } else if (index_2 < tokensPrint_2.length) {
 *
 *                         System.out.println(tokensPrint_2[index_2]);
 *                         index_2++;
 *                     } else {
 *                         print = false;
 *                     }
 *                 }
 *             } catch (IOException e) {
 *                 e.printStackTrace();
 *             }
 *             }else{
 *                 Alert alert = new Alert(Alert.AlertType.ERROR);
 *                 alert.setContentText("Error in folder path");
 *                 alert.show();
 *             }
 */

/**
 *              ReadFile rf = new ReadFile("C:\\Users\\omer\\Desktop");
 *         rf.readInsideAllFiles();
 *         Parse parse_test = new Parse();
 *         parse_test.start();
 *
 *         for(Map.Entry<String, int[]> entry : parse_test.termDic.entrySet()) {
 *             String key = entry.getKey();
 *             int[] value = entry.getValue();
 *             System.out.println(key+","+value[0]);
 */
