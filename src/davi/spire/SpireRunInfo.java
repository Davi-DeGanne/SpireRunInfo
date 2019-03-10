package davi.spire;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.security.GeneralSecurityException;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class SpireRunInfo {
	
    public static void main(String[] args) {

        PrintStream o = null;
		try {
			o = new PrintStream("log.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
        System.setOut(o); 
    	
        try {
        	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
        		| IllegalAccessException | UnsupportedLookAndFeelException e) {
        	e.printStackTrace();
        	return;
        }
        //Schedule a job for the event-dispatching thread:
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
        
        long updatePeriod;
		try {
			updatePeriod = Configuration.get().getUpdatePeriod();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
        while (true) {
        	try {
				SheetUpdater.get().update();
			} catch (IOException | GeneralSecurityException e) {
				e.printStackTrace();
				return;
			}
        	
        	try {
				Thread.sleep(updatePeriod);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
        }
    }
     
    private static void createAndShowGUI() {
        //Check the SystemTray support
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }
        
        final TrayIcon trayIcon = new TrayIcon(createImage("trayIcon.jpg", "tray icon"));
        final SystemTray tray = SystemTray.getSystemTray();
        trayIcon.setImageAutoSize(true);
        
        
        
        try {
        	tray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
        
        
        final PopupMenu popup = new PopupMenu();
         
        // Create a popup menu components
        MenuItem aboutItem = new MenuItem("About");
        MenuItem exitItem = new MenuItem("Exit");
         
        //Add components to popup menu
        popup.add(aboutItem);
        popup.addSeparator();
        popup.add(exitItem);
         
        trayIcon.setPopupMenu(popup);
        trayIcon.setToolTip("SpireRunInfo");
        trayIcon.displayMessage("SpireRunInfo", "SpireRunInfo started successfully. Right click tray icon to exit.", TrayIcon.MessageType.NONE);
         
        aboutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,
                        "SpireRunInfo v0.8 Beta\nCreated by Davi\nPlease contact DaviBones#6180 on Discord with questions, suggestions, and/or bug reports.");
            }
        });
         
         
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tray.remove(trayIcon);
                System.exit(0);
            }
        });
        
    }
     
    //Obtain the image URL
    protected static Image createImage(String path, String description) {
        URL imageURL = SpireRunInfo.class.getResource(path);
         
        if (imageURL == null) {
            System.err.println("Resource not found: " + path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }
}