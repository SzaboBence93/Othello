package MI;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class GameFrame extends JFrame  {

	private static final long serialVersionUID = 1L;
	private static GameFrame gf; //maga az ablak
	public static View view; //A játékmezõ színtere, és egyben kontroller funkciók
	

	public GameFrame(){
		view = new View();
		//Inicializáció
		InitGui();
	}
	
	//GUI inicializálása
	private void InitGui(){
		//A panel amire kirajzolódik a GUI
		JPanel gamecanvas = new JPanel(new GridLayout(1,1), true);
		//Megkapja a view componenst ami rendelkezik az onPainttel
		gamecanvas.add(view);
		//A view egyben kezeli a felhasználói interakciót is (Document & view modell)
		addMouseListener(view);
		addMouseMotionListener(view);
		addKeyListener(view);
		
		//Panel beadása, ablak beállítása
		this.setSize(View.window_size+6, View.window_size+29); //Ablak mérete, +borderek miatti pixelek
		this.setLayout(new BorderLayout(0, 0)); //elrendezés meghatározása, nyujtott cella (az egész ablak lefedése)
		this.add(gamecanvas, BorderLayout.CENTER); //view layout beadása
		setDefaultCloseOperation(EXIT_ON_CLOSE); //bezárásnál leáll a grafikai szál
		setResizable(false); //nem átméretezhetõ
		setVisible(true); //láthatóság bekapcsolása
	}
	public static void main(String[] args) throws InterruptedException {
		
		//Grafikus felület egy új szálon indul el
		SwingUtilities.invokeLater(
				new Runnable() {
		            @Override
		            public void run() {
		            	//Ablak létrehozása
		            	gf = new GameFrame();
		            }
	            }
			);
		
		//Kicsit várunk az ablak létrehozása után
		while(GameFrame.view == null) 
			Thread.sleep(100);
		
		while(gf == null) 
			Thread.sleep(100);
		
		boolean again = true;
		while (again){
			//Játék deklarálása
			Game game = new Game();
			
			//Folyton kiírjuk a játék állását, ehhez kellenek a következõ változók:
			int onec = 2, twoc = 2;
			String out = "AI: 2 YOU: 2";
			//Játék futtatása amíg nincs vége a játéknak
			while (!game.gameover) {
				game.Update(); //Update lánc
				//új pontok
				int noneCnt = Game.PlayerOneCount();
				int ntwoCnt = Game.discsize()-noneCnt;
				//Ha változott akkor kiírjuk fejlécre
				if (noneCnt != onec || ntwoCnt != twoc){
					out = "AI: "+noneCnt + " YOU: "+ntwoCnt;
					gf.setTitle(out);
					//A régi megkapja az újat
					onec = noneCnt;
					twoc = ntwoCnt;
				}
			}		
			//Játék vége, hozzáadjuk a fejléc tartalmához
			gf.setTitle(out + " --------- Game Over");
			
			//Elõugró ablak felparaméterezése
			Object[] PossibleValues = {"Igen", "Nem"};
			//Kérdés/Típus/gombok típus/gombok szöveg
			JOptionPane opt = new JOptionPane("Új játék indítása?", 
			JOptionPane.QUESTION_MESSAGE, 
			JOptionPane.YES_NO_OPTION, null, PossibleValues); 
			// Az ablak neve
			JDialog jd = opt.createDialog(gf, "Kérdés");
			jd.setVisible(true); //Megjelenítés
			//Ha kiikszelte vagy ha nemet választott
			if (opt.getValue() == null || opt.getValue() == PossibleValues[1]) 
				again = false;
			else {
				//Új játék
				//Pár dolog amit a viewban vissza kell állítani
				View.initialize();
				GameFrame.view.repaint();
			}
		}
		//Nincs új játék
		GameFrame.gf.dispatchEvent(new WindowEvent(GameFrame.gf, WindowEvent.WINDOW_CLOSING));
	}

}
