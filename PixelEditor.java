import javax.swing.JFrame;
import javax.swing.*;
import java.awt.*;
import javax.swing.JMenuBar;
import java.awt.event.*;
import javax.swing.filechooser.*;
import java.io.File;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class PixelEditor extends JFrame
   {
   private ImageRenderer imageRenderer;
   
   private File currentFile;
   private File homeDirectory;
   private BufferedImage loadedImage;
   private float scale;
   
   private ImageState currState = new ImageState(null, null, null, null);
   private ImageState firstState = currState;
   private int numStates = 1;
   
   //Used by the undo and redo buttons
   public static class ImageState
      {
      private BufferedImage modifiedImage;
      private File file;
      private ImageState next;
      private ImageState prev;
      
      public ImageState(BufferedImage modifiedImage, File file,  ImageState next, ImageState prev)
         {
         this.modifiedImage = modifiedImage;
         this.file = file;
         this.next = next;
         this.prev = prev;
         }
      public ImageState getNext()
         {
         return this.next;
         }
      public ImageState getPrev()
         {
         return this.prev;
         }
      public BufferedImage getImage()
         {
         return this.modifiedImage;
         }
      public File getFile()
         {
         return this.file;
         }
      }
   
   public static class ImageRenderer extends JComponent implements MouseListener,
   MouseMotionListener, MouseWheelListener, KeyListener
      {
      private final PixelEditor parent;
      private int dragStartX;
      private int dragStartY;
      private int offsetX;
      private int offsetY;
      private int currX;
      private int currY;
      private double scale = 1.0;
      
      public ImageRenderer(PixelEditor parent)
         {
         this.parent = parent;
         
         this.addMouseListener(this);
         this.addMouseMotionListener(this);
         this.addMouseWheelListener(this);
         
         this.addKeyListener(this);
         this.setFocusable(true);
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
      @Override public void mouseDragged(MouseEvent e) 
         { 
         if(parent.getLoadedImage() != null && SwingUtilities.isRightMouseButton(e))
            {  
            this.offsetX = (int)((e.getX() - dragStartX) * (1 / scale) / 1.5);
            this.offsetY = (int)((e.getY() - dragStartY) * (1 / scale) / 1.5);

            this.repaint();
            }
         }
      @Override public void mouseReleased(MouseEvent e) 
         { 
         if(e.getButton() == 3 && parent.getLoadedImage() != null)
            {
            currX += this.offsetX;
            currY += this.offsetY;
            this.offsetX = 0;
            this.offsetY = 0;
            }
         }
      public void zoomIn()
         {
         scale = Math.max(0.1, scale *= 0.9);
         this.repaint();
         }
      public void zoomOut()
         {
         scale = Math.min(10, scale *= 1.1);
         this.repaint();
         }
      public void resetView()
         {
         scale = 1.0;
         this.currX = 0;
         this.currY = 0;
         this.repaint();
         }
      @Override public void mouseWheelMoved(MouseWheelEvent e)
         {
         int notches = e.getWheelRotation();
         if (notches > 0) 
            {
            zoomIn();
            }
         else 
            {
            zoomOut();
            }
         }
      @Override public void keyPressed(KeyEvent e) 
         {
         //Undo button pressed
         if(e.getKeyChar() == 0x1A)
            {
            this.parent.undoAction(null);
            }
         //Redo button pressed
         else if(e.getKeyChar() == 0x19)
            {
            this.parent.redoAction(null);
            }
         }
      @Override public void mouseMoved(MouseEvent e) 
         { 
         this.requestFocusInWindow();
         }
      @Override public void keyReleased(KeyEvent e) { }
      @Override public void keyTyped(KeyEvent e) { }
      @Override public void mouseClicked(MouseEvent e) { }
      @Override public void mouseExited(MouseEvent e) { }
      @Override public void mouseEntered(MouseEvent e) { }
      }

   public JMenuBar createMenu()
      {
      JMenuBar menuBar = new JMenuBar();
      
      JMenu fileMenu = new JMenu("File");
      JMenu editMenu = new JMenu("Edit");
      JMenu viewMenu = new JMenu("View");
      JMenu exitMenu = new JMenu("Exit");
      
      menuBar.add(fileMenu);
      menuBar.add(editMenu);
      menuBar.add(viewMenu);
      
      fileMenu.add(createMenuItem("Open", this::openFileAction));
      fileMenu.add(createMenuItem("Save", this::saveFileAction));
      
      viewMenu.add(createMenuItem("Zoom in", (e) -> this.imageRenderer.zoomIn()));
      viewMenu.add(createMenuItem("Zoom out", (e) -> this.imageRenderer.zoomOut()));
      viewMenu.add(createMenuItem("Reset View", (e) -> this.imageRenderer.resetView()));
      
      editMenu.add(createMenuItem("Undo", this::undoAction));
      editMenu.add(createMenuItem("Redo", this::redoAction));
            
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
   public JPanel createButtons()
      {
      JPanel buttonPanel = new JPanel();
      JButton grayScaleButton = new JButton("Gray Scale");
      JButton blurButton = new JButton("Blur");
      JButton sepiaButton = new JButton("Sepia");
      JButton resetButton = new JButton("Reset");
      
      grayScaleButton.addActionListener((e) -> {
         if(this.getLoadedImage() != null)
            {
            this.setLoadedImage(Filters.grayscale(this.getLoadedImage()));
            this.imageRenderer.repaint();
            }
         });
      
      blurButton.addActionListener((e) -> {
         if(this.getLoadedImage() != null)
            {
            this.setLoadedImage(Filters.blur(this.getLoadedImage(), 3));
            this.imageRenderer.repaint();
            }
         });
         
      sepiaButton.addActionListener((e) -> {
         if(this.getLoadedImage() != null)
            {
            this.setLoadedImage(Filters.sepia(this.getLoadedImage()));
            this.imageRenderer.repaint();
            }
         });
         
      resetButton.addActionListener((e) -> {
         if(this.getLoadedImage() != null)
            {
            loadImageFromFile(currentFile, false);
            }
         });
         
      buttonPanel.add(grayScaleButton);
      buttonPanel.add(blurButton);
      buttonPanel.add(sepiaButton);
      buttonPanel.add(resetButton);
      
      return buttonPanel;
      }
   public PixelEditor(String homeURL)
      {     
      this.setJMenuBar(createMenu());
      
      this.getContentPane().setLayout(new BorderLayout());
      
      this.add(this.imageRenderer = new ImageRenderer(this), BorderLayout.CENTER);
      this.add(createButtons(), BorderLayout.SOUTH);
      
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
   public BufferedImage getLoadedImage()
      {
      return loadedImage;
      }
   public void setLoadedImage(BufferedImage image, boolean undoable)
      {
      if(undoable)
         {
         ImageState newState = new ImageState(image, this.currentFile, null, currState);
         currState.next = newState;
         currState = newState;
      
         if(++numStates > 10 && firstState.next != null) 
            {
            firstState = firstState.next;
            firstState.prev = null;
            }
         }
      System.out.println("Changed Image");
      this.loadedImage = image;
      }
   public void setLoadedImage(BufferedImage image)
      {
      this.setLoadedImage(image, true);
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
      
      if(selectedFile != null && loadImageFromFile(selectedFile, true))
         {
         this.currentFile = selectedFile;
         imageRenderer.repaint();
         }
      }
   public void saveFileAction(ActionEvent ignore)
      {
      if(this.getLoadedImage() == null)
         {
         JOptionPane.showMessageDialog(null, "Load an image before saving it!", "Error", JOptionPane.ERROR_MESSAGE);
         return;
         }
      JFileChooser jfc = new JFileChooser(
         homeDirectory != null 
         ? homeDirectory 
         : FileSystemView.getFileSystemView().getHomeDirectory());
         
      jfc.showSaveDialog(null);
      
      File selectedFile = jfc.getSelectedFile();
      try{
         ImageIO.write(this.getLoadedImage(), "png", selectedFile);
         }
      catch(Exception e)
         {
         String errorMessage = String.format("Error saving image: %s", e.toString());
         System.out.println(errorMessage);
         JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
         }
      }
   public void undoAction(ActionEvent e)
      {
      if(this.currState.prev != null)
         {
         this.currState = currState.prev;
         this.setLoadedImage(currState.getImage(), false);
         this.currentFile = currState.getFile();
         this.repaint();
         System.out.println("Successfully undid previous action");
         }
      }
   public void redoAction(ActionEvent e)
      {
      if(this.currState.next != null)
         {
         this.currState = currState.next;
         this.setLoadedImage(currState.getImage(), false);
         this.currentFile = currState.getFile();
         this.repaint();
         System.out.println("Successfully reimplemented a future action");
         }
      }
   public boolean loadImageFromFile(File file, boolean shouldPrintMsg)
      {
      try{
         BufferedImage image = ImageIO.read(file);
         
         setLoadedImage(image);
         if(shouldPrintMsg)
            {
            System.out.printf("Successfully loaded image from %s\n", file);
            }
         this.imageRenderer.repaint();
         return true;
         }
      catch(Exception e)
         {
         e.printStackTrace();
         return false;
         }
      }
   public static void main(String[] args)
      {
      PixelEditor pixelEditor = new PixelEditor(args.length > 0 ? args[0] : null);
      }
   }