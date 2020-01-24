/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trafficsimulator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileFilter;
/**
 *
 * @author ASUS
 */
public class MainProgram extends javax.swing.JFrame {
    File currentPathSetting;
    File data_File =null;
    boolean flagRun=false;
    CityPannel cityPannel=null;
    JFrameStatistics statisticsDialog=null;
    FileFilter fileFilter=null;    
    /**
     * Creates new form MainProgram
     */
    public MainProgram() {
        initComponents();
        jSpinnerSpawn.setValue(5);
        SpinnerNumberModel model=new SpinnerNumberModel(30, 10, 60, 1);
        jSpinnerSpawn.setModel(model);
        cityPannel=new CityPannel(this);
        cityPannel.setSize(2000, 1500);
        javax.swing.GroupLayout cityPannelLayout = new javax.swing.GroupLayout(cityPannel);
        cityPannel.setLayout(cityPannelLayout);
        cityPannelLayout.setHorizontalGroup(
            cityPannelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 3200, Short.MAX_VALUE)
        );
        cityPannelLayout.setVerticalGroup(
            cityPannelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 3200, Short.MAX_VALUE)
        );        
        jScrollPaneBody.setViewportView(cityPannel);
        
        ButtonGroup buttonGroup1=new ButtonGroup();
        buttonGroup1.add(jRadioButtonAddRoad);
        buttonGroup1.add(jRadioButtonEditRoad);
        jRadioButtonAddRoad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                editStateChanged();
            }
        });
        jRadioButtonEditRoad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                editStateChanged();
            }
        });
        jRadioButtonEditRoad.setSelected(true);
        editStateChanged();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(new Point((screenSize.width-getWidth())/2, (screenSize.height-getHeight())/2));
   
        jComboBoxZoom.removeAllItems();
        for(int i=10;i<=200;i+=10)
            jComboBoxZoom.addItem(""+i);
