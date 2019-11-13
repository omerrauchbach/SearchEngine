package Part_1;

import javafx.scene.control.Alert;

import java.io.*;
import java.util.*;

public class Parse {

    public static Queue<Document> documentsSet = new LinkedList<>();
    private HashMap<String, Integer> stopWords;
    private HashMap<String , int[]> termDic = new HashMap<>();


    public void parseDocs() {

        while (!documentsSet.isEmpty()) {
            Document newDoc = documentsSet.poll();
            String text = newDoc.getText().toString();
            String[] allTokens = text.split("(?!,[0-9])[, ?@!:;+)_(\"\\r\\n]+");

            for (int i = 0; i < allTokens.length; i++) {
                String rightToken;
                String currToken = allTokens[i];

                if (String.valueOf(currToken.charAt(0)).equals("$") && isNumeric(String.valueOf(currToken.charAt(currToken.length() - 1))) && allTokens[i + 1].toLowerCase().equals("million"))  // $450 million
                    rightToken = allTokens[i].substring(1) + " M Dollars"; ///removes the $ from the beginning
                else if ((String.valueOf(currToken.charAt(0)).equals("$") && isNumeric(String.valueOf(currToken.charAt(currToken.length() - 1))) && allTokens[i + 1].toLowerCase().equals("billion")))  // $450 billion
                    rightToken = allTokens[i].substring(1) + "000 M Dollars";
                else if (isNumeric(currToken) && allTokens[i + 1].equals("million") && allTokens[i + 2].toUpperCase().equals("U.S.") && allTokens[i + 3].toLowerCase().equals("dollars"))  // 320 million U.S. Dollars
                    rightToken = currToken + " M Dollars";
                else if (isNumeric(currToken) && allTokens[i + 1].equals("billion") && allTokens[i + 2].toUpperCase().equals("U.S.") && allTokens[i + 3].toLowerCase().equals("dollars"))  // 100 billion U.S. Dollars
                    rightToken = currToken + "000 M Dollars";

                else if (String.valueOf(currToken.charAt(0)).equals("$") && isNumeric(String.valueOf(currToken.charAt(1))) && !isNumeric(allTokens[i + 1]) && lessThanMillion(currToken.substring(1))) // $450 blabla // $450,000 blabla
                    rightToken = allTokens[i].substring(1) + "Dollars"; //removes the $ from the beginning


                else if (String.valueOf(currToken.charAt(0)).equals("$") && isNumeric(String.valueOf(currToken.charAt(1))) && !isNumeric(allTokens[i + 1]) && !lessThanMillion(currToken.substring(1)))  // $150,000,000 ==> 150 M Dollars
                    rightToken = allTokens[i].substring(1, currToken.indexOf(",")) + " M " + "Dollars"; //until first "," !
                else if (isNumeric(currToken) && !lessThanMillion(currToken) && allTokens[i + 1].toLowerCase().equals("dollars")) // 1,000,000 Dollars
                    rightToken = allTokens[i].substring(0, currToken.indexOf(",")) + " M " + "Dollars";


                else if (currToken.contains("-")) { // word-word // word-word-word // num-word // word-num //
                    // BETWEEN NUMBERSSSS ????????????????????????????????????????????????????????????????????????/
                    //????????????????
                    rightToken = currToken;
                }
                // DDAATTEEESSSSSS
                else if (isNumeric(allTokens[i]) && isDate(allTokens[i + 1]) && allTokens[i].length() == 2) //14 May
                    rightToken = turnMonthToNumber(allTokens[i + 1]) + "-" + allTokens[i]; //05-14
                else if (isDate(allTokens[i]) && isNumeric(allTokens[i + 1]) && allTokens[i + 1].length() == 2) //JUN 4
                    rightToken = turnMonthToNumber(allTokens[i]) + "-" + allTokens[i + 1]; //06-04
                else if (isDate(allTokens[i]) && isNumeric(allTokens[i + 1]) && allTokens[i + 1].length() == 4) //JUN 1994
                    rightToken = allTokens[i + 1] + "-" + turnMonthToNumber(allTokens[i]); //1994-06

                else if (isNumeric(currToken)) {
                    if (isThousand(currToken) && !isNumeric(allTokens[i + 1])) //only 100,123 (K)
                        rightToken = allTokens[i].substring(0, currToken.indexOf(",")) + "." + allTokens[i].substring(currToken.indexOf(",") + 1) + "K";
                    else if (isMillion(currToken) && !isNumeric(allTokens[i + 1])) // only 100,123,333 (M)
                        rightToken = allTokens[i].substring(0, currToken.indexOf(",")) + "." + allTokens[i].substring(currToken.indexOf(",") + 1, currToken.indexOf(",") + 4) + "M";
                    else if (isBillion(currToken) && !isNumeric(allTokens[i + 1])) // only 100,123,333,000 (B)
                        rightToken = allTokens[i].substring(0, currToken.indexOf(",")) + "." + allTokens[i].substring(currToken.indexOf(",") + 1, currToken.indexOf(",") + 4) + "B";

                    else if (allTokens[i + 1].toLowerCase().equals("thousand"))
                        rightToken = currToken + "K";
                    else if (allTokens[i + 1].toLowerCase().equals("million"))
                        rightToken = currToken + "M";
                    else if (allTokens[i + 1].toLowerCase().equals("billion"))
                        rightToken = currToken + "B";

                    else if (allTokens[i + 1].equals("percent") || allTokens[i + 1].equals("percentage"))
                        rightToken = allTokens[i] + "%";

                    else if (isFraction(allTokens[i + 1]) && allTokens[i + 2].toLowerCase().equals("dollars")) // 22 3/4 Dollars
                        rightToken = allTokens[i] + allTokens[i + 1] + "Dollars";
                    else if (isFraction(allTokens[i + 1])) // FRACTIONNNNN ???
                        rightToken = allTokens[i] + allTokens[i + 1]; //as is. 25 3/4
                    else if (allTokens[i + 1].toLowerCase().equals("dollars"))
                        rightToken = allTokens[i] + "Dollars";
                }
                // FRACTION ESRONIIII
                else if (allTokens[i].contains(".") && allTokens[i].substring(0, allTokens[i].indexOf(".")).length() > 3 && allTokens[i].substring(0, allTokens[i].indexOf(".")).length() < 7) { // 1023.48 // less/equal than 3 stays as is. 102.2 ,,, 10.873. GREATER THAN 6 IS ??????
                    //String tempToken = allTokens[i].substring(0, allTokens[i+1].indexOf(".")); //only 1023
                    //if (tempToken.length() > 6) //1023.4999 ==> 1.023
                    int tempNum = (Integer.parseInt(allTokens[i]) / 100); // SHOULD BE 1.02348
                    String temp = Integer.toString(tempNum);
                    rightToken = temp.substring(temp.indexOf(".") + 1, temp.indexOf(".") + 4) + "K"; // 1.023K // only 3 DIGITS !
                } else if (isNumeric(String.valueOf(currToken.charAt(0))) && String.valueOf(currToken.charAt(currToken.length() - 1)).equals("%")) //632%
                    rightToken = currToken; //already includes the "%" sign

                else if (isNumeric(String.valueOf(currToken.charAt(0))) && String.valueOf(currToken.charAt(currToken.length() - 1)).equals("m") && allTokens[i + 2].toLowerCase().equals("dollars")) // 20.6m Dollars
                    rightToken = currToken.substring(0, currToken.length() - 1) + " M Dollars"; //without the m itself in the token.
                else if (isNumeric(String.valueOf(currToken.charAt(0))) && String.valueOf(currToken.charAt(currToken.length() - 2)).equals("b") && String.valueOf(currToken.charAt(currToken.length() - 1)).equals("n") && allTokens[i + 1].toLowerCase().equals("dollars")) // 100bn Dollars
                    rightToken = currToken.substring(0, currToken.length() - 2) + "000 M Dollars";

                else {
                    rightToken = allTokens[i];
                }
            }
        }
    }

