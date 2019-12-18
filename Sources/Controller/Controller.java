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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller {

    public TextField documentPath ;
    public TextField postingPath;
    public Button start;
    public Button browse_Doc;
    public Button browse_posting;
    public Button restart;
    public Button displayInv;
    public Button loadInv;
    public CheckBox stemming;
    String docPath= "" ;
    String postingPathSaved = "" ;


    public void onStart(){

        docPath = documentPath.getText();
        postingPathSaved = this.postingPath.getText();
        boolean stemming =this.stemming.isSelected();
        if(docPath.equals("") || postingPathSaved.equals("")){
            displayError("You have to fill the two paths");
        }else{

            try {
                ReadFile rd = new ReadFile(docPath);
                Parse parse = new Parse(stemming, docPath);
                Indexer indexer = new Indexer(stemming ,postingPathSaved);

                Thread threadReadFile = new Thread(rd);
                Thread threadParse = new Thread(parse);
                Thread threadIndexer = new Thread(indexer);


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

    public void onRestart(){

        File rootFile = new File(documentPath.getText());
        File[] Files = rootFile.listFiles();
        for(File f : Files){
            File[] textFiles = f.listFiles();
            for (File textFile : textFiles)
                textFile.delete();
            f.delete();
        }
        rootFile.delete();
        documentPath.clear();
        postingPath.clear();
        stemming.setSelected(false);
        Parse.restart();
        Indexer.restart();

    }

    public void onDisplayInv(){

        ObservableList<Map.Entry<String, Integer>> invertedList = getObservableList();
        Stage stage = new Stage();
        stage.setTitle("Inverted Table");

        TableColumn<Map.Entry<String, Integer>, String> tokenCol = new TableColumn<>("Token");
        tokenCol.setMinWidth(200);
        tokenCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKey()));

        TableColumn<Map.Entry<String, Integer> , Integer> numCol = new TableColumn<>("#");
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

        Path out = Paths.get(postingPathSaved+"\\LoadInv");
        Files.write(out,getInvAsList());

    }

    private ObservableList<Map.Entry<String, Integer>> getObservableList(){

        ObservableList<Map.Entry<String, Integer>> invertedList = FXCollections.observableArrayList();
        for(Map.Entry<String, int[]> entry : Indexer.termDic.entrySet()){
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
        for(Map.Entry<String, int[]> entry : Indexer.termDic.entrySet())
            InvList.add(entry.getKey()+","+entry.getValue()[0]+","+entry.getValue()[1]+","+entry.getValue()[2]);

        return InvList;


    }

    private void displayError(String error){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(error);
        alert.show();
    }



}
