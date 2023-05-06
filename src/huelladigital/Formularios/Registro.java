/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package huelladigital.Formularios;

import DB.conexion;
import com.digitalpersona.onetouch.DPFPDataPurpose;
import com.digitalpersona.onetouch.DPFPFeatureSet;
import com.digitalpersona.onetouch.DPFPGlobal;
import com.digitalpersona.onetouch.DPFPSample;
import com.digitalpersona.onetouch.DPFPTemplate;
import com.digitalpersona.onetouch.capture.DPFPCapture;
import com.digitalpersona.onetouch.capture.event.DPFPDataAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPDataEvent;
import com.digitalpersona.onetouch.capture.event.DPFPErrorAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPErrorEvent;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusEvent;
import com.digitalpersona.onetouch.capture.event.DPFPSensorAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPSensorEvent;
import com.digitalpersona.onetouch.processing.DPFPEnrollment;
import com.digitalpersona.onetouch.processing.DPFPFeatureExtraction;
import com.digitalpersona.onetouch.processing.DPFPImageQualityException;
import com.digitalpersona.onetouch.verification.DPFPVerification;
import com.mysql.cj.jdbc.CallableStatement;
import java.awt.Image;
import java.awt.image.ImageProducer;
import java.io.ByteArrayInputStream;
import java.sql.*;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author Rafta
 */
public class Registro extends javax.swing.JFrame {

    /**
     * Creates new form Principal
     */
     private   DPFPCapture lector = DPFPGlobal.getCaptureFactory().createCapture();
     private   DPFPEnrollment Reclutador = DPFPGlobal.getEnrollmentFactory().createEnrollment(); // almacena la platilla de la huella
     private   DPFPVerification verificador = DPFPGlobal.getVerificationFactory().createVerification();
     
     private DPFPTemplate template;
     public static String TEMPLATE_PROPERTY = "template";
     
     public DPFPFeatureSet featuresincripcion;
     public DPFPFeatureSet featuresverification;
     
     conexion conectarbd;
     conexion desconectarbd;
     CallableStatement cst;
     ResultSet resultado;
     
     conexion con = new conexion();
     
     
     public void EnviarTexto (String string){
         txtInfo.append(string + "\n");
     }
     
     public DPFPFeatureSet extraerCaracteristicas(DPFPSample sample, DPFPDataPurpose purpose){
         DPFPFeatureExtraction extractor = DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();
         try {
             return extractor.createFeatureSet(sample, purpose);
         } catch (DPFPImageQualityException e) {
              return null;
         }
     }
     
     public Image crearImagenHuella (DPFPSample sample){
         return DPFPGlobal.getSampleConversionFactory().createImage(sample);
     }
     
    
     public void EstadoHuellas() {
     EnviarTexto("Muestra de huellas necesarias para guardar Template" + Reclutador.getFeaturesNeeded());
     }
      public void dibujarHuella (Image image){
         lblHuella.setIcon(new ImageIcon(
                 image.getScaledInstance(lblHuella.getWidth(), lblHuella.getHeight(), Image.SCALE_DEFAULT)
         ));
         repaint();
     }
     
     public void setTemplate(DPFPTemplate template){
         DPFPTemplate odl = this.template;
         this.template = template;
         firePropertyChange(TEMPLATE_PROPERTY, odl, template);
     }
     
     public void stop () {
         lector.stopCapture();
         EnviarTexto("No se esta usando el lector de huella");
     }
     
     public void start(){
         lector.startCapture();
         EnviarTexto("Utilizando el lector de huella");
     }
     
