package org.martus.client.swingui.jfx;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
Technology, Inc. (Benetech).

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

 */

/**
 * 
 * This class is just a tiny sample demo that was used to 
 * send to the jfx mailing list to find out why scrollpanes 
 * were not working as expected. The original code below 
 * used Pane, and didn't work correctly. Following advice 
 * from the list, switching to StackPane solved the problem.
 * 
 * This can probably be deleted at some point. 
 *
 */
public class FxScrollPaneTest extends Application
{
    public static void main(String[] args) 
    {
        launch(args);
    }
    
    @Override
	public void start(Stage primaryStage) 
	{
	    primaryStage.setTitle("Hello World!");
	    
	    GridPane grid = new GridPane();
	    grid.setBackground(new Background(new BackgroundFill(Color.DARKMAGENTA, null, null)));

	    for(int i = 0; i < 10; ++i)
	    {
	    	grid.add(new Label("" + i), 0, i);
	    	grid.add(new Label("this is a longer value to invoke scrolling"), 1, i);
	    }
	    
	    StackPane root = new StackPane();
	    root.setBackground(new Background(new BackgroundFill(Color.ALICEBLUE, null, null)));
	    StackPane inner = new StackPane();
	    inner.setBackground(new Background(new BackgroundFill(Color.BEIGE, null, null)));
	    root.getChildren().add(inner);
	    ScrollPane scroller = new ScrollPane(grid);
	    scroller.setBackground(new Background(new BackgroundFill(Color.CHARTREUSE, null, null)));
	    inner.getChildren().add(scroller);
	    primaryStage.setScene(new Scene(root, 300, 250));
	    primaryStage.show();
	}
	
}
