package Controller;

import Part_1.Indexer;
import Part_1.Parse;
import Part_1.ReadFile;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class Controller {

    public TextField documentPath ;
    public TextField postingPath;
    public Button start;
    public Button browse_Doc;
    public Button browse_posting;
    public Button reset;
    public Button displayInv;
    public Button loadInv;
    public CheckBox stemming;
    public String docPath= "" ;
    public String postingPathSaved = "" ;
    public boolean alreadyIndexedWithStemming = false;
    public boolean alreadyIndexedWithoutStemming = false;
    private boolean startsIndexing = false;
    public long startTime;
    public String loadDicPath;
    public boolean stemm = false;

    public void onStart(){

 /*       loadInv.setVisible(false);
        displayInv.setVisible(false);
        browse_Doc.setVisible(false);
        browse_posting.setVisible(false);*/

        docPath = documentPath.getText();
        postingPathSaved = this.postingPath.getText();
        stemm = this.stemming.isSelected();
        String infoToDisplay = "";

        if(docPath.equals("") || postingPathSaved.equals("")){
            displayError("You have to fill the two paths");
        }else{

            try {
                ReadFile rd = new ReadFile(docPath);
                ReadFile.stopParser = false;
                Parse parse = new Parse(stemm, docPath);
                Parse.documentsSet = new LinkedBlockingQueue<>(3000);
                Parse.stopIndexer =false;
                Indexer indexer = new Indexer(stemm ,postingPathSaved);
                Indexer.currChunk = new LinkedBlockingQueue<>(3000);
                Indexer.termDic = new HashMap<>();
                Indexer.allDocuments = new HashMap<>();

                Thread threadParse = new Thread(parse);
                Thread threadReadFile = new Thread(rd);
                Thread threadIndexer = new Thread(indexer);

                startTime = System.nanoTime();
                startsIndexing = true;

                if (stemm)
                    alreadyIndexedWithStemming = true;
                else
                    alreadyIndexedWithoutStemming = true;

                threadParse.start();
                threadReadFile.start();
                threadIndexer.start();

                threadParse.join();
                threadReadFile.join();
                threadIndexer.join();
            }
            catch(Exception e){
                e.printStackTrace();
            }finally {
 /*               loadInv.setDisable(false);
                displayInv.setDisable(false);
                browse_Doc.setDisable(false);
                browse_posting.setDisable(false);*/
                double totalTimeInSeconds = (System.nanoTime() - startTime) * Math.pow(10, -9);
                int numOfDocs = Indexer.allDocuments.size();
                int numOfUniqueTerms = Indexer.termDic.size();
                infoToDisplay = "Total documents indexed: " + numOfDocs + "\nNumber of unique " +
                        "terms in the corpus: " + numOfUniqueTerms + "\nTotal process' running time: "
                        + totalTimeInSeconds + " seconds";

                displayInfo(infoToDisplay);
                startsIndexing =false;
                System.out.println("Done!!!!!!!");

            }
        }
    }

    public void onBrowseDoc(){ Browse(documentPath); }

    public void onBrowsePosting(){ Browse(postingPath); }

    private void Browse(TextField text){
        JButton open = new JButton();
        JFileChooser jc = new JFileChooser();
        jc.setCurrentDirectory(new File("."));
        jc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if(jc.showOpenDialog(open) == JFileChooser.APPROVE_OPTION){
            text.setText(jc.getSelectedFile().getAbsolutePath());
        }
    }

    public void onReset(){

        postingPathSaved = this.postingPath.getText();
        if (!startsIndexing && ((alreadyIndexedWithStemming || alreadyIndexedWithoutStemming) || !postingPathSaved.equals(""))) {

                documentPath.setText("");
                postingPath.setText("");
                stemming.setSelected(false);
                File dir = new File(postingPathSaved);
                File[] dirFiles = dir.listFiles();
                if (dirFiles != null) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to reset?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
                    alert.showAndWait();
                    if (alert.getResult() == ButtonType.YES){
                        for (File fileInDir : dirFiles) {
                            if (fileInDir != null)
                                fileInDir.delete();
                        }
                        dir.delete();
                    }
                }
                else
                    displayError("No data to be reset!");

                alreadyIndexedWithStemming = false;
                alreadyIndexedWithoutStemming = false;
                Parse.reset();
                Indexer.reset();
                documentPath.clear();
                postingPath.clear();
                docPath = null;
                postingPath = new TextField();
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("No data to be reset!");
            alert.show();
        }
    }

    public void onDisplayInv(){

        ObservableList<Map.Entry<String, Integer>> invertedList = getObservableList();
        Stage stage = new Stage();
        stage.setTitle("Dictionary");

        TableColumn<Map.Entry<String, Integer>, String> tokenCol = new TableColumn<>("term");
        tokenCol.setMinWidth(200);
        tokenCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKey()));

        TableColumn<Map.Entry<String, Integer> , Integer> numCol = new TableColumn<>("total shows");
        numCol.setMinWidth(100);
        numCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getValue()).asObject());

        TableView table = new TableView<>();
        table.setItems(invertedList);
        table.getColumns().addAll(tokenCol, numCol);

        VBox vbox = new VBox();
        vbox.getChildren().addAll(table);

        Scene scene = new Scene(vbox);
        stage.setScene(scene);
        stage.show();
    }

    public void onLoadInv() throws IOException {

        postingPathSaved = this.postingPath.getText();
        stemm = this.stemming.isSelected();
        String line1 = "";
        String term = "";
        int[] termData = new int[2];
        int numOfDocs = 0;
        String[] allDocsInfo;
        int totalShows = 0;

        if (postingPathSaved.equals("")) {
            displayError("You have to fill the posting path to load a dictionary!");

        } else { //correct posting files  path.

            if (new File(postingPathSaved + "\\stemming.txt").exists() && stemm)
                loadDicPath = postingPathSaved + "\\stemming.txt";
            else if (new File(postingPathSaved + "\\nonStemming.txt").exists() && !stemm)
                loadDicPath = postingPathSaved + "\\nonStemming.txt";
            else
                displayError("No files to load from the given path!");

            ///////////////////////////////////////////////////////////////// ?????????
            try {
                Scanner scanner = new Scanner((new File(loadDicPath)));

                while (scanner.hasNextLine()) {

                    line1 = scanner.nextLine();
                    term = line1.substring(0, line1.indexOf("|")); // only term itself, with no other data.
                    totalShows = 0;
                    allDocsInfo = new String[line1.length()];
                    numOfDocs = line1.split("|").length;
                    line1 = line1.substring(line1.indexOf("|") + 1); //without term itself.

                    if (!line1.contains("|")) { //only one doc in list.
                        numOfDocs = 1;
                        totalShows = Integer.parseInt(line1.substring(line1.indexOf(":")+1 , line1.indexOf(";")));
                        line1 = null; //finish
                    }

                    numOfDocs = 0;
                    while (line1 != null) {
                        allDocsInfo = line1.split(":|\\;|\\|"); //each cell is a different doc and its values
                        numOfDocs++;
                        totalShows = totalShows + Integer.parseInt(allDocsInfo[1]);
                        line1 = line1.substring(line1.indexOf("|") + 1); // next docID of term and info...
                        if (!line1.contains("|")) {
                            numOfDocs++;
                            allDocsInfo = line1.split(":|\\;|\\|"); //each cell is a different doc and its values
                            totalShows = totalShows + Integer.parseInt(allDocsInfo[1]);
                            break;
                        }
                    }

                    termData[0] = numOfDocs;
                    termData[1] = totalShows;

                    Indexer.termDic.put(term, termData); //adds to dic.
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                displayInfo("Dictionary was successfully loaded to memory.");

            }
        }
    }





    private ObservableList<Map.Entry<String, Integer>> getObservableList(){

        ObservableList<Map.Entry<String, Integer>> invertedList = FXCollections.observableArrayList();
        TreeMap<String,int[]> sortedList = new TreeMap<>(new Comparator<String>(){

            @Override
            public int compare(String s1, String s2) {
                int result = s1.compareToIgnoreCase(s2);
                if( result == 0 )
                    result = s1.compareTo(s2);
                return result;
            }
        });
        sortedList.putAll(Indexer.termDic);
        for(Map.Entry<String, int[]> entry : sortedList.entrySet()){
            Map.Entry<String, Integer> newEntry = new Map.Entry<String, Integer>() {
                @Override
                public String getKey() {
                    return entry.getKey();
                }

                @Override
                public Integer getValue() {
                    return entry.getValue()[1];
                }

                @Override
                public Integer setValue(Integer value) {
                    return null;
                }
            };
            invertedList.add(newEntry);
        }
        return invertedList;
    }

    private List<String> getInvAsList(){

        List<String> InvList = new ArrayList<>();
        TreeMap<String,int[]> sortedList = new TreeMap<>(new Comparator<String>(){

            @Override
            public int compare(String s1, String s2) {
                int result = s1.compareToIgnoreCase(s2);
                if( result == 0 )
                    result = s1.compareTo(s2);
                return result;
            }
        });
        sortedList.putAll(Indexer.termDic);
        for(Map.Entry<String, int[]> entry: sortedList.entrySet()) {
            int[] value = entry.getValue();
            InvList.add(entry.getKey() + "," + value[0] + "," + value[1]) ;
        }

        return InvList;
    }


    private void displayError(String error){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(error);
        alert.show();
    }

    private void displayInfo(String info){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(info);
        alert.show();
    }

    private boolean alreadyIndexedAll() {
        return alreadyIndexedWithStemming && alreadyIndexedWithoutStemming;
    }
}