   protected void iniciar() {
    lector.addDataListener(new DPFPDataAdapter() {
        @Override
        public void dataAcquired(final DPFPDataEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    EnviarTexto("La huella digital ha sido capturada");
                    ProcesarCaptura(e.getSample());
                }
            });
        }
    });
    lector.addReaderStatusListener(new DPFPReaderStatusAdapter() {
        @Override
        public void readerConnected(final DPFPReaderStatusEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    EnviarTexto("El sensor de huella digital esta conectado o activado");
                }
            });
        }

        @Override
        public void readerDisconnected(final DPFPReaderStatusEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    EnviarTexto("El swnsor de huella digital esta desactivado o no conectado");
                }
            });
        }
    });
    lector.addSensorListener(new DPFPSensorAdapter() {
        @Override
        public void fingerTouched(final DPFPSensorEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    EnviarTexto("El dedo ha sido colocado en el sensor de huella");
                }
            });
        }
         @Override
    public void fingerGone( final DPFPSensorEvent e){
           SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                    EnviarTexto("El dedo ha sido Quitado del sensor de huella");
                }
           });
    }
   
    });
    lector.addErrorListener(new DPFPErrorAdapter(){
      public void errorReader(final DPFPErrorEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
        public void run (){
            EnviarTexto("Error " + e.getError());
        }
        });
      }
    });
   } // cierre de la función iniciar()

   public void ProcesarCaptura(DPFPSample sample){
       
    featuresincripcion = extraerCaracteristicas(sample, DPFPDataPurpose.DATA_PURPOSE_ENROLLMENT);
    featuresverification = extraerCaracteristicas(sample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);
    
    if (featuresincripcion != null){
        try {
            System.out.println("Las caracteristicas de la huella ha sido creada");
            Reclutador.addFeatures(featuresincripcion);
            Image image = crearImagenHuella(sample);
            dibujarHuella(image);
        } catch (DPFPImageQualityException ex){
            System.err.println("Error" + ex.getMessage());
        } finally {
            EstadoHuellas();
            
            switch (Reclutador.getTemplateStatus()) {
                case TEMPLATE_STATUS_READY:
                    stop();
                    setTemplate(Reclutador.getTemplate());
                    EnviarTexto("La plantilla de la huella ha sido creada, ya puede verificarla o Identificarla");
                    btnGuardar.setEnabled(true);
                    btnGuardar.grabFocus();
                    break;
                    
                case TEMPLATE_STATUS_FAILED: // informe de fallas
                    Reclutador.clear();
                    stop();
                    EstadoHuellas();
                    setTemplate(null);
                    JOptionPane.showMessageDialog(Registro.this, template, "La plantilla de la huella no pudo ser creada",  JOptionPane.ERROR_MESSAGE);
                    start();
                    break;
            }
        }
    }
    
   }
        
   public boolean guardarHuella(){
         Connection conectarBD = con.conexion();
         ByteArrayInputStream datoHuella = new ByteArrayInputStream(template.serialize());
         Integer tamañoHuella = template.serialize().length;
         
         String nombre = JOptionPane.showInputDialog("Nombre");
         try{
             
             PreparedStatement guardarStm = conectarBD.prepareStatement("Insert into admin(huella, nombrePersona) values (?,?)");
             guardarStm.setBinaryStream(1, datoHuella,tamañoHuella);
             guardarStm.setString(2, nombre);
             
             JOptionPane.showMessageDialog(null, "Huella Guardada con exito");
             conectarBD.setAutoCommit(false);
             guardarStm.executeUpdate();
             conectarBD.commit();
             return true;
         }catch (Exception ex) {
             JOptionPane.showMessageDialog(null, ex.toString());
         }
      return false;
   }
        
    public Registro() {
        initComponents();
       
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnGuardar = new javax.swing.JButton();
        lblHuella = new javax.swing.JLabel();
        lblHuellaInfo = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtInfo = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        btnGuardar.setText("Guardar");
        btnGuardar.setToolTipText("");
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });

        lblHuella.setBackground(new java.awt.Color(102, 255, 51));

        lblHuellaInfo.setText("Huella");

        txtInfo.setColumns(20);
        txtInfo.setRows(5);
        jScrollPane1.setViewportView(txtInfo);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblHuellaInfo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnGuardar))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 13, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblHuella, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE))))
                .addGap(19, 19, 19))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnGuardar))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(lblHuellaInfo)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 59, Short.MAX_VALUE)
                .addComponent(lblHuella, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31))
        );

        btnGuardar.getAccessibleContext().setAccessibleName("btnGuardar");
        lblHuella.getAccessibleContext().setAccessibleName("lblHuella");
        lblHuellaInfo.getAccessibleContext().setAccessibleName("jLabelHuella");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        guardarHuella();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        // TODO add your handling code here:
        iniciar();
        start();
        EstadoHuellas();
    }//GEN-LAST:event_formWindowOpened

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
            java.util.logging.Logger.getLogger(Registro.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Registro.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Registro.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Registro.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Registro().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGuardar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblHuella;
    private javax.swing.JLabel lblHuellaInfo;
    private javax.swing.JTextArea txtInfo;
    // End of variables declaration//GEN-END:variables
}
