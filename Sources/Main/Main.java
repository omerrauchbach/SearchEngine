package Main;


import Part_1.Parse;
import Part_1.ReadFile;
import Part_1.Document;
import java.util.HashMap;
import java.util.Map;


public class Main {


    public static void main(String[] args) {

        try {

            Integer.parseInt("10.6");
        }
        catch (NumberFormatException e){
            System.out.println("Error number");
        }
    }


    private void test_1(){
        ReadFile rf = new ReadFile("C:\\Users\\omer\\Desktop");
        HashMap<String, Integer> termCounter = new HashMap<>();
        rf.readInsideAllFiles();
        for (Document d : Parse.documentsSet) {
            String text = d.getText().toString();
            String[] allTokens = text.split( "(?!,[0-9])[, ?@!:;+)_(\"\\r\\n]+");
            for(String term : allTokens )
                if(termCounter.containsKey(term)){
                    termCounter.replace(term, termCounter.get(term)+1);
                }
                else{
                    termCounter.put(term ,1);
                }
        }

        for(Map.Entry<String, Integer> entry : termCounter.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            System.out.println(key+","+value);

        }
    }

    private void test_2(){

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
 *  if (isPrice(currToken)) {
 *                     String price = "";
 *                     boolean sign =(currToken.charAt(0) == '$');
 *                     if(index < allTokens.length){
 *                         String sum = equalToSum(allTokens[index+1]);
 *                         if(!sum.equals("")){
 *                             if(sum.equals("Dollars")) {
 *                                 //Price Dollars
 *                                 price = numericToPrice(currToken, "", sign, true, false);
 *                             }else if(sum.equals("million") || sum.equals("million")){
 *
 *                             }else if(index+1<allTokens.length ){
 *                                 if((sum.equals("m") || sum.equals("bn")) && allTokens[index+2].equals("Dollars")){
 *                                     // Price m Dollars Or Price bn Dollars
 *                                     price = numericToPrice(currToken ,sum ,sign, true , false);
 *                                 }
 *                                 else if(allTokens[index+2].equals("U.S."))
 *                                     if(index+2 < allTokens.length &&allTokens[index+2].equals("Dollars") )
 *                                         price = numericToPrice(currToken ,sum,sign, true , true);
 *
 *                             }else{
 *                                 price = numericToPrice(currToken ,sum,sign,false,false); //$price
 *                             }
 *
 *                         }else{
 *                             price = numericToPrice(currToken ,"",sign, false , false); //$price
 *                         }
 *
 *                     }else{
 *                         price = numericToPrice(currToken ,"",sign, false , false); //$price
 *                     }
 *
 *                 }
 *
 */

/**
 *
 * if (String.valueOf(currToken.charAt(0)).equals("$") && isNumeric(String.valueOf(currToken.charAt(currToken.length() - 1))) && allTokens[index + 1].toLowerCase().equals("million"))  // $450 million
 *                     rightToken = allTokens[index].substring(1) + " M Dollars"; ///removes the $ from the beginning
 *                 else if ((String.valueOf(currToken.charAt(0)).equals("$") && isNumeric(String.valueOf(currToken.charAt(currToken.length() - 1))) && allTokens[index + 1].toLowerCase().equals("billion")))  // $450 billion
 *                     rightToken = allTokens[index].substring(1) + "000 M Dollars";
 *                 else if (isNumeric(currToken) && allTokens[index + 1].equals("million") && allTokens[index + 2].toUpperCase().equals("U.S.") && allTokens[index + 3].toLowerCase().equals("dollars"))  // 320 million U.S. Dollars
 *                     rightToken = currToken + " M Dollars";
 *                 else if (isNumeric(currToken) && allTokens[index + 1].equals("billion") && allTokens[index + 2].toUpperCase().equals("U.S.") && allTokens[index + 3].toLowerCase().equals("dollars"))  // 100 billion U.S. Dollars
 *                     rightToken = currToken + "000 M Dollars";
 *
 *                 else if (String.valueOf(currToken.charAt(0)).equals("$") && isNumeric(String.valueOf(currToken.charAt(1))) && !isNumeric(allTokens[index + 1]) && lessThanMillion(currToken.substring(1))) // $450 blabla // $450,000 blabla
 *                     rightToken = allTokens[index].substring(1) + "Dollars"; //removes the $ from the beginning
 *
 *
 *                 else if (String.valueOf(currToken.charAt(0)).equals("$") && isNumeric(String.valueOf(currToken.charAt(1))) && !isNumeric(allTokens[index + 1]) && !lessThanMillion(currToken.substring(1)))  // $150,000,000 ==> 150 M Dollars
 *                     rightToken = allTokens[index].substring(1, currToken.indexOf(",")) + " M " + "Dollars"; //until first "," !
 *                 else if (isNumeric(currToken) && !lessThanMillion(currToken) && allTokens[index + 1].toLowerCase().equals("dollars")) // 1,000,000 Dollars
 *                     rightToken = allTokens[index].substring(0, currToken.indexOf(",")) + " M " + "Dollars";
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
