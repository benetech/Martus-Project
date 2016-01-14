package org.martus.client;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.Vector;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class RightToLeftChoiceBox extends Application
{
	public static void main(String[] args)
	{
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) {
		try
		{
			Parent shellContents = loadShellFxml();
			Scene scene = new Scene(shellContents, 800, 600);
			scene.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
			
			primaryStage.setTitle("Hello World!");
			primaryStage.setScene(scene);
			primaryStage.show();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}

	public Parent loadShellFxml() throws MalformedURLException, IOException
	{
		ShellController shellController = new ShellController();
		URL shellFxmlUrl = getClass().getResource("shell.fxml");
		return loadFxml(shellController, shellFxmlUrl);
	}

	public Parent loadFxml(Object shellController, URL shellFxmlUrl) throws IOException
	{
		
		FxmlLoaderWithController loader = new FxmlLoaderWithController(shellController, shellFxmlUrl);
		System.out.println("Loading " + shellFxmlUrl);
	    InputStream in = shellFxmlUrl.openStream();
	    try
	    {
	    	Parent shellContents = loader.load(in);
	    	return shellContents;
	    }
	    finally
	    {
	    	in.close();
	    }
	}

	public class FxmlLoaderWithController extends FXMLLoader
	{
		public FxmlLoaderWithController(Object controllerToUse, URL resourceAsUrl)
		{
			super(resourceAsUrl, new EmptyResourceBundle());
			
			setController(controllerToUse);
		}
	}

	public class ShellController implements Initializable
	{
		@Override
		public void initialize(URL arg0, ResourceBundle arg1)
		{
			try
			{
				Node innerContents = loadContentsFxml();
				contentPane.getChildren().addAll(innerContents);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				System.exit(1);
			}
		}

		public Node loadContentsFxml() throws MalformedURLException, IOException
		{
			ContentsController contentsController = new ContentsController();
			URL contentsFxmlUrl = getClass().getResource("contents.fxml");
			return loadFxml(contentsController, contentsFxmlUrl);
		}

		@FXML
		protected void onBack(ActionEvent event) 
		{
		}

		@FXML
		protected void onNext(ActionEvent event) 
		{
		}

		@FXML
		private Pane contentPane;

	}
	
	public class ContentsController implements Initializable
	{
		@Override
		public void initialize(URL arg0, ResourceBundle arg1)
		{
			ObservableList<ChoiceItem> list = FXCollections.observableArrayList();
			list.add(new ChoiceItem("Day Month Year"));
			list.add(new ChoiceItem("Month Day Year"));
			list.add(new ChoiceItem("Year Month Day"));
			
			dateFormatSequenceDropDown.setItems(list);
			dateFormatSequenceDropDown.getSelectionModel().select(0);
		}

		@FXML
		protected void OnLinkTorProject(ActionEvent event) 
		{
		}

		@FXML
		private ChoiceBox<ChoiceItem> dateFormatSequenceDropDown;
	}
	
	public class ChoiceItem
	{
		public ChoiceItem(String itemText)
		{
			text = itemText;
		}
		
		@Override
		public String toString()
		{
			return text;
		}
		
		private String text;
	}
	
	public class EmptyResourceBundle extends ResourceBundle
	{
		@Override
		protected Object handleGetObject(String key)
		{
			System.out.println(key);
			if(key.equals("PreferencesWhyUseTor"))
				return "This is a much longer string, which will cause things to be wrapped differently, which might trigger the bug.";
			if(key.startsWith("Step"))
				return "*";
			return "whatever";
		}

		@Override
		public Enumeration<String> getKeys()
		{
			String[] keys = new String[] {
				"StepArrow",
				"Step1",
				"WizardStep1",
				"Step2",
				"WizardStep2",
				"Step3",
				"WizardStep3",
				"Step4",
				"WizardStep4",
				"Step5",
				"WizardStep5",
				"Step6",
				"WizardStep6",
				"Button.GoBack",
				"Button.Continue",
				
				"DisplaySettings",
				"mdyOrder",
				"DateDelimiter",
				"TorSettings",
				"UseTor",
				"PreferencesWhyUseTor",
			};
			Vector<String> items = new Vector(Arrays.asList(keys));
			return items.elements();
		}
		
	}

}