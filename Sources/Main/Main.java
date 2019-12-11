package Main;


import Part_1.Indexer;
import Part_1.Parse;
import Part_1.ReadFile;
import Part_1.Document;
import java.util.HashMap;
import java.util.Map;


public class Main {

    static Indexer indexer = new Indexer();
    static Parse parse = new Parse(false, null);

    public static void main(String[] args) {

        ReadFile rf = new ReadFile("C:\\Users\\Tali\\BGU\\searchEngine");

        //indexer.start();
       // indexer.add();
       // indexer.search();
        //indexer.test();
        parse.start();
        indexer.start();

        rf.readInsideAllFiles();
        Parse parse_test = new Parse(false,null);
        parse_test.start();

        for(Map.Entry<String, int[]> entry : parse_test.termDic.entrySet()) {
            String key = entry.getKey();
            int[] value = entry.getValue();
            System.out.println(key+","+value[0]);

        }


    }





}


/**
 * String regaxExp_1 = "(?!,[0-9])[, ?@!:;+)_(\"\\r\\n]+";
 *         String regaxExp_2 = "(?!/[0-9])(?!,[0-9])[?!:,#`@^~&+{*}|\"<=>\\s;()_\\\\\\[\\]]+";
 *         String path = "C:\\Users\\omer\\Desktop\\corpus";
 *         File textFile = new File(path);
 *         if (textFile != null){
 *             try {
 *                 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(textFile)));
 *                 StringBuilder allLinesInDoc = new StringBuilder();
 *                 for (String currLine; (currLine = bufferedReader.readLine()) != null; )
 *                     allLinesInDoc.append(currLine + System.lineSeparator());
 *                 bufferedReader.close();
 *                 String[] tokensPrint_1 = allLinesInDoc.toString().split(regaxExp_1);
 *                 String[] tokensPrint_2 = allLinesInDoc.toString().split(regaxExp_2);
 *                 boolean print = true;
 *                 int index_1 = 0;
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
 *                 else if (currToken.contains("-")) { // word-word // word-word-word // num-word // word-num //
 *                     // BETWEEN NUMBERSSSS ????????????????????????????????????????????????????????????????????????/
 *                     //????????????????
 *                     rightToken = currToken;
 *                 }
 *                 // DDAATTEEESSSSSS
 *                 else if (isNumeric(allTokens[index]) && isDate(allTokens[index + 1]) && allTokens[index].length() == 2) //14 May
 *                     rightToken = turnMonthToNumber(allTokens[index + 1]) + "-" + allTokens[index]; //05-14
 *                 else if (isDate(allTokens[index]) && isNumeric(allTokens[index + 1]) && allTokens[index + 1].length() == 2) //JUN 4
 *                     rightToken = turnMonthToNumber(allTokens[index]) + "-" + allTokens[index + 1]; //06-04
 *                 else if (isDate(allTokens[index]) && isNumeric(allTokens[index + 1]) && allTokens[index + 1].length() == 4) //JUN 1994
 *                     rightToken = allTokens[index + 1] + "-" + turnMonthToNumber(allTokens[index]); //1994-06
 */
