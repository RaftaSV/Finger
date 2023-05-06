package DB;


import java.sql.*;;
import javax.swing.JOptionPane;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Rafta
 */
public class conexion {
    
   
    Connection con = null;
    
  public  Connection conexion () {
    
            try {
                String user = "root";
                String pass ="root";
                String sURL = "jdbc:mysql://localhost:3306/unab?serverTimezone=UTC";
               con = DriverManager.getConnection(sURL,user,pass);
                System.out.println("Conexion exitosa");
            } catch (Exception e) {
                System.out.println("Error de conexion" + e);
                JOptionPane.showConfirmDialog(null, e);
            }
        return con;
    }
    
  public  Connection cerrarConexion () {
        try {
            System.out.println("Cerrando conexion");
        } catch (Exception e) {
              System.out.println("Error cerrando  conexion");
                JOptionPane.showConfirmDialog(null, e); 
        }
        con = null;
    return con;
    }
    
}
