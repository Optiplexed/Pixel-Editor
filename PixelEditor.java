import javax.swing.JFrame;
import javax.swing.*;
import java.awt.*;
import javax.swing.JMenuBar;
import java.awt.event.*;
import javax.swing.filechooser.*;
import java.io.File;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class PixelEditor extends JFrame
   {
   private ImageRenderer imageRenderer;
   
   private File currentFile;
   private File homeDirectory;
   private BufferedImage loadedImage;
   private float scale;

   public JMenuBar createMenu()
      {
      JMenuBar menuBar = new JMenuBar();
      
      JMenu fileMenu = new JMenu("File");
      JMenu editMenu = new JMenu("Edit");
      JMenu settingsMenu = new JMenu("Settings");
      JMenu helpMenu = new JMenu("Help");
      JMenu viewMenu = new JMenu("View");
      JMenu exitMenu = new JMenu("Exit");
      
      menuBar.add(fileMenu);
      menuBar.add(editMenu);
      menuBar.add(settingsMenu);
      menuBar.add(helpMenu);
      menuBar.add(viewMenu);
      menuBar.add(exitMenu);
      
      fileMenu.add(createMenuItem("Open", this::openFileAction));
      
      return menuBar;
      }
   public JPanel createPixelManipulator()
      {
      JPanel panel = new JPanel();
      JTextField redText = new JTextField();
      JTextField greenText = new JTextField();
      JTextField blueText = new JTextField();
      
      panel.add(new JButton());
      return panel;
      }
   public JMenuItem createMenuItem(String name, ActionListener event)
      {
      JMenuItem item = new JMenuItem(name);
      if(event != null)
         {
         item.addActionListener(event);
         }
      return item;
      }
   public static class ImageRenderer extends JComponent implements MouseListener,
   MouseMotionListener, MouseWheelListener
      {
      private final PixelEditor parent;
      private int dragStartX;
      private int dragStartY;
      private int offsetX;
      private int offsetY;
      private int currX;
      private int currY;
      private double scale;
      
      public ImageRenderer(PixelEditor parent)
         {
         this.parent = parent;
         this.setBackground(Color.BLACK);
         this.addMouseListener(this);
         this.addMouseMotionListener(this);
         this.addMouseWheelListener(this);
         }
      @Override
      public void paintComponent(Graphics g) 
         {  
         Graphics2D graphicsObj = (Graphics2D) g;
         graphicsObj.scale(scale, scale);
         graphicsObj.drawImage(
            parent.getLoadedImage(), 
            currX + offsetX, 
            currY + offsetY, 
            null);
         }
      
      @Override public void mousePressed(MouseEvent e) 
         { 
         if(e.getButton() == 3 && parent.getLoadedImage() != null)
            {
            this.dragStartX = e.getX();
            this.dragStartY = e.getY();
            }
         }
      @Override public void mouseReleased(MouseEvent e) 
         { 
         if(e.getButton() == 3 && parent.getLoadedImage() != null)
            {
            this.currX += offsetX;
            this.currY += offsetY;
            }
         }
      @Override public void mouseWheelMoved(MouseWheelEvent e)
         {
         int notches = e.getWheelRotation();
         if (notches < 0) 
            {
            scale = Math.max(0.1, scale *= 0.9);
            }
         else 
            {
            scale = Math.min(10, scale *= 1.1);
            }
         this.repaint();
         }
      @Override public void mouseClicked(MouseEvent e) { }
      @Override public void mouseExited(MouseEvent e) { }
      @Override public void mouseEntered(MouseEvent e) { }
      @Override public void mouseMoved(MouseEvent e) { }
      @Override public void mouseDragged(MouseEvent e) 
         { 
         // The getButton() method does not return the button pressed, so it was omitted
         if(parent.loadedImage != null && SwingUtilities.isRightMouseButton(e))
            {  
            this.offsetX = e.getX() - dragStartX;
            this.offsetY = e.getY() - dragStartY;

            this.repaint();
            }
         }
      }
   public BufferedImage getLoadedImage()
      {
      return loadedImage;
      }
   public boolean loadImageFromFile(File file)
      {
      try{
         BufferedImage image = ImageIO.read(file);
         
         this.loadedImage = image;
         Graphics g = loadedImage.getGraphics();
         
         System.out.printf("Successfully loaded image from %s\n", file);
         return true;
         }
      catch(Exception e)
         {
         e.printStackTrace();
         return false;
         }
      }
   public void openFileAction(ActionEvent ignore)
      {
      JFileChooser jfc = new JFileChooser(
         homeDirectory != null 
         ? homeDirectory 
         : FileSystemView.getFileSystemView().getHomeDirectory());
         
      Action details = jfc.getActionMap().get("viewTypeDetails");
      details.actionPerformed(null);
      
      int returnValue = jfc.showOpenDialog(null);
      
      File selectedFile = jfc.getSelectedFile();
      
      if(selectedFile != null && loadImageFromFile(selectedFile))
         {
         this.currentFile = selectedFile;
         imageRenderer.repaint();
         }
      }
   public void openFile()
      {
      
      }
   public PixelEditor(String homeURL)
      {
      imageRenderer = new ImageRenderer(this);
            
      this.setJMenuBar(createMenu());
      this.add(imageRenderer);
      
      try{
         this.homeDirectory = new File(homeURL);
         }
      catch(Exception e)
         {
         this.homeDirectory = null;
         }
      
      this.setSize(400, 250);
      this.setTitle("Image Filter");
      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      this.setVisible(true);
      }
   public static void main(String[] args)
      {
      PixelEditor pixelEditor = new PixelEditor(args.length > 0 ? args[0] : null);
      }
   }