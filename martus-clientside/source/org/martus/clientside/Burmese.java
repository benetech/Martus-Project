package org.martus.clientside;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Enumeration;

import org.martus.common.utilities.BurmeseUtilities;
import org.martus.swing.FontHandler;

public class Burmese
{
	public static final String SAMPLE_STORED_TEXT = "သီဟိုဠ်မှ ဉာဏ်ကြီးရှင်သည် အာယုဝဍ္ဎနဆေးညွှန်းစာကို ဇလွန်ဈေးဘေးဗာဒံပင်ထက် အဓိဋ္ဌာန်လျက် ဂဃနဏဖတ်ခဲ့သည်။ ၁၂၃၄၅၆၇၈၉၀";

	public static void main(String[] args) throws Exception
	{
		MainFrame.setUIFont(FontHandler.BURMESE_FONT);
		MainFrame frame = new MainFrame();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}
}

class MainFrame extends JFrame
{
	MainFrame() throws Exception
	{
		super();
		load();

		JPanel mainPanel = new JPanel(new BorderLayout());
		label = new JLabel(text);
		mainPanel.add(label, BorderLayout.BEFORE_FIRST_LINE);
		editor = new JTextArea(150, 10);
		mainPanel.add(editor, BorderLayout.CENTER);
		JButton update = new JButton("Update");
		update.addActionListener(new UpdateHandler());
		mainPanel.add(update, BorderLayout.AFTER_LAST_LINE);
		
		getContentPane().add(mainPanel);
		
		update();
		setSize(1000, 200);
	}

	private void load() throws Exception
	{
		text = Burmese.SAMPLE_STORED_TEXT;
		File sample = getFile();
		if(!sample.exists())
			return;
		
		byte[] bytes = new byte[(int) sample.length()];
		FileInputStream in = new FileInputStream(sample);
		in.read(bytes);
		in.close();
		text = new String(bytes, "UTF-8");
	}

	private File getFile()
	{
		return new File("Burmese.txt");
	}
	
	void save() throws Exception
	{
		File sample = getFile();
		FileOutputStream out = new FileOutputStream(sample);
		out.write(text.getBytes("UTF-8"));
		out.close();
		System.out.println("Saved to: " + sample.getAbsolutePath());
	}
	
	void extractTextFromEditor()
	{
		String displayed = editor.getText();
		text = BurmeseUtilities.getStorable(displayed);
	}

	void update()
	{
		String displayable = BurmeseUtilities.getDisplayable(text);
        //String displayable = text;
		setTitle(displayable);
		label.setText(displayable);
		editor.setText(displayable);
	}

	public static void setUIFont(String fontName)
	{
		   Enumeration keys = UIManager.getDefaults().keys();
		   while (keys.hasMoreElements()) {
		       Object key = keys.nextElement();
		       Object value = UIManager.get(key);
		       if (value instanceof FontUIResource)
		       {
		           FontUIResource orig = (FontUIResource) value;
		           Font font = new Font(fontName, orig.getStyle(), orig.getSize());
		           UIManager.put(key, new FontUIResource(font));
		       }
		   }
	 }
	
	class UpdateHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			extractTextFromEditor();
			update();
			try
			{
				save();
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

	}
	
	private JLabel label;
	private JTextArea editor;
	private String text;
}
