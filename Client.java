package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Client extends Application {
    Stage window;
    ServerSocket server;
    Socket socket;
    BufferedReader br;
    PrintWriter out;
    TextArea messages;
    String ip;
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        window=primaryStage;
        window.setTitle("Client");
        //Connection initialization
        try {
            socket=new Socket("127.0.0.1", 9998);
            br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out=new PrintWriter(socket.getOutputStream());

            //startWriting();
        }
        catch (Exception e){
            System.out.println("Exception");
            e.printStackTrace();
        }

        //padding
        Rectangle2D visbounds= Screen.getPrimary().getVisualBounds();
        double w=visbounds.getWidth()/2;
        double h=visbounds.getHeight()-25;
        GridPane grid= new GridPane();
        //grid.setPadding(new Insets(20,20,20,20));
        grid.setVgap(12);
        grid.setHgap(15);

        //opposite person name
        Label opp_person=new Label("Server");
        opp_person.setFont(new Font("Georgia", 30));
        opp_person.setTextFill(Color.BLACK);
        opp_person.setTranslateX(15);
        //Rectangle to contain opposite name
        Rectangle r=new Rectangle();
        r.setX(0);
        r.setY(0);
        r.setWidth(w-2);
        r.setHeight(40);
        r.setFill(Color.web("#008080"));
        r.setStrokeWidth(1.5);
        r.setStroke(Color.web("#33ffad"));
        Group g=new Group();

        //Text Area
        messages=new TextArea();
        messages.setPrefSize(w-2, h-93);
        messages.setTranslateY(41);
        messages.setWrapText(true);
        messages.setEditable(false);
        //messages.setText("hi");
        //messages.appendText("\nhello");

        //Input field
        //Text Input
        TextField msg_input=new TextField();
        msg_input.setPrefSize(639, 40);
        msg_input.setTranslateX(71);
        msg_input.setTranslateY(h-50);
        msg_input.setFont(Font.font(18));

        //Send button
        Button send=new Button("Send");
        send.setTranslateX(w-75);
        send.setTranslateY(h-50);
        send.setPrefSize(70,40);
        messages.setFont(Font.font(18));
        //messages.textProperty().bind(msg_input.textProperty());
        send.setOnAction(e->{
            //Printing your message on output
            String content =msg_input.getText();
            out.println("msg");
            out.flush();
            messages.appendText("You: "+content+"\n");
            out.println(content);
            out.flush();
            msg_input.setText(null);
            msg_input.requestFocus();
        });


        //Send File Button
        Button file_button=new Button("Files");
        file_button.setTranslateX(1);
        file_button.setTranslateY(h-50);
        file_button.setPrefSize(70,40);
        file_button.setOnAction(e->{
            try {
                //BufferedReader br1=new BufferedReader(new InputStreamReader(System.in));
                FileChooser fc = new FileChooser();
                File f = fc.showOpenDialog(window);
                out.println("file");
                out.flush();
                out.println(f.getName());
                out.flush();
                messages.appendText("You: file-> "+f.getName()+"\n"+"        size-> "+f.length()+" B\n");

                //System.out.println(f.getAbsolutePath());

                FileInputStream fr = new FileInputStream(f.getAbsolutePath());
                byte[] b = new byte[(int) f.length()];
                fr.read(b);
                DataOutputStream os = new DataOutputStream(socket.getOutputStream());
                out.flush();
                os.writeInt(b.length);
                os.write(b);
            } catch (Exception e1){
                System.out.println("Error!!");
            }
        });

        g.getChildren().addAll(r, opp_person, messages, msg_input, send, file_button);
        grid.getChildren().addAll(g);

        //Making the screen
        //System.out.println(w+" "+h);
        Scene scene=new Scene(grid, w, h);
        window.setScene(scene);
        window.show();

        startReading();

    }

    public void startReading() {

        Runnable r1 = () -> {
            System.out.println("Reading started.");
            while (true) {
                try {
                    String msg = br.readLine();
                    String content = br.readLine();
                    messages.appendText("Server: " + content + "\n");
                    if (msg.equals("file")) {
                        DataInputStream is = new DataInputStream(socket.getInputStream());
                        int fl = is.readInt();
                        if (fl > 0) {
                            byte[] b = new byte[fl];
                            is.readFully(b, 0, b.length);
                            FileOutputStream fr = new FileOutputStream("D:\\"+content);
                            fr.write(b, 0, b.length);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Exception2");
                    e.printStackTrace();
                }
            }
        };
        new Thread(r1).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
