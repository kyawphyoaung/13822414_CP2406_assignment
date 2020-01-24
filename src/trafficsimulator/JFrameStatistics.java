/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trafficsimulator;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.Timer;

public class JFrameStatistics extends javax.swing.JFrame {

    public JFrameStatistics(CityPannel cityPannel) {
        initComponents();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(new Point((screenSize.width-getWidth())/2, (screenSize.height-getHeight())/2));
        jLabelVhicleNum.setText("");
        jLabelLightNum.setText("");
        jLabelAvgSpeed.setText("");        
        new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                jLabelVhicleNum.setText(""+cityPannel.vecVhc.size());
                int lightNum=0;
                for (Vector<Light> vecLight : cityPannel.vecLights) {
                    for (Light light : vecLight) {
                        lightNum++;
                    }
                }
                jLabelLightNum.setText(""+lightNum +" ( flash :" + cityPannel.cntChangedLight + " )");
                float avgSpeed=0;
                double totalTime=0.0;
                double totalLength=0.0;
                avgSpeed=(float)Math.round(cityPannel.sumPath/cityPannel.sumTime*1000)/1000;
                for (Vehicle vhc : cityPannel.vecVhc) {
                    totalLength+=vhc.totalLenth;
                    totalTime+=vhc.totalTime;
                }
                if(totalTime>0.0)avgSpeed=(float)Math.round(totalLength/totalTime*1000)/1000;
                jLabelAvgSpeed.setText(""+avgSpeed+"km/h");
            }
        }).start();
    }

     //This method is called from within the constructor

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabelVhicleNum = new javax.swing.JLabel();
        jLabelLightNum = new javax.swing.JLabel();
        jLabelAvgSpeed = new javax.swing.JLabel();

        setTitle("Statistics");
        setAlwaysOnTop(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jLabel1.setText("Number of vehicles:");

        jLabel2.setText("Number of traffic lights:");

        jLabel3.setText("Average speed of vehicles:");

        jLabelVhicleNum.setText("jLabel4");

        jLabelLightNum.setText("jLabel5");

        jLabelAvgSpeed.setText("jLabel6");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addGap(45, 45, 45)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelAvgSpeed)
                    .addComponent(jLabelLightNum)
                    .addComponent(jLabelVhicleNum))
                .addContainerGap(212, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabelVhicleNum))
                .addGap(42, 42, 42)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabelLightNum))
                .addGap(38, 38, 38)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabelAvgSpeed))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {
        
    }

    // Variables declaration
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabelAvgSpeed;
    private javax.swing.JLabel jLabelLightNum;
    private javax.swing.JLabel jLabelVhicleNum;
    // End of variables declaration
}
