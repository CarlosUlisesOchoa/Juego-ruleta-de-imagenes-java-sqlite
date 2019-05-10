/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package juego_imagenes;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import shane.Utils;
import shane.Dialogs;

/**
 *
 * @author Uli Gibson
 */
public class Principal extends javax.swing.JFrame {

    
    private int dinero_Usuario, dinero_Maquina, dinero_Apostado;
    
    public static int[] imgs_Seleccionadas = new int[]{0,0,0,0,0};
    
    public static int[] imgs_Generadas = new int[]{0,0,0};
    
    public static JLabel[] img_Principal;
    
    private List<JCheckBox> checkBox_Mini_Img;
    
    public static int tiempo_cambio = 100; // tiempo entre cambio de imagenes ruleta (milisegundos)
    
    Hilo_Img_1 h_1 = new Hilo_Img_1();
    Hilo_Img_2 h_2 = new Hilo_Img_2();
    Hilo_Img_3 h_3 = new Hilo_Img_3();
    
    private String estado_Juego;
    
    DB.DB my_DB = new DB.DB("Scores");
    
    /**
     * Creates new form Principal
     */
    public Principal() {
        initComponents();
        
        openAcerca();
        
        img_Principal = new JLabel[]{img_Principal_1, img_Principal_2, img_Principal_3};
        
        checkBox_Mini_Img = new ArrayList<>();
        for (Component C : Panel_Mini_Imgs.getComponents()) {
            if (C instanceof JCheckBox) {
                checkBox_Mini_Img.add(((JCheckBox) C));
            }
        }
        
        h_1.start();
        h_2.start();
        h_3.start();
        
        setEstadoHilos(false);
        
        lbl_AzarMouseClicked(null);
        lbl_Repetir_Juego.setVisible(false);
        
        DB_init();
    }
    
    private void DB_init()
    {
        if(my_DB.connect()) {
            setDinero("maquina", my_DB.getScore("maquina"));
            setDinero("usuario", my_DB.getScore("usuario"));
        }
    }
    
    private void setEstadoJuego(String estado) // Si el estado no es "activo" los hilos son suspendidos
    {
        if(estado.equals("activo")) {
            lbl_Estado.setText("Playing! ***");
            lbl_Estado.setForeground(new Color(67,169,225));
            setEstadoHilos(true);
            btn_Iniciar.setEnabled(false);
            btn_Apostar.setEnabled(false);
            btn_Terminar.setEnabled(true);
            btn_Cancelar.setEnabled(true);
        }
        if(estado.equals("listo")) {
            lbl_Estado.setText("Game is not started");
            lbl_Estado.setForeground(new Color(117, 117, 117));
            btn_Iniciar.setEnabled(true);
            btn_Cancelar.setEnabled(false);
            btn_Terminar.setEnabled(false);
            btn_Apostar.setEnabled(true);
            setCheckBoxes(true);
            setEstadoHilos(false);
            for (JLabel img : img_Principal) {
                img.setIcon(new ImageIcon(getClass().getResource("/img/incognito.jpg")));
            }
        }
        if(estado.equals("nuevo")) {
            lbl_Repetir_Juego.setVisible(false);
            lbl_Estado.setText("Game is not started");
            lbl_Estado.setForeground(new Color(117,117,117));
            btn_Iniciar.setEnabled(false);
            btn_Cancelar.setEnabled(false);
            btn_Terminar.setEnabled(false);
            btn_Apostar.setEnabled(true);
            btn_Cancelar.setText("Cancel");
            setDinero("apostado", 0);
            setEstadoHilos(false);
            for(JLabel img : img_Principal) {
                img.setIcon(new ImageIcon(getClass().getResource("/img/incognito.jpg")));
            }
            for (Component C : Panel_Mini_Imgs.getComponents()) {
                if (C instanceof JCheckBox) {

                    if (((JCheckBox) C).isSelected()) {
                        ((JCheckBox) C).setSelected(false);
                    }
                }
            }
            setCheckBoxes(true);
            lbl_AzarMouseClicked(null);
        }
        if(estado.equals("ganado")) {
            lbl_Estado.setText("YOU WON !!");
            lbl_Estado.setForeground(new Color(47,173,66));
            setDinero("usuario", dinero_Usuario+dinero_Apostado);
            setDinero("maquina", dinero_Maquina-dinero_Apostado);
            btn_Terminar.setEnabled(false);
            btn_Cancelar.setText("New game");
            lbl_Repetir_Juego.setVisible(true);
        }
        if(estado.equals("perdido")) {
            lbl_Estado.setText("YOU LOST !!");
            lbl_Estado.setForeground(Color.red);
            setDinero("usuario", dinero_Usuario-dinero_Apostado);
            setDinero("maquina", dinero_Maquina+dinero_Apostado);
            btn_Terminar.setEnabled(false);
            btn_Cancelar.setText("New game");
            if(dinero_Usuario >= dinero_Apostado) lbl_Repetir_Juego.setVisible(true);
        }
        estado_Juego = estado;
    }
    
