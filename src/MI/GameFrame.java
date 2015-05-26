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
	public static View view; //A j�t�kmez� sz�ntere, �s egyben kontroller funkci�k
	

	public GameFrame(){
		view = new View();
		//Inicializ�ci�
		InitGui();
	}
	
	//GUI inicializ�l�sa
	private void InitGui(){
		//A panel amire kirajzol�dik a GUI
		JPanel gamecanvas = new JPanel(new GridLayout(1,1), true);
		//Megkapja a view componenst ami rendelkezik az onPainttel
		gamecanvas.add(view);
		//A view egyben kezeli a felhaszn�l�i interakci�t is (Document & view modell)
		addMouseListener(view);
		addMouseMotionListener(view);
		addKeyListener(view);
		
		//Panel bead�sa, ablak be�ll�t�sa
		this.setSize(View.window_size+6, View.window_size+29); //Ablak m�rete, +borderek miatti pixelek
		this.setLayout(new BorderLayout(0, 0)); //elrendez�s meghat�roz�sa, nyujtott cella (az eg�sz ablak lefed�se)
		this.add(gamecanvas, BorderLayout.CENTER); //view layout bead�sa
		setDefaultCloseOperation(EXIT_ON_CLOSE); //bez�r�sn�l le�ll a grafikai sz�l
		setResizable(false); //nem �tm�retezhet�
		setVisible(true); //l�that�s�g bekapcsol�sa
	}
	public static void main(String[] args) throws InterruptedException {
		
		//Grafikus fel�let egy �j sz�lon indul el
		SwingUtilities.invokeLater(
				new Runnable() {
		            @Override
		            public void run() {
		            	//Ablak l�trehoz�sa
		            	gf = new GameFrame();
		            }
	            }
			);
		
		//Kicsit v�runk az ablak l�trehoz�sa ut�n
		while(GameFrame.view == null) 
			Thread.sleep(100);
		
		while(gf == null) 
			Thread.sleep(100);
		
		boolean again = true;
		while (again){
			//J�t�k deklar�l�sa
			Game game = new Game();
			
			//Folyton ki�rjuk a j�t�k �ll�s�t, ehhez kellenek a k�vetkez� v�ltoz�k:
			int onec = 2, twoc = 2;
			String out = "AI: 2 YOU: 2";
			//J�t�k futtat�sa am�g nincs v�ge a j�t�knak
			while (!game.gameover) {
				game.Update(); //Update l�nc
				//�j pontok
				int noneCnt = Game.PlayerOneCount();
				int ntwoCnt = Game.discsize()-noneCnt;
				//Ha v�ltozott akkor ki�rjuk fejl�cre
				if (noneCnt != onec || ntwoCnt != twoc){
					out = "AI: "+noneCnt + " YOU: "+ntwoCnt;
					gf.setTitle(out);
					//A r�gi megkapja az �jat
					onec = noneCnt;
					twoc = ntwoCnt;
				}
			}		
			//J�t�k v�ge, hozz�adjuk a fejl�c tartalm�hoz
			gf.setTitle(out + " --------- Game Over");
			
			//El�ugr� ablak felparam�terez�se
			Object[] PossibleValues = {"Igen", "Nem"};
			//K�rd�s/T�pus/gombok t�pus/gombok sz�veg
			JOptionPane opt = new JOptionPane("�j j�t�k ind�t�sa?", 
			JOptionPane.QUESTION_MESSAGE, 
			JOptionPane.YES_NO_OPTION, null, PossibleValues); 
			// Az ablak neve
			JDialog jd = opt.createDialog(gf, "K�rd�s");
			jd.setVisible(true); //Megjelen�t�s
			//Ha kiikszelte vagy ha nemet v�lasztott
			if (opt.getValue() == null || opt.getValue() == PossibleValues[1]) 
				again = false;
			else {
				//�j j�t�k
				//P�r dolog amit a viewban vissza kell �ll�tani
				View.initialize();
				GameFrame.view.repaint();
			}
		}
		//Nincs �j j�t�k
		GameFrame.gf.dispatchEvent(new WindowEvent(GameFrame.gf, WindowEvent.WINDOW_CLOSING));
	}

}