    private boolean isNumeric (String docToken){ //checks if the token is a number
        try {
            Double.parseDouble(docToken);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    private boolean isFraction (String docToken){ //checks if the token is a number with a fraction
       if (docToken.contains("/"))
           return true;
       return false;
    }

    private boolean lessThanMillion (String numToken){
        if (Integer.parseInt(numToken) < 1000000)
            return true;
        return false;
    }

    private boolean isThousand (String numToken){
        if (Integer.parseInt(numToken) >= 1000 && (Integer.parseInt(numToken) < 1000000))
            return true;
        return false;
    }

    private boolean isMillion (String numToken){
        if (Integer.parseInt(numToken) >= 1000000 && (Integer.parseInt(numToken) < 1000000000))
            return true;
        return false;
    }

    private boolean isBillion (String numToken){
        if (Integer.parseInt(numToken) >= 1000000000 )
            return true;
        return false;
    }

    private boolean isDate (String docToken){ //checks if the token is a month (date)
        String[] months = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};
        boolean found = false;
        for (String month : months) {
            if (docToken.toLowerCase().contains(month)) {
                found = true;
                break;
            }
        }
        return found;
    }

    public static String turnMonthToNumber (String docMonth){ //turns the month name to number

        if (docMonth.toLowerCase().equals("jan") || docMonth.toLowerCase().equals("january"))
            return "01";
        else if (docMonth.toLowerCase().equals("feb") || docMonth.toLowerCase().equals("february"))
            return "02";
        else if (docMonth.toLowerCase().equals("mar") || docMonth.toLowerCase().equals("march"))
            return "03";
        else if (docMonth.toLowerCase().equals("apr") || docMonth.toLowerCase().equals("april"))
            return "04";
        else if (docMonth.toLowerCase().equals("may"))
            return "05";
        else if (docMonth.toLowerCase().equals("jun") || docMonth.toLowerCase().equals("june"))
            return "06";
        else if (docMonth.toLowerCase().equals("jul") || docMonth.toLowerCase().equals("july"))
            return "07";
        else if (docMonth.toLowerCase().equals("aug") || docMonth.toLowerCase().equals("august"))
            return "08";
        else if (docMonth.toLowerCase().equals("sep") || docMonth.toLowerCase().equals("september"))
            return "09";
        else if (docMonth.toLowerCase().equals("oct") || docMonth.toLowerCase().equals("october"))
            return "10";
        else if (docMonth.toLowerCase().equals("nov") || docMonth.toLowerCase().equals("november"))
            return "11";
        else if (docMonth.toLowerCase().equals("dec") || docMonth.toLowerCase().equals("december"))
            return "12";
        return "-1";
    }

    private void setStopWords(String path){

        File stopWordsFile = new File(path);
                if (stopWordsFile != null) {
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(stopWordsFile)));
                        StringBuilder stopWordLines = new StringBuilder();
                        for ( String currLine ; (currLine = bufferedReader.readLine()) != null; )
                            stopWords.put( currLine, 1 );
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }


        else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error in folder path");
            alert.show();
        }
    }

    private void handleWords(String word){

        if(word != null && word.length() > 1) {
            /// check if word is in lower letters
            if (word.equals(word.toLowerCase())) {
                /// word is save in the Dic
                if(termDic.containsKey(word)){

                }
                else if(termDic.containsKey(word.toUpperCase())){
                    //check if the word is save in upper case


                }
                else{
                    // first occur
                }

            }
            /// check if the first char in the word is upper letter
            else if (word.charAt(0) == word.toUpperCase().charAt(0)){
                if(termDic.containsKey(word)){

                }
                else if(termDic.containsKey(word.toUpperCase())){
                    //check if the ward is save in upper case


                }
                else{
                    // first occur
                }
            }
            ///check if the word is all in upper letter.
            else if (word.equals(word.toUpperCase())){
                //first occur
                if(!termDic.containsKey(word)){

                }
                else if(termDic.containsKey(word.toLowerCase())){

                }
                else{
                    //first occur
                }
            }
        }
    }
}