    private String getEstadoJuego()
    {
        return estado_Juego;
    }
    
    private void setEstadoHilos(boolean estado)
    {
        if(estado) {
            h_1.resume();
            h_2.resume();
            h_3.resume();
        } else {
            h_1.suspend();
            h_2.suspend();
            h_3.suspend();
        }
    }
    
    private void modoHack()
    {
        int rand = Utils.randomEx(0, Principal.imgs_Seleccionadas.length-1);
        
        for(int i = 0; i < imgs_Generadas.length; i++) {
            imgs_Generadas[i] = imgs_Seleccionadas[rand];
            img_Principal[i].setIcon(new ImageIcon(getClass().getResource("/img/"+imgs_Generadas[i]+".jpg")));
        }
        
        
    }
    
    private void verificarGanador() 
    {
        if(contarImgRepetidas() == 3) {
            setEstadoJuego("ganado");
        } else {
            setEstadoJuego("perdido");
        }
    }
    
    private int contarImgRepetidas() {
        int res = 0;
        for (int i = 0; i < imgs_Generadas.length; i++) {
            for (int j = i + 1; j < imgs_Generadas.length; j++) {
                if (imgs_Generadas[i] == imgs_Generadas[j]) {
                    res++;
                }
            }
        }
        return res;
    }
    
    /*private void corregirImgRepetidas()
    {
        while(Utils.intArrayHasDuplicates(imgs_Generadas)) {
            for(int i = 0; i < imgs_Generadas.length; i++)  {
                for(int j = i+1; j < imgs_Generadas.length; j++) {
                    if(imgs_Generadas[i] == imgs_Generadas[j]) {
                        imgs_Generadas[i] = (imgs_Generadas[i] > 0 ? imgs_Generadas[i]-1 : imgs_Generadas[i]+1);
                        img_Principal[i].setIcon(new ImageIcon(getClass().getResource("/img/"+imgs_Generadas[i]+".jpg")));
                    }
                }
            }
        }
        
    }*/
    
    private void setDinero(String who, int amount)
    {
        if(who.equals("maquina"))
        {
            dinero_Maquina = amount;
            lbl_Dinero_Maquina.setText(String.format("$%d.00", amount));
            
        }
        if(who.equals("usuario"))
        {
            dinero_Usuario = amount;
            lbl_Dinero_Usuario.setText(String.format("$%d.00", amount));
        }
        if(who.equals("apostado"))
        {
            dinero_Apostado = amount;
            lbl_Dinero_Apostado.setText(String.format("$%d.00", amount));
            if(dinero_Apostado > 0) {
                btn_Iniciar.setEnabled(true);
                setCheckBoxes(false);
                btn_Cancelar.setEnabled(true);
            }
        }
    }
    
