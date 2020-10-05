package rl;

import java.io.File;
import java.io.FileWriter;

public class TraitementDonnee {
	/*
	 * Ecrit dans un fichier les donnees des iterations du Pac-Man
	 */
	public static void impressionClasse(int world_age,int eaten,int good,int stuck) {
		try{
			File ff=new File("donnee1.txt");
			FileWriter ffw=new FileWriter(ff,true);
			ffw.write(Integer.toString(world_age));
			ffw.write ("\t");
			ffw.write(Integer.toString(eaten));
			ffw.write ("\t");
			ffw.write(Integer.toString(good));
			ffw.write ("\t");
			ffw.write(Integer.toString(stuck));
			ffw.write ("\t");
			ffw.write ("\r\n");
			ffw.close();
			} catch (Exception e) {}
	}

	/*public static void impressionClasseRatio(int world_age,int eaten,int good,int stuck) {
		try{
			File ff=new File("donnee2.txt");
			FileWriter ffw=new FileWriter(ff,true);
			ffw.write(Double.toString(world_age));
			ffw.write ("\t");
			ffw.write(Double.toString(eaten/good));
			ffw.write ("\r\n");
			ffw.close();
			} catch (Exception e) {}
	}*/

}


