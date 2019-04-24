/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package juego_imagenes;

import javax.swing.ImageIcon;
import shane.Utils;

/**
 *
 * @author Uli Gibson
 */
public class Hilo_Img_1 extends Thread {

    public Hilo_Img_1() {
    }
    
    @Override
    public synchronized void run()
    {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                
                doWork();
                
                wait(Principal.tiempo_cambio);
                
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    int rnd = 0, ant_rnd = -1;
    private void doWork() {
        do {
            ant_rnd = rnd;
            rnd = Utils.randomEx(0, Principal.imgs_Seleccionadas.length-1);
            Principal.img_Principal[0].setIcon(new ImageIcon(getClass().getResource("/img/"+Principal.imgs_Seleccionadas[rnd]+".jpg")));
        } while(ant_rnd == rnd);
        Principal.imgs_Generadas[0] = Principal.imgs_Seleccionadas[rnd];
    }
    
}