    private void setCheckBoxes(boolean state)
    {
        for(JCheckBox c : checkBox_Mini_Img) {
            c.setEnabled(state);
        }
        lbl_Azar.setEnabled(state);
        lbl_Deseleccionar.setEnabled(state);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Panel_Principal_Imgs = new javax.swing.JPanel();
        img_Principal_2 = new javax.swing.JLabel();
        img_Principal_3 = new javax.swing.JLabel();
        img_Principal_1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        btn_Apostar = new javax.swing.JButton();
        btn_Iniciar = new javax.swing.JButton();
        btn_Cancelar = new javax.swing.JButton();
        btn_Terminar = new javax.swing.JButton();
        lbl_Repetir_Juego = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        Panel_Mini_Imgs = new javax.swing.JPanel();
        img_Mini_1 = new javax.swing.JLabel();
        checkBox_1 = new javax.swing.JCheckBox();
        checkBox_2 = new javax.swing.JCheckBox();
        img_Mini_2 = new javax.swing.JLabel();
        checkBox_3 = new javax.swing.JCheckBox();
        img_Mini_3 = new javax.swing.JLabel();
        checkBox_4 = new javax.swing.JCheckBox();
        img_Mini_4 = new javax.swing.JLabel();
        checkBox_5 = new javax.swing.JCheckBox();
        img_Mini_5 = new javax.swing.JLabel();
        checkBox_6 = new javax.swing.JCheckBox();
        img_Mini_6 = new javax.swing.JLabel();
        checkBox_7 = new javax.swing.JCheckBox();
        img_Mini_7 = new javax.swing.JLabel();
        checkBox_8 = new javax.swing.JCheckBox();
        img_Mini_8 = new javax.swing.JLabel();
        checkBox_9 = new javax.swing.JCheckBox();
        img_Mini_9 = new javax.swing.JLabel();
        checkBox_10 = new javax.swing.JCheckBox();
        img_Mini_10 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        lbl_Estado = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        lbl_Dinero_Maquina = new javax.swing.JLabel();
        lbl_Dinero_Apostado = new javax.swing.JLabel();
        lbl_Dinero_Usuario = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        checkBox_Hack = new javax.swing.JCheckBox();
        lbl_Azar = new javax.swing.JLabel();
        lbl_Deseleccionar = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        Panel_Principal_Imgs.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        img_Principal_2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/incognito.jpg"))); // NOI18N

        img_Principal_3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/incognito.jpg"))); // NOI18N

        img_Principal_1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/incognito.jpg"))); // NOI18N

        javax.swing.GroupLayout Panel_Principal_ImgsLayout = new javax.swing.GroupLayout(Panel_Principal_Imgs);
        Panel_Principal_Imgs.setLayout(Panel_Principal_ImgsLayout);
        Panel_Principal_ImgsLayout.setHorizontalGroup(
            Panel_Principal_ImgsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, Panel_Principal_ImgsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(img_Principal_1)
                .addGap(18, 18, 18)
                .addComponent(img_Principal_2)
                .addGap(18, 18, 18)
                .addComponent(img_Principal_3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        Panel_Principal_ImgsLayout.setVerticalGroup(
            Panel_Principal_ImgsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Panel_Principal_ImgsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(Panel_Principal_ImgsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(img_Principal_3)
                    .addComponent(img_Principal_2)
                    .addComponent(img_Principal_1))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        btn_Apostar.setText("Bet");
        btn_Apostar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_ApostarActionPerformed(evt);
            }
        });

        btn_Iniciar.setText("Start");
        btn_Iniciar.setEnabled(false);
        btn_Iniciar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_IniciarActionPerformed(evt);
            }
        });

        btn_Cancelar.setText("Cancel");
        btn_Cancelar.setEnabled(false);
        btn_Cancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_CancelarActionPerformed(evt);
            }
        });

        btn_Terminar.setText("Finish");
        btn_Terminar.setEnabled(false);
        btn_Terminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_TerminarActionPerformed(evt);
            }
        });

        lbl_Repetir_Juego.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lbl_Repetir_Juego.setForeground(new java.awt.Color(0, 153, 255));
        lbl_Repetir_Juego.setText("Play again");
        lbl_Repetir_Juego.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lbl_Repetir_Juego.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbl_Repetir_JuegoMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btn_Apostar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btn_Iniciar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lbl_Repetir_Juego)
                .addGap(99, 99, 99)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btn_Cancelar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btn_Terminar, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(btn_Cancelar))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btn_Apostar)
                        .addGap(32, 32, 32)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btn_Iniciar)
                            .addComponent(btn_Terminar)
                            .addComponent(lbl_Repetir_Juego))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        Panel_Mini_Imgs.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        img_Mini_1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/1_mini.jpg"))); // NOI18N
        img_Mini_1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        img_Mini_1.setName("1"); // NOI18N
        img_Mini_1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                img_Mini_1MouseClicked(evt);
            }
        });

        checkBox_1.setName("1"); // NOI18N
        checkBox_1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                checkBox_2ItemStateChanged(evt);
            }
        });

        checkBox_2.setName("2"); // NOI18N
        checkBox_2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                checkBox_2ItemStateChanged(evt);
            }
        });

        img_Mini_2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/2_mini.jpg"))); // NOI18N
        img_Mini_2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        img_Mini_2.setName("2"); // NOI18N
        img_Mini_2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                img_Mini_1MouseClicked(evt);
            }
        });

        checkBox_3.setName("3"); // NOI18N
        checkBox_3.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                checkBox_2ItemStateChanged(evt);
            }
        });

        img_Mini_3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/3_mini.jpg"))); // NOI18N
        img_Mini_3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        img_Mini_3.setName("3"); // NOI18N
        img_Mini_3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                img_Mini_1MouseClicked(evt);
            }
        });

        checkBox_4.setName("4"); // NOI18N
        checkBox_4.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                checkBox_2ItemStateChanged(evt);
            }
        });

        img_Mini_4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/4_mini.jpg"))); // NOI18N
        img_Mini_4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        img_Mini_4.setName("4"); // NOI18N
        img_Mini_4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                img_Mini_1MouseClicked(evt);
            }
        });

        checkBox_5.setName("5"); // NOI18N
        checkBox_5.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                checkBox_2ItemStateChanged(evt);
            }
        });

        img_Mini_5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/5_mini.jpg"))); // NOI18N
        img_Mini_5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        img_Mini_5.setName("5"); // NOI18N
        img_Mini_5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                img_Mini_1MouseClicked(evt);
            }
        });

        checkBox_6.setName("6"); // NOI18N
        checkBox_6.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                checkBox_2ItemStateChanged(evt);
            }
        });

        img_Mini_6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/6_mini.jpg"))); // NOI18N
        img_Mini_6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        img_Mini_6.setName("6"); // NOI18N
        img_Mini_6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                img_Mini_1MouseClicked(evt);
            }
        });

        checkBox_7.setName("7"); // NOI18N
        checkBox_7.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                checkBox_2ItemStateChanged(evt);
            }
        });

        img_Mini_7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/7_mini.jpg"))); // NOI18N
        img_Mini_7.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        img_Mini_7.setName("7"); // NOI18N
        img_Mini_7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                img_Mini_1MouseClicked(evt);
            }
        });

        checkBox_8.setName("8"); // NOI18N
        checkBox_8.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                checkBox_2ItemStateChanged(evt);
            }
        });

        img_Mini_8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/8_mini.jpg"))); // NOI18N
        img_Mini_8.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        img_Mini_8.setName("8"); // NOI18N
        img_Mini_8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                img_Mini_1MouseClicked(evt);
            }
        });

        checkBox_9.setName("9"); // NOI18N
        checkBox_9.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                checkBox_2ItemStateChanged(evt);
            }
        });

        img_Mini_9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/9_mini.jpg"))); // NOI18N
        img_Mini_9.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        img_Mini_9.setName("9"); // NOI18N
        img_Mini_9.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                img_Mini_1MouseClicked(evt);
            }
        });

        checkBox_10.setName("10"); // NOI18N
        checkBox_10.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                checkBox_2ItemStateChanged(evt);
            }
        });

        img_Mini_10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/10_mini.jpg"))); // NOI18N
        img_Mini_10.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        img_Mini_10.setName("10"); // NOI18N
        img_Mini_10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                img_Mini_1MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout Panel_Mini_ImgsLayout = new javax.swing.GroupLayout(Panel_Mini_Imgs);
        Panel_Mini_Imgs.setLayout(Panel_Mini_ImgsLayout);
        Panel_Mini_ImgsLayout.setHorizontalGroup(
            Panel_Mini_ImgsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Panel_Mini_ImgsLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(Panel_Mini_ImgsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, Panel_Mini_ImgsLayout.createSequentialGroup()
                        .addComponent(checkBox_5)
                        .addGap(11, 11, 11)
                        .addComponent(img_Mini_5))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, Panel_Mini_ImgsLayout.createSequentialGroup()
                        .addComponent(checkBox_3)
                        .addGap(11, 11, 11)
                        .addComponent(img_Mini_3))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, Panel_Mini_ImgsLayout.createSequentialGroup()
                        .addComponent(checkBox_2)
                        .addGap(11, 11, 11)
                        .addComponent(img_Mini_2))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, Panel_Mini_ImgsLayout.createSequentialGroup()
                        .addComponent(checkBox_1)
                        .addGap(11, 11, 11)
                        .addComponent(img_Mini_1))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, Panel_Mini_ImgsLayout.createSequentialGroup()
                        .addComponent(checkBox_4)
                        .addGap(11, 11, 11)
                        .addComponent(img_Mini_4))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, Panel_Mini_ImgsLayout.createSequentialGroup()
                        .addComponent(checkBox_10)
                        .addGap(11, 11, 11)
                        .addComponent(img_Mini_10))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, Panel_Mini_ImgsLayout.createSequentialGroup()
                        .addComponent(checkBox_8)
                        .addGap(11, 11, 11)
                        .addComponent(img_Mini_8))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, Panel_Mini_ImgsLayout.createSequentialGroup()
                        .addComponent(checkBox_7)
                        .addGap(11, 11, 11)
                        .addComponent(img_Mini_7))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, Panel_Mini_ImgsLayout.createSequentialGroup()
                        .addComponent(checkBox_6)
                        .addGap(11, 11, 11)
                        .addComponent(img_Mini_6))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, Panel_Mini_ImgsLayout.createSequentialGroup()
                        .addComponent(checkBox_9)
                        .addGap(11, 11, 11)
                        .addComponent(img_Mini_9)))
                .addContainerGap())
        );
        Panel_Mini_ImgsLayout.setVerticalGroup(
            Panel_Mini_ImgsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Panel_Mini_ImgsLayout.createSequentialGroup()
                .addGroup(Panel_Mini_ImgsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(Panel_Mini_ImgsLayout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(checkBox_1))
                    .addGroup(Panel_Mini_ImgsLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(img_Mini_1)))
                .addGap(29, 29, 29)
                .addGroup(Panel_Mini_ImgsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(Panel_Mini_ImgsLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(checkBox_2))
                    .addComponent(img_Mini_2))
                .addGap(29, 29, 29)
                .addGroup(Panel_Mini_ImgsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(Panel_Mini_ImgsLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(checkBox_3))
                    .addComponent(img_Mini_3))
                .addGap(27, 27, 27)
                .addGroup(Panel_Mini_ImgsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(Panel_Mini_ImgsLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(checkBox_4))
                    .addComponent(img_Mini_4))
                .addGap(28, 28, 28)
                .addGroup(Panel_Mini_ImgsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(Panel_Mini_ImgsLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(checkBox_5))
                    .addComponent(img_Mini_5))
                .addGap(27, 27, 27)
                .addGroup(Panel_Mini_ImgsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(Panel_Mini_ImgsLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(checkBox_6))
                    .addComponent(img_Mini_6))
                .addGap(29, 29, 29)
                .addGroup(Panel_Mini_ImgsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(Panel_Mini_ImgsLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(checkBox_7))
                    .addComponent(img_Mini_7))
                .addGap(29, 29, 29)
                .addGroup(Panel_Mini_ImgsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(Panel_Mini_ImgsLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(checkBox_8))
                    .addComponent(img_Mini_8))
                .addGap(27, 27, 27)
                .addGroup(Panel_Mini_ImgsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(Panel_Mini_ImgsLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(checkBox_9))
                    .addComponent(img_Mini_9))
                .addGap(28, 28, 28)
                .addGroup(Panel_Mini_ImgsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(Panel_Mini_ImgsLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(checkBox_10))
                    .addComponent(img_Mini_10))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(Panel_Mini_Imgs);

        jLabel1.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        jLabel1.setText("STATUS:");

        lbl_Estado.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
        lbl_Estado.setForeground(new java.awt.Color(117, 117, 117));
        lbl_Estado.setText("Game is not started");

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/titulo.png"))); // NOI18N

        jLabel5.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
        jLabel5.setText("BETTING:");

        jLabel6.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel6.setText("Machine Cash:");

        lbl_Dinero_Maquina.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        lbl_Dinero_Maquina.setForeground(new java.awt.Color(76, 177, 106));
        lbl_Dinero_Maquina.setText("$0.00");

        lbl_Dinero_Apostado.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        lbl_Dinero_Apostado.setForeground(new java.awt.Color(76, 177, 106));
        lbl_Dinero_Apostado.setText("$0.00");
        lbl_Dinero_Apostado.setToolTipText("");

        lbl_Dinero_Usuario.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        lbl_Dinero_Usuario.setForeground(new java.awt.Color(76, 177, 106));
        lbl_Dinero_Usuario.setText("$0.00");

        jLabel10.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel10.setText("Your Money:");

        checkBox_Hack.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        checkBox_Hack.setText("HACK MODE");

        lbl_Azar.setForeground(new java.awt.Color(51, 153, 255));
        lbl_Azar.setText("<html><p><u>Random select</u></p></html>");
        lbl_Azar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lbl_Azar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbl_AzarMouseClicked(evt);
            }
        });

        lbl_Deseleccionar.setForeground(new java.awt.Color(51, 153, 255));
        lbl_Deseleccionar.setText("<html><p><u>Deselect all</u></p></html>");
        lbl_Deseleccionar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lbl_Deseleccionar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbl_DeseleccionarMouseClicked(evt);
            }
        });

        jMenuBar1.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N

        jMenu2.setText("About");
        jMenu2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jMenu2MouseClicked(evt);
            }
        });
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lbl_Dinero_Maquina)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 189, Short.MAX_VALUE)
                        .addComponent(checkBox_Hack)
                        .addGap(123, 123, 123)
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lbl_Dinero_Usuario))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(27, 27, 27))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(Panel_Principal_Imgs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(155, 155, 155)
                                        .addComponent(jLabel1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(lbl_Estado))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel5)
                                        .addGap(18, 18, 18)
                                        .addComponent(lbl_Dinero_Apostado)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addComponent(lbl_Deseleccionar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(29, 29, 29)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(lbl_Azar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(23, 23, 23)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lbl_Azar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lbl_Deseleccionar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(lbl_Estado))
                        .addGap(26, 26, 26)
                        .addComponent(Panel_Principal_Imgs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(45, 45, 45)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(lbl_Dinero_Apostado))
                        .addGap(56, 56, 56)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(lbl_Dinero_Maquina)
                    .addComponent(jLabel10)
                    .addComponent(lbl_Dinero_Usuario)
                    .addComponent(checkBox_Hack))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_ApostarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_ApostarActionPerformed
        // TODO add your handling code here:
        if (imgs_Seleccionadas[0] > 0 && imgs_Seleccionadas[1] > 0
            && imgs_Seleccionadas[2] > 0) { // Si ya seleccionó las 3 imagenes
        
            if(dinero_Usuario > 0) {
            
                Integer apuesta = Dialogs.getInt(String.format
                    (
                        "How much you want to bet?\n\n"
                      + "You have $%d.00\n\n ", dinero_Usuario
                    )
                );

                if(apuesta != null) {
                    if(apuesta <= dinero_Usuario) {
                        if(apuesta > 0) {
                            if(apuesta <= dinero_Maquina) {
                                setDinero("apostado", apuesta); 
                            } else {
                                if(dinero_Maquina > 0) {
                                    Dialogs.ErrorMsg(String.format
                                        (
                                            "Sorry I don't own that money :( \n\n"
                                          + "I just have $%d.00\n\n ", dinero_Maquina
                                        )
                                    );
                                } else {
                                    Dialogs.ErrorMsg("Machine doesn't have money...");
                                }
                            }
                        } else {
                            Dialogs.ErrorMsg("Don't waste my time dude, you gotta bet!...");
                        }
                    } else {
                        Dialogs.ErrorMsg(String.format
                            (
                                "You only own $%d.00\n\n"
                              + "You need aditional $%d.00, if you wanna bet $%d.00\n\n ", dinero_Usuario, (apuesta-dinero_Usuario), apuesta
                            )
                        );
                    }
                } else {
                    Dialogs.ErrorMsg("Check entered amount...\n\n ");
                }
            } else {
                Dialogs.InfoMsg("You don't have money dude");
            }
        } else {
            Dialogs.InfoMsg("First you need to select your images");
        }
    }//GEN-LAST:event_btn_ApostarActionPerformed

    private void checkBox_2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_checkBox_2ItemStateChanged
        // TODO add your handling code here:
        int contador = 0;
        for(JCheckBox c : checkBox_Mini_Img) {
            if(c.isSelected()) {
                if(contador < imgs_Seleccionadas.length) imgs_Seleccionadas[contador] = Integer.valueOf(c.getName());
                contador++;
            }
        }
        
        if(contador > imgs_Seleccionadas.length) {
            Dialogs.InfoMsg("You cannot select more images, max is "+imgs_Seleccionadas.length);
            
        }
        
        
    }//GEN-LAST:event_checkBox_2ItemStateChanged

    private void btn_IniciarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_IniciarActionPerformed
        // TODO add your handling code here:

        setEstadoJuego("activo");
    }//GEN-LAST:event_btn_IniciarActionPerformed

    private void btn_TerminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_TerminarActionPerformed
        // TODO add your handling code here:
        setEstadoHilos(false);
        //corregirImgRepetidas();
        if(checkBox_Hack.isSelected()) {
            modoHack();
        }
        verificarGanador();
    }//GEN-LAST:event_btn_TerminarActionPerformed

    private void btn_CancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_CancelarActionPerformed
        // TODO add your handling code here:

        setEstadoJuego("nuevo");

    }//GEN-LAST:event_btn_CancelarActionPerformed

    private void img_Mini_1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_img_Mini_1MouseClicked
        // TODO add your handling code here:
        
    }//GEN-LAST:event_img_Mini_1MouseClicked

    private void lbl_AzarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_AzarMouseClicked
        // TODO add your handling code here:
        if(!lbl_Azar.isEnabled()) return;
        
        int azar = 0;
        
        deselectJCheckBox(checkBox_Mini_Img);
        
        while (getCheckedBoxes() != imgs_Seleccionadas.length) {

            azar = Utils.randomEx(0, checkBox_Mini_Img.size()-1);
            checkBox_Mini_Img.get(azar).setSelected(true);
        }
    }//GEN-LAST:event_lbl_AzarMouseClicked

    private void lbl_DeseleccionarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_DeseleccionarMouseClicked
        // TODO add your handling code here:
        if(!lbl_Deseleccionar.isEnabled()) return;
        deselectJCheckBox(checkBox_Mini_Img);
    }//GEN-LAST:event_lbl_DeseleccionarMouseClicked

    private void lbl_Repetir_JuegoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_Repetir_JuegoMouseClicked
        // TODO add your handling code here:
        setEstadoJuego("activo");
        btn_Cancelar.setText("Cancel");
        lbl_Repetir_Juego.setVisible(false);
    }//GEN-LAST:event_lbl_Repetir_JuegoMouseClicked

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        my_DB.setScore("maquina", dinero_Maquina);
        my_DB.setScore("usuario", dinero_Usuario);
        my_DB.close_conection();
        System.exit(0);
    }//GEN-LAST:event_formWindowClosing

    private void jMenu2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenu2MouseClicked
        // TODO add your handling code here:
        openAcerca();
    }//GEN-LAST:event_jMenu2MouseClicked

    private void openAcerca() {
        AcercaDe vA = new AcercaDe();
        vA.setVisible(true);
        vA.setLocationRelativeTo(null);
    }
    
    private void deselectJCheckBox(List<JCheckBox> listaCheck)
    {
        for (JCheckBox c : listaCheck) {
            if (c.isSelected()) {
                c.setSelected(false);
            }
        }
        Arrays.fill(imgs_Seleccionadas, 0);
    }
    private int getCheckedBoxes()
    {
        int res = 0;
        for(JCheckBox c : checkBox_Mini_Img) {
            if(c.isSelected()) {
                res++;
            }
        }
        return res;
    }
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Panel_Mini_Imgs;
    private javax.swing.JPanel Panel_Principal_Imgs;
    private javax.swing.JButton btn_Apostar;
    private javax.swing.JButton btn_Cancelar;
    private javax.swing.JButton btn_Iniciar;
    private javax.swing.JButton btn_Terminar;
    private javax.swing.JCheckBox checkBox_1;
    public static javax.swing.JCheckBox checkBox_10;
    private javax.swing.JCheckBox checkBox_2;
    private javax.swing.JCheckBox checkBox_3;
    private javax.swing.JCheckBox checkBox_4;
    private javax.swing.JCheckBox checkBox_5;
    private javax.swing.JCheckBox checkBox_6;
    private javax.swing.JCheckBox checkBox_7;
    private javax.swing.JCheckBox checkBox_8;
    private javax.swing.JCheckBox checkBox_9;
    private javax.swing.JCheckBox checkBox_Hack;
    private javax.swing.JLabel img_Mini_1;
    private javax.swing.JLabel img_Mini_10;
    private javax.swing.JLabel img_Mini_2;
    private javax.swing.JLabel img_Mini_3;
    private javax.swing.JLabel img_Mini_4;
    private javax.swing.JLabel img_Mini_5;
    private javax.swing.JLabel img_Mini_6;
    private javax.swing.JLabel img_Mini_7;
    private javax.swing.JLabel img_Mini_8;
    private javax.swing.JLabel img_Mini_9;
    public static javax.swing.JLabel img_Principal_1;
    public static javax.swing.JLabel img_Principal_2;
    public static javax.swing.JLabel img_Principal_3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbl_Azar;
    private javax.swing.JLabel lbl_Deseleccionar;
    private javax.swing.JLabel lbl_Dinero_Apostado;
    private javax.swing.JLabel lbl_Dinero_Maquina;
    private javax.swing.JLabel lbl_Dinero_Usuario;
    private javax.swing.JLabel lbl_Estado;
    private javax.swing.JLabel lbl_Repetir_Juego;
    // End of variables declaration//GEN-END:variables
}
