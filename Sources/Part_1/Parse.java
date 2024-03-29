package Part_1;



import javafx.scene.control.Alert;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Parse extends Thread {


    public static BlockingQueue<Document> currChunk = new LinkedBlockingQueue<>(2000);
    private Set<String> stopWords = new HashSet<>();
    public  String[] allTokens ;
    private int index ;
    private String[] sums = {"Dollars","million","billion","trillion","m","bn"};
    private String[] months = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec","January",
            "February","March","April","May","June","July","August","September","October","November","December"};
    String docName = "";
    boolean iSstemmer ;
    Stemmer stemmer = new Stemmer();
    private Document newDoc;
    DecimalFormat df = new DecimalFormat("#.###");
    private String termLocationsInDoc = "";
    private String currTokenStartEnd;
    public static boolean stopIndexer = false;
    private ReadFile rd;


    public Parse(boolean stemmer , String stopWordPath , ReadFile rd){
        this.rd = rd ;
        this.iSstemmer = stemmer;
        df.setRoundingMode(RoundingMode.CEILING);
        setStopWord(stopWordPath);
    }

    private void parseDocs() {
        String currToken = "";

        while (!ReadFile.stopParser || (ReadFile.stopParser && !rd.documentsSet.isEmpty())) {
                rd.getChunckOfDoc();
                //Queue<Document> queueOfDoc = rd.documentsSet.
                //eadFile.documentsSet.drainTo(queueOfDoc,3000);
                while (!rd.documentsSet.isEmpty()) {
                    newDoc = rd.documentsSet.poll();
                    docName = newDoc.getId();
                    System.out.println(docName + ": Parse");
                    allTokens = newDoc.getText().split("(?!,[0-9])[\",\\/?&=@!\\[\\]:;|*#'+)_(\\s]+");
                    newDoc.clear();

                    index = 0;

                    try {
                        while (index < allTokens.length) {

                            currToken = allTokens[index].trim();
                            if(currToken.matches("[^\u0000-\u007F]+")){
                                index++;
                                continue;
                            }

                            currTokenStartEnd = startEndWord(currToken);
                            if (currToken.equals("") || currToken.length() == 1 || currToken.equals("\n") || stopWord(currTokenStartEnd)) {
                                if (!currToken.equals("May")) {
                                    index++;
                                    continue;
                                }
                            }

                            if (!currTokenStartEnd.matches(".*\\d.*") && !isMonths(currTokenStartEnd)) {
                                if (isLine(currToken)) {
                                    handleLine(currToken);

                                } else {
                                    handleWords(currTokenStartEnd);
                                }

                            } else if (isDate(currTokenStartEnd)) {
                                continue;
                            } else if (isPrice(currToken)) {
                                handlePrice(currToken);
                            } else if (isPercent(currToken)) {
                                handlePercent(currToken);
                            } else if (isNumericDouble(currToken)) {
                                handleNum(currToken);
                            } else if (isBetween(currToken)) {
                                insertTermDic(currToken + allTokens[index + 1] + allTokens[index + 2] + allTokens[index + 3]);
                                index = index + 4;
                            } else if (isLine(currToken)) {
                                handleLine(currToken);

                            } else {
                                handleWords(currToken);

                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // System.out.println (currToken);
                    }


                    try {

                        synchronized(currChunk){
                            if(currChunk.remainingCapacity() == 0)
                                while(!currChunk.isEmpty())
                                    currChunk.wait();
                        }

                        currChunk.put(newDoc);

                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    allTokens = null;
                }

        }

        stopIndexer = true;

    }

    private void insertFirstOccur(String term){
        if(term != null && !term.equals("") && term.length()>1) {
            int[] data = new int[4];
            data[0] = 1;
            //System.out.println(term+","+ data[0]+","+docName);
            newDoc.termDic.put(term, data);
            termLocationsInDoc = String.valueOf(index); //adds curr location of term in doc.
            newDoc.termPlacesInDoc.put(term, termLocationsInDoc);
        }
    }

    private void insertTermDic(String term){

        if(term != null && !term.equals("") && term.length()>1) {

            int[] newData = newDoc.termDic.get(term);
            if(newData == null)
                insertFirstOccur(term);
            else {
                newData[0]++;
                newDoc.termDic.replace(term, newData);
                termLocationsInDoc = newDoc.termPlacesInDoc.get(term) + "," + index; //adds curr location of term in doc.
                newDoc.termPlacesInDoc.replace(term, termLocationsInDoc);
                //System.out.println(term + "," + newData[0]+","+docName);
            }
        }
    }

    private void changeUpperCaseToLowerCase(String term){

        if(term != null && !term.equals("")) {
            int[] newData = newDoc.termDic.get(term.toUpperCase());
            newData[0]++;
            newDoc.termDic.remove(term.toUpperCase());
            newDoc.termDic.put(term , newData);
            //System.out.println(term + "," + newData[0]);
        }
    }

    private String numericToPrice(String num ,String sum,String fraction ,boolean sign, boolean Dollars , boolean US ){
        //String price = num.replace("," , "");
        String price = num;
        if(sign)
            price = price.substring(1);
        if(lessThanMillion(price) &&!sum.equals("trillion") && !sum.equals("million") && !sum.equals("billion") && !sum.equals("m") && !sum.equals("bn")){
            if(!fraction.equals(""))
                price = price+" "+fraction+" Dollars";
            else if(price.charAt(0) == '.')
                price = "0"+price+" Dollars";
            else
                price = price+" Dollars";
        }else{  ///greater then M
            price = price.replace("," , "");
            if (sum.equals("billion") || sum.equals("bn"))
                price = new BigDecimal(price).movePointRight(3).toString(); //adds 3 zeroes. B ==> M.
            else if (sum.equals("trillion"))
                price = new BigDecimal(price).movePointRight(6).toString(); //adds 6 ??? zeroes. T ==> M.

            price = price+" M Dollars";
        }

        return price;
    }

    private boolean equalToSum(String word){
        if(word == null)
            return false;
        for(String sum : sums ){
            if(sum.equals(word.toLowerCase()))
                return true;
        }
        return false;
    }

    private boolean isPrice(String price){

        if(price.charAt(0) == '$' && isNumericDouble(price.substring(1)))
            return true;
        if(index < allTokens.length - 1 && isNumericDouble(price)){
            String word = allTokens[index+1];
            if(equalToSum(word))
                return true;
        }

        return false;
    }

    private void handlePrice(String currToken ){
        String price = "";
        boolean sign ;
        String[] priceTerms = new String[4];
        int priceTermIndex = 0;
        for(int i = index ; i <allTokens.length && i<index+4 ; i++ ){
            if(allTokens[i].equals("")) {
                index++;
                continue;
            }
            priceTerms[priceTermIndex] = allTokens[i];
            priceTermIndex++;
        }
        for (int i =0 ; i<priceTerms.length ; i++){
            if(priceTerms[i] == null)
                priceTerms[i] = "";
        }

        if (priceTerms[0] == null){
            index++;
            return;
        }


        sign =(priceTerms[0].charAt(0) == '$');

        if(equalToSum(priceTerms[1])){
            priceTerms[1] = priceTerms[1].toLowerCase();
            if(priceTerms[1].equals("Dollars")){
                price = numericToPrice(priceTerms[0],priceTerms[1],"", sign, true , false);
                index= index+2;
            }else if(priceTerms[1].equals("million") || priceTerms[1].equals("billion") ||priceTerms[1].equals("trillion") ){
                if(priceTerms[2].equals("U.S.") && priceTerms[3].equals("dollars")) {
                    price = numericToPrice(priceTerms[0], priceTerms[1],"",  sign, true, true);
                    index= index+4;
                }else { // $100 million
                    price = numericToPrice(priceTerms[0], priceTerms[1],"",  sign, false, false);
                    index= index +2;
                }
            } else if (priceTerms[1].equals("m") || priceTerms[1].equals("bn")) {
                if(priceTerms[2].equals("Dollars")) {
                    price = numericToPrice(priceTerms[0], priceTerms[1],"",  sign, true, false);
                    index= index +3;
                }else {
                    price = numericToPrice(priceTerms[0], priceTerms[1],"",  sign, false, false);
                    index++;
                }
            }else if(priceTerms[2].equals("U.S.") && priceTerms[3].equals("dollars")) {
                //trillion
                price = numericToPrice(priceTerms[0], priceTerms[1],"",  sign, true, true);
                index = index + 4;
            }else if(isFraction(priceTerms[1]) && priceTerms[2].equals("Dollars")){
                price = numericToPrice(priceTerms[0],"",priceTerms[1],  sign, true , false);
                index= index + 3;
            }else{
                price = numericToPrice(priceTerms[0],"","",  sign, false , false);
                index++;
            }
        }else{
            price = numericToPrice(priceTerms[0],"","",  sign, false , false);
            index++;
        }
        insertTermDic(price);
    }

    private boolean isNumericDouble (String docToken){ //checks if the token is a number

        try {

            String afterReplace = docToken.replace(",","");
            Double.parseDouble(afterReplace);
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

        String notEsrony = numToken;
        if (numToken.charAt(0) == '.')
            notEsrony = "0"+numToken ;
        if (Double.parseDouble(notEsrony.replace(",", "")) < 1000000)
            return true;
        return false;
    }

    private boolean isThousand (String numToken){
        String num  = numToken.replace("," , "");
        if (Double.parseDouble(num) >= 1000 && (Double.parseDouble(num) < 1000000))
            return true;
        return false;
    }

    private boolean isMillion (String numToken){

        if (Double.parseDouble(numToken) >= 1000000 && (Double.parseDouble(numToken) < 1000000000))
            return true;
        return false;
    }

    private boolean lessThenThousand(String numToken){

        try {
            if (Double.parseDouble(numToken) < 1000)
                return true;
        }catch (NumberFormatException e){
            return false;
        }
        return false;
    }

    private boolean isBillion (String numToken){
        if (Double.parseDouble(numToken) >= 1000000000 )
            return true;
        return false;
    }

    private boolean isMonths(String token){
        for (String month : months)
            if ((token.toLowerCase()).equals(month.toLowerCase()))
                return true;
        return false;
    }

    /**
     *  function that get a number and remove
     *  all number after the 3 char after the "."
     * @param numToken - number
     * @return correct num.
     */
    private String returnDouble(String numToken){

        if(numToken.contains(".")){
            try{

                Double test_d =Double.parseDouble(numToken);
                return df.format(test_d);
            }catch (Exception e){
                e.printStackTrace();
                return numToken;
            }
        }else
            return numToken;
    }

    /**
     * function that shift the char "." number of times.
     * @param numToken - number
     * @param shift - the number of shift for "."
     * @return shift number
     */
    private String shiftLeft(String numToken ,int shift ){

        /*
        try {
            Double.parseDouble(numToken);
        }
        catch (Exception e){
            e.printStackTrace();
            return numToken;
        }
        */
        return  df.format(new BigDecimal(numToken).movePointLeft(shift));
    }

    private boolean isNumericDate(String numToken){
        if(numToken.length()>4 || numToken.length() <2)
            return false;
        String numTerm = numToken;
        try {
            int num = Integer.parseInt(numTerm) ;
            if(numTerm.length() == 2 && num < 32 && num > 0) {
                return true;
            }else if(numTerm.length() == 4)
                return true;
        } catch(NumberFormatException e){
            return false;
        }
        return false;
    }


    private boolean isNumericDayDate(String numTerm){

        try {
            int num = Integer.parseInt(numTerm) ;
            if(numTerm.length() == 2 && num < 32 && num > 0)
                return true;
            else
                return false;
        } catch(NumberFormatException e){
            return false;
        }
    }

    private boolean isNumericYearDate(String numTerm){

        try {
            int num = Integer.parseInt(numTerm) ;
            if(numTerm.length() == 4 )
                return true;
            else
                return false;
        } catch(NumberFormatException e){
            return false;
        }
    }

    private boolean isDate (String docToken) { //checks if the token is a month (date)

        boolean year = false;
        String yearTerm = "";
        if (index + 1 < allTokens.length) {
            String nextTerm = startEndWord(allTokens[index + 1]);
            if ((isNumericDayDate(currTokenStartEnd) && isMonths(nextTerm))) {
                if (index + 2 < allTokens.length) {
                    yearTerm = startEndWord(allTokens[index + 2]);
                    if (isNumericYearDate(yearTerm))
                        year = true;
                }
                if (year) { //20 june 1984
                    insertTermDic(yearTerm+"-"+turnMonthToNumber(nextTerm) + "-" + currTokenStartEnd);
                    index++;
                } else  ///20 June
                    insertTermDic(turnMonthToNumber(nextTerm) + "-" + currTokenStartEnd);
                index = index + 2;
                return true;
            } else if ((isNumericDate(nextTerm) && isMonths(docToken))) {
                ///June 4 or May 1994
                if(nextTerm.length() == 4)
                    insertTermDic(nextTerm+"-"+turnMonthToNumber(currTokenStartEnd));
                else
                    insertTermDic(turnMonthToNumber(currTokenStartEnd)+"-"+nextTerm);
                index = index + 2;
                return true;
            }
        } else
            return false;
        return false;
    }

    private boolean isPercent(String term){
        String secondTerm= "";
        if(index < allTokens.length-1) {
            secondTerm = startEndWord(allTokens[index + 1]);
            if ((isNumericDouble(term)) && secondTerm.equals("%"))
                return true;
            else if ((isNumericDouble(term)) && (secondTerm.toLowerCase().equals("percent") || secondTerm.toLowerCase().equals("percentage")))
                return true;

        }
        else
            return false;
        return false;
    }

    private boolean isBetween(String term){

        if(term.toLowerCase().equals("between") && ((index+3) <= allTokens.length) ){
            if(allTokens[index+2].equals("and")){
                boolean isNumber_1 = isNumericDouble(allTokens[index+1]);
                boolean isNumber_2 = isNumericDouble(allTokens[index+3]);
                return isNumber_1 && isNumber_2;
            }
        }
        return false;
    }

    private static String turnMonthToNumber (String docMonth){ //turns the month name to number

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

    private boolean stopWord(String word){
        if(stopWords != null && stopWords.size()>0){
            if(stopWords.contains(word) || stopWords.contains(word.toLowerCase()) || stopWords.contains(word.toUpperCase()))
                return true;
        }
        return false;
    }

    public String startEndWord(String word){

        if(word.equals("U.S."))
            return word;


        int size = word.length();
        for(int startindex =0 ; startindex  < size ; startindex ++){
            if(word.charAt(0 ) == '.' ||  word.charAt(0) == ',' || word.charAt(0 ) == '-' ||word.charAt(0 ) == '\'' ) {
                word = word.substring(1);
            }
            else
                break;
        }


        for(int endIndex = word.length()-1 ; endIndex  >= 1 ; endIndex --){
            if(word.charAt(word.length()-1 ) == '.' ||  word.charAt(word.length()-1) == ',' || word.charAt(word.length()-1 ) == '-'|| word.charAt(word.length()-1 ) == '\'' ) {

                word = word.substring(0,word.length()-1);
            }
            else
                break;
        }

        if(word.length() == 1)
            return "";
        return word;
    }

    private void handleWords(String word){
        word = currTokenStartEnd;
        word.replace("%" ,"");
        if(iSstemmer)
            word = stemmer.getStermTerm(currTokenStartEnd);



        if(word != null && word.length() > 1) {
            /// check if word is in lower letters
            if(newDoc.termDic.containsKey(word)){
                insertTermDic(word);
            }
            else if (word.equals(word.toLowerCase())) {
                /// word is save in the Dic

                if(newDoc.termDic.containsKey(word.toUpperCase())){
                    //check if the word is save as upper case
                    changeUpperCaseToLowerCase(word);

                }
                else{
                    //first occur
                    insertFirstOccur(word);
                }
            }
            ///check if the word is all in upper letter.
            else if (word.equals(word.toUpperCase())){

                if(newDoc.termDic.containsKey(word.toLowerCase())){
                    insertTermDic(word.toLowerCase());
                }
                else{
                    //first occur
                    insertFirstOccur(word);
                }
            }
            /// check if the first char in the word is upper letter
            else if (word.charAt(0) == word.toUpperCase().charAt(0)){
                if(newDoc.termDic.containsKey(word.toUpperCase())){
                    insertTermDic(word.toUpperCase());
                }
                else if(newDoc.termDic.containsKey(word.toLowerCase())){
                    insertFirstOccur(word.toLowerCase());
                }else{
                    insertFirstOccur(word.toUpperCase());
                }
            }
            index++;
            return;
        }
        index++;
    }

    private void handleDate(String date){

        boolean monthDate =false;
        boolean yearDate = false;
        boolean firstWordMonth = false;
        String numMonth;
        String  num = allTokens[index+1];

        if(isMonths(date))
            firstWordMonth = true;
        if(firstWordMonth)
            num = num.replace(".", "");

        if(firstWordMonth && num.length() == 2){ //JUNE 4
            insertTermDic(turnMonthToNumber(date)+"-"+num);
        }
        else if(firstWordMonth && num.length() == 4){ //MAY 1994
            insertTermDic(num+"-"+turnMonthToNumber(date));
        }else if(date.length() == 2){ // 14 MAY
            String year = startEndWord(allTokens[index+2]);
            if(year.length()== 4 && isNumericDate(allTokens[index+2])) {
                insertTermDic(year + "-" + turnMonthToNumber(num) + "-" + date);
                index++;
            }else
                insertTermDic(turnMonthToNumber(num)+"-"+date);
        }else{ //1994 May
            insertTermDic(date+"-"+turnMonthToNumber(allTokens[index+1]));
        }

    }

    private void handlePercent(String num) {

        insertTermDic(num + "%");
        index = index +2;

    }

    private void handleNum(String intNum) {

        if(intNum.charAt(intNum.length()-1) == '.')
            intNum = intNum.substring(0,intNum.length()-1);
        String sum = "";
        String zero = "";
        String num = "";
        intNum = intNum.replace(",", "");
        boolean twoTerm = index < allTokens.length -1;
        if (twoTerm) {
            if (allTokens[index + 1].toLowerCase().equals("thousand")) {
                sum = "K";
                zero = "000";
            } else if (allTokens[index + 1].toLowerCase().equals("million")) {
                sum = "M";
                zero = "000000";
            } else if (allTokens[index + 1].toLowerCase().equals("billion")) {
                sum = "B";
                zero = "000000000";
            }
        }
        if (isFraction(intNum) && !sum.equals("")) {
            int fraction = intNum.charAt('.');
            intNum = intNum.substring(0, fraction) + zero + intNum.substring(fraction);
        }
        if (sum.equals("") && lessThenThousand(intNum)) {
            if (twoTerm && isFraction(allTokens[index + 1])) {
                num = intNum + " " + allTokens[index + 1];

            } else {
                num = intNum;
            }
        }
        else if (isThousand(intNum) || sum.equals("K")) {//only 100,123 (K)
            num = shiftLeft(returnDouble(intNum), 3)+"K";

        } else if (isMillion(intNum) || sum.equals("M")) { // only 100,123,333 (M)
            num = shiftLeft(returnDouble(intNum), 6) + "M";

        } else if (isBillion(intNum) || sum.equals("B")){ // only 100,123,333,000 (B)
            num = shiftLeft(returnDouble(intNum), 9) + "B";

        }

        if(!num.equals(""))
            insertTermDic(num);

        if(sum.equals(""))
            index++;
        else
            index = index+2;

    }

    private boolean isLine(String token) {


        if(currTokenStartEnd.contains("-"))
            return true;
        return false;
    }

    private void handleLine(String line){

        String[] lines = line.split("-");
        if(lines == null || lines.length<2)
            return;
        else if(lines.length == 2 && index < allTokens.length-1){
            try{ /// 23-27 Feb
                lines[0]= startEndWord(lines[0]);
                lines[1]= startEndWord(lines[1]);
                int firstNumDate = Integer.parseInt(lines[0]);
                int SecondNumDate =Integer.parseInt(lines[1]);
                if(firstNumDate >0 && firstNumDate <32 && SecondNumDate > 0 && SecondNumDate <32 && isMonths(allTokens[index+1])) {
                    if(lines[0].length() == 1)

                        insertTermDic(turnMonthToNumber(allTokens[index + 1]) + "-0"+lines[0]);
                    else
                        insertTermDic(turnMonthToNumber(allTokens[index + 1]) + "-" + lines[0]);
                    if(lines[1].length() == 1)
                        insertTermDic(turnMonthToNumber(allTokens[index + 1]) + "-0"+lines[1]);
                    else
                        insertTermDic(turnMonthToNumber(allTokens[index + 1]) + "-" + lines[1]);

                    index = index+2;
                }else{
                    String addWords = "";
                    int counter = 0;
                    for(String term: lines){
                        term = startEndWord(term);
                        if(!term.equals(""))
                            if(counter == 0) {
                                addWords = term;
                                counter++;
                            }else
                                addWords = addWords+"-"+term;
                    }
                    insertTermDic(addWords);
                    index++;
                }
            }
            catch (Exception e){
                String addWords = "";
                int counter = 0;
                for(String term: lines){
                    term = startEndWord(term);
                    if(!term.equals(""))
                        if(counter == 0) {
                            addWords = term;
                            counter++;
                        }else
                            addWords = addWords+"-"+term;
                }
                insertTermDic(addWords);
                index++;
            }
        }else{
            String addWords = "";
            int counter = 0;
            for(String term: lines){
                term = startEndWord(term);
                if(!term.equals(""))
                    if(counter == 0) {
                        addWords = term;
                        counter++;
                    }else
                        addWords = addWords+"-"+term;
            }
            insertTermDic(addWords);
            index++;
        }
    }

    private void setStopWord(String path){

        File rootDirectory= null;

        if(path == null || path.length() == 0)
            rootDirectory = new File("Resources\\stop_words.txt");
        else
            rootDirectory = new File(path+"\\stop_words.txt");
        if(rootDirectory != null) {
            try {
                BufferedReader myBufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(rootDirectory)));
                for (String currLine; (currLine = myBufferedReader.readLine()) != null; )
                    stopWords.add(currLine.trim());
                myBufferedReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error in folder path");
            alert.show();
        }
    }

    public static void reset(){

    }

    public void run(){
        parseDocs();
    }
}