;
        topBarCityButtonClick(null);
        
        fileFilter=new FileFilter() {
            @Override
            public boolean accept(File file) {
                if(file.isDirectory())return true;
                return file.getName().endsWith(Constants.FILE_EXT);
            }
            @Override
            public String getDescription() {
                return "Traffic Simulator Data (*"+Constants.FILE_EXT+")";
            }
        };
        currentPathSetting=new File("current_path");
        if(currentPathSetting.exists()){
            try {
                BufferedReader br=new BufferedReader(new FileReader(currentPathSetting));
                String path=br.readLine();
                data_File =new File(path);
            } catch (Exception ex) {
                Logger.getLogger(MainProgram.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public int getSpawn(){
        return ((SpinnerNumberModel)(jSpinnerSpawn.getModel())).getNumber().intValue();
    }
    private void topBarCityButtonClick(ActionEvent evt){
        jButtonCreate.setVisible(true);
        jButtonSave.setVisible(true);
        jButtonOpen.setVisible(true);
        jButtonUndo.setVisible(true);
        jButtonRedo.setVisible(true);
        jRadioButtonAddRoad.setVisible(true);
        jRadioButtonEditRoad.setVisible(true);
        
        jButtonRun.setVisible(false);
        jButtonPause.setVisible(false);
        jButtonStop.setVisible(false);
        jButtonStatistics.setVisible(false);
        
        jButtonCity.setBackground(Color.gray);
        jButtonSimulation.setBackground(new Color(240, 240, 240));
        jLabelMode.setText("City Edit");
        
        cityPannel.setMode(CityPannel.ModeType.MT_EDIT);
        
        flagRun=false;
        
    }
    private void topBarSimulationButtonClick(ActionEvent evt){
        jButtonCreate.setVisible(false);
        jButtonSave.setVisible(false);
        jButtonOpen.setVisible(false);
        jButtonUndo.setVisible(false);
        jButtonRedo.setVisible(false);
        jRadioButtonAddRoad.setVisible(false);
        jRadioButtonEditRoad.setVisible(false);
        jButtonRun.setVisible(!flagRun);
        jButtonPause.setVisible(flagRun);
        jButtonStop.setVisible(true);
        jButtonStatistics.setVisible(true);
        
        jButtonCity.setBackground(new Color(240, 240, 240));
        jButtonSimulation.setBackground(Color.gray);
        
        jLabelMode.setText("Simulation");
        cityPannel.setMode(CityPannel.ModeType.MT_SINMULATE);
        
    }
    private void navBarRunButtonClick(ActionEvent evt){
        flagRun=true;
        jButtonRun.setVisible(false);
        jButtonPause.setVisible(true);
        jLabelStatus.setText("Run");
    }
    private void navBarPauseButtonClick(ActionEvent evt){
        flagRun=false;
        jButtonRun.setVisible(true);
        jButtonPause.setVisible(false);
        jLabelStatus.setText("Pause");
    }
    private void navBarStopButtonClick(ActionEvent evt){
        navBarPauseButtonClick(evt);
        cityPannel.stopSimulate();
        jLabelStatus.setText("Stop");
    }    
    private void navBarCreateButtonClick(ActionEvent evt){
        cityPannel.createNew();
    }
    private void navBarSaveButtonClick(ActionEvent evt){
        JFileChooser fileChooser = new JFileChooser(data_File);
        fileChooser.addChoosableFileFilter(fileFilter);
        fileChooser.setAcceptAllFileFilterUsed(true);
        int retval = fileChooser.showSaveDialog(this);
        if (retval == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file == null)return;
            if (!file.getName().toLowerCase().endsWith(Constants.FILE_EXT)) {
                file = new File(file.getParentFile(), file.getName() + Constants.FILE_EXT);
            }
            data_File =file;
            if(!cityPannel.save(data_File)){
                JOptionPane.showConfirmDialog(this, "Can't save");
            }
        }
    }
    private void navBarOpenButtonClick(ActionEvent evt){
        JFileChooser fileChooser = new JFileChooser(data_File);
        fileChooser.addChoosableFileFilter(fileFilter);
        fileChooser.setAcceptAllFileFilterUsed(true);
        int retval = fileChooser.showOpenDialog(this);
        if (retval == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file == null)return;
            data_File =file;
            if(!cityPannel.load(file)){
                JOptionPane.showMessageDialog(this, "Can't open");
            }
        }
    }
    private void editStateChanged(){
        if(jRadioButtonAddRoad.isSelected())cityPannel.setEditMode(CityPannel.EditType.ET_ADD);
        if(jRadioButtonEditRoad.isSelected())cityPannel.setEditMode(CityPannel.EditType.ET_EDIT);
    }
    private void formClosing(){
        try {
            if(data_File !=null){
                PrintWriter pw=new PrintWriter(currentPathSetting);
                if(data_File.isFile())
                    pw.println(data_File.getAbsoluteFile().getParent());
                else if(data_File.isDirectory())
                    pw.println(data_File.getAbsoluteFile());
                pw.flush();
                pw.close();                
            }
            
            dispose();
            
        } catch (Exception ex) {
            Logger.getLogger(MainProgram.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void showStatistics(){
        if(statisticsDialog==null){
            statisticsDialog=new JFrameStatistics(cityPannel);
        }
        statisticsDialog.setVisible(true);
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanelTopBar = new javax.swing.JPanel();
        jButtonCity = new javax.swing.JButton();
        jButtonSimulation = new javax.swing.JButton();
        jComboBoxZoom = new javax.swing.JComboBox<String>();
        jLabel1 = new javax.swing.JLabel();
        jScrollPaneBody = new javax.swing.JScrollPane();
        jPanelNavBar = new javax.swing.JPanel();
        jButtonCreate = new javax.swing.JButton();
        jButtonSave = new javax.swing.JButton();
        jButtonOpen = new javax.swing.JButton();
        jButtonRun = new javax.swing.JButton();
        jButtonStop = new javax.swing.JButton();
        jButtonStatistics = new javax.swing.JButton();
        jButtonPause = new javax.swing.JButton();
        jRadioButtonAddRoad = new javax.swing.JRadioButton();
        jRadioButtonEditRoad = new javax.swing.JRadioButton();
        jButtonUndo = new javax.swing.JButton();
        jButtonRedo = new javax.swing.JButton();
        jPanelSpawn = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jSpinnerSpawn = new javax.swing.JSpinner();
        jPanelStatus = new javax.swing.JPanel();
        jLabelModeLabel = new javax.swing.JLabel();
        jLabelMode = new javax.swing.JLabel();
        jLabelStatusLabel = new javax.swing.JLabel();
        jLabelStatus = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanelTopBar.setBackground(new java.awt.Color(0, 102, 102));
        jPanelTopBar.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jButtonCity.setBackground(new java.awt.Color(51, 255, 51));
        jButtonCity.setText("City");
        jButtonCity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCityActionPerformed(evt);
            }
        });

        jButtonSimulation.setBackground(new java.awt.Color(204, 0, 51));
        jButtonSimulation.setText("Simulation");
        jButtonSimulation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSimulationActionPerformed(evt);
            }
        });

        jComboBoxZoom.setEditable(true);
        jComboBoxZoom.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "100" }));
        jComboBoxZoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxZoomActionPerformed(evt);
            }
        });

        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Zoom:");

        javax.swing.GroupLayout jPanelTopBarLayout = new javax.swing.GroupLayout(jPanelTopBar);
        jPanelTopBar.setLayout(jPanelTopBarLayout);
        jPanelTopBarLayout.setHorizontalGroup(
            jPanelTopBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTopBarLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonCity, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonSimulation, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxZoom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelTopBarLayout.setVerticalGroup(
            jPanelTopBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTopBarLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelTopBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonCity)
                    .addComponent(jButtonSimulation)
                    .addComponent(jComboBoxZoom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelNavBar.setBackground(new java.awt.Color(0, 153, 102));
        jPanelNavBar.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jButtonCreate.setBackground(new java.awt.Color(153, 255, 102));
        jButtonCreate.setText("New City");
        jButtonCreate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCreateActionPerformed(evt);
            }
        });

        jButtonSave.setBackground(new java.awt.Color(153, 255, 102));
        jButtonSave.setText("Save");
        jButtonSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveActionPerformed(evt);
            }
        });

        jButtonOpen.setBackground(new java.awt.Color(153, 255, 102));
        jButtonOpen.setText("Open");
        jButtonOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpenActionPerformed(evt);
            }
        });

        jButtonRun.setBackground(new java.awt.Color(0, 255, 51));
        jButtonRun.setText("Run");
        jButtonRun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRunActionPerformed(evt);
            }
        });

        jButtonStop.setBackground(new java.awt.Color(255, 0, 0));
        jButtonStop.setText("Stop");
        jButtonStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStopActionPerformed(evt);
            }
        });

        jButtonStatistics.setBackground(new java.awt.Color(51, 153, 255));
        jButtonStatistics.setText("Statistics");
        jButtonStatistics.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStatisticsActionPerformed(evt);
            }
        });

        jButtonPause.setBackground(new java.awt.Color(204, 204, 0));
        jButtonPause.setText("Pause");
        jButtonPause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPauseActionPerformed(evt);
            }
        });

        jRadioButtonAddRoad.setText("Add Roads");
        jRadioButtonAddRoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonAddRoadActionPerformed(evt);
            }
        });

        jRadioButtonEditRoad.setText("Edit Roads");

        jButtonUndo.setBackground(new java.awt.Color(255, 102, 153));
        jButtonUndo.setText("Undo");
        jButtonUndo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUndoActionPerformed(evt);
            }
        });

        jButtonRedo.setBackground(new java.awt.Color(255, 51, 153));
        jButtonRedo.setText("Redo");
        jButtonRedo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRedoActionPerformed(evt);
            }
        });

        jPanelSpawn.setBackground(new java.awt.Color(0, 153, 102));

        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("<html>Vehicle<br/>spawn<br/>rate</html>");

        javax.swing.GroupLayout jPanelSpawnLayout = new javax.swing.GroupLayout(jPanelSpawn);
        jPanelSpawn.setLayout(jPanelSpawnLayout);
        jPanelSpawnLayout.setHorizontalGroup(
            jPanelSpawnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSpawnLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSpinnerSpawn, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3))
        );
        jPanelSpawnLayout.setVerticalGroup(
            jPanelSpawnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelSpawnLayout.createSequentialGroup()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanelSpawnLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSpinnerSpawn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanelNavBarLayout = new javax.swing.GroupLayout(jPanelNavBar);
        jPanelNavBar.setLayout(jPanelNavBarLayout);
        jPanelNavBarLayout.setHorizontalGroup(
            jPanelNavBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelNavBarLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelNavBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButtonSave, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonOpen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonCreate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonRun, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonPause, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonStop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonStatistics, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jRadioButtonEditRoad)
                    .addComponent(jRadioButtonAddRoad)
                    .addComponent(jButtonUndo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonRedo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelSpawn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 7, Short.MAX_VALUE))
        );
        jPanelNavBarLayout.setVerticalGroup(
            jPanelNavBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelNavBarLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonCreate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonSave)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonOpen)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonRun)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonPause)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonStop)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonStatistics)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonUndo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonRedo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelSpawn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 264, Short.MAX_VALUE)
                .addComponent(jRadioButtonAddRoad)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButtonEditRoad)
                .addContainerGap())
        );

        jPanelStatus.setBackground(new java.awt.Color(102, 204, 0));
        jPanelStatus.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabelModeLabel.setForeground(new java.awt.Color(255, 255, 255));
        jLabelModeLabel.setText("Mode:");

        jLabelMode.setForeground(new java.awt.Color(255, 255, 255));
        jLabelMode.setText("jLabel2");

        jLabelStatusLabel.setForeground(new java.awt.Color(255, 255, 255));
        jLabelStatusLabel.setText("Status:");

        jLabelStatus.setForeground(new java.awt.Color(255, 255, 255));
        jLabelStatus.setText("jLabel4");

        javax.swing.GroupLayout jPanelStatusLayout = new javax.swing.GroupLayout(jPanelStatus);
        jPanelStatus.setLayout(jPanelStatusLayout);
        jPanelStatusLayout.setHorizontalGroup(
            jPanelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStatusLayout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(jLabelModeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelMode, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelStatusLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(619, Short.MAX_VALUE))
        );
        jPanelStatusLayout.setVerticalGroup(
            jPanelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelStatusLayout.createSequentialGroup()
                .addContainerGap(14, Short.MAX_VALUE)
                .addGroup(jPanelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelModeLabel)
                    .addComponent(jLabelMode)
                    .addComponent(jLabelStatusLabel)
                    .addComponent(jLabelStatus))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanelTopBar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelStatus, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanelNavBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPaneBody)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelTopBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPaneBody)
                    .addComponent(jPanelNavBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonCityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCityActionPerformed
        topBarCityButtonClick(evt);
    }//GEN-LAST:event_jButtonCityActionPerformed

    private void jButtonSimulationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSimulationActionPerformed
        topBarSimulationButtonClick(evt);
    }//GEN-LAST:event_jButtonSimulationActionPerformed

    private void jButtonCreateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCreateActionPerformed
        navBarCreateButtonClick(evt);
    }//GEN-LAST:event_jButtonCreateActionPerformed

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveActionPerformed
        navBarSaveButtonClick(evt);
    }//GEN-LAST:event_jButtonSaveActionPerformed

    private void jButtonOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOpenActionPerformed
        navBarOpenButtonClick(evt);
    }//GEN-LAST:event_jButtonOpenActionPerformed

    private void jButtonRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRunActionPerformed
        navBarRunButtonClick(evt);
    }//GEN-LAST:event_jButtonRunActionPerformed

    private void jButtonStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStopActionPerformed
        navBarStopButtonClick(evt);
    }//GEN-LAST:event_jButtonStopActionPerformed

    private void jButtonStatisticsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStatisticsActionPerformed
        showStatistics();
    }//GEN-LAST:event_jButtonStatisticsActionPerformed

    private void jButtonPauseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPauseActionPerformed
        navBarPauseButtonClick(evt);
    }//GEN-LAST:event_jButtonPauseActionPerformed

    private void jButtonUndoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUndoActionPerformed
        cityPannel.undo();
    }//GEN-LAST:event_jButtonUndoActionPerformed

    private void jButtonRedoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRedoActionPerformed
        cityPannel.redo();
    }//GEN-LAST:event_jButtonRedoActionPerformed

    private void jComboBoxZoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxZoomActionPerformed
        int z=0;
        try{
            jComboBoxZoom.setSelectedItem("100");
            z=Integer.parseInt(jComboBoxZoom.getSelectedItem().toString());
        }catch(Exception ex){}
        if(z>0)
        cityPannel.setZoom(z);
    }//GEN-LAST:event_jComboBoxZoomActionPerformed

    private void jRadioButtonAddRoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonAddRoadActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButtonAddRoadActionPerformed
    
    private void formWindowClosing(java.awt.event.WindowEvent evt) {
        formClosing();
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
                /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainProgram.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainProgram.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainProgram.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainProgram.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainProgram().setVisible(true);
                
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCity;
    private javax.swing.JButton jButtonCreate;
    private javax.swing.JButton jButtonOpen;
    private javax.swing.JButton jButtonPause;
    private javax.swing.JButton jButtonRedo;
    private javax.swing.JButton jButtonRun;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JButton jButtonSimulation;
    private javax.swing.JButton jButtonStatistics;
    private javax.swing.JButton jButtonStop;
    private javax.swing.JButton jButtonUndo;
    private javax.swing.JComboBox<String> jComboBoxZoom;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelMode;
    private javax.swing.JLabel jLabelModeLabel;
    private javax.swing.JLabel jLabelStatus;
    private javax.swing.JLabel jLabelStatusLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelNavBar;
    private javax.swing.JPanel jPanelSpawn;
    private javax.swing.JPanel jPanelStatus;
    private javax.swing.JPanel jPanelTopBar;
    private javax.swing.JRadioButton jRadioButtonAddRoad;
    private javax.swing.JRadioButton jRadioButtonEditRoad;
    private javax.swing.JScrollPane jScrollPaneBody;
    private javax.swing.JSpinner jSpinnerSpawn;
    // End of variables declaration//GEN-END:variables
}
