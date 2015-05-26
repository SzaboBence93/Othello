package MI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;



public class View extends JComponent implements MouseListener, MouseMotionListener, KeyListener{
	private static final long serialVersionUID = 1L;
	private Graphics2D drawingSurface;
	private BufferedImage offScreenBuffer;
	private static int fieldwidth, d, dragX, dragY;
	private static Color disk1, disk2, fieldC1, fieldC2;
	private boolean released = true;
	public static int window_size = 700;
	public static Disc dragDisc;
	
	//El�z� l�p�sek visszan�z�s�hez sz�ks�ges v�ltoz�k
	private static boolean replay = false;
	private static int rv = 1;
	private static boolean len = true;
	private static boolean ren = true;


	
	
	
	
	public View(){
		initialize();
		//Bufferk�p a kimenetnek a t�rol�s�ra
		offScreenBuffer = new BufferedImage(fieldwidth * 8, fieldwidth * 8, BufferedImage.TYPE_INT_ARGB);
		//Kirajzol� meghat�roz�sa
		drawingSurface = offScreenBuffer.createGraphics();
		drawingSurface.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}
	
	//Inicializ�ci�
	public static void initialize(){
		//Kezd� param�terek be�ll�t�sa
		fieldwidth = window_size/8; //Mez� sz�less�ge
		d = (int)(fieldwidth - (double)fieldwidth*0.2); //korong �tm�r�je
		disk1 = Color.black; //korong 1 sz�ne
		disk2 = Color.white; //korong kett� sz�ne
		fieldC1 = new Color(.6f,1f,.4f); //mez� 1 sz�ne
		fieldC2 = new Color(.6f,.8f,.4f); //mez� 2 sz�ne
		dragDisc = null; //kurzor �ltalt hordozott korong
		
		replay = false; //replay m�dban van-e
		rv = 1; //replay index, melyik n�zetet n�zi
		len = true; //bal billenty�t le�theti
		ren = true; //jobb billenty�t le�theti
	}
	
	//Ablakkezel� f�ggv�ny kirajzol� f�ggv�nye
	public void paint(Graphics g) {
		DrawFields(); //Mez�k kirajzol�sa
		drawDiscs(); //korongok kirajzol�sa
		g.drawImage(offScreenBuffer, 0, 0, null); //buffer kirajzol�sa
	}
	
	//Mez�k kirajzol�sa
	private void DrawFields(){
		boolean dark = false;
		for (int y = 0; y<8; y++)
			for (int x = 0; x<8; x++){
				drawingSurface.setColor(dark ? fieldC1 : fieldC2); //sz�n be�ll�t�sa
				drawingSurface.fillRect(x*fieldwidth, y*fieldwidth, fieldwidth, fieldwidth); //kit�lt�tt n�gyzet kirajzol�sa
				if (x!=7) dark = dark ? false : true; //mez� t�pus v�lt�sa
			}
	}
	
	//korongok kirajzol�sa
	private void drawDiscs(){
		int i = 0;
		Disc disc;
		
		if (!replay) //Ha nem replay n�zet
		while((disc = Game.GetDisc(i++)) != null){ //Addig k�rj�k a Game oszt�lyt�l a korongokat ameddig van
			//A korong t�pus�t�l f�gg�en be�ll�tjuk a szin�t
			drawingSurface.setColor(disc.p.type ? disk1 : disk2);
			if (disc != dragDisc) //Ha nem a hordozott korong
				{
				//Ha az utols� lerakott akkor annak kisebb �tm�r�, egy�bk�nt az inicializ�ci�ban meghat�rozott
				if (i == Game.discsize()-1)
					drawingSurface.fillOval(disc.x * fieldwidth + fieldwidth/2 - (int)(d/2.8),disc.y * fieldwidth + fieldwidth/2 - (int)(d/2.8), (int)(d/1.4), (int)(d/1.4));
				else 
					drawingSurface.fillOval(disc.x * fieldwidth + fieldwidth/2 - d/2,disc.y * fieldwidth + fieldwidth/2 - d/2, d, d);
				}
		}
		else
			//A kiv�lasztott n�zet megjelen�t�se a t�bl�n replay m�dban
			if (Game.pre.size() != 0) 
				for (Disc ds : Game.pre.get(Game.pre.size()-rv)){
					drawingSurface.setColor(ds.p.type ? disk1 : disk2);
					if (ds == Game.pre.get(Game.pre.size()-rv).get(Game.pre.get(Game.pre.size()-rv).size()-1))
						drawingSurface.fillOval(ds.x * fieldwidth + fieldwidth/2 - (int)(d/2.8),ds.y * fieldwidth + fieldwidth/2 - (int)(d/2.8), (int)(d/1.4), (int)(d/1.4));
					else 
						drawingSurface.fillOval(ds.x * fieldwidth + fieldwidth/2 - d/2,ds.y * fieldwidth + fieldwidth/2 - d/2, d, d);
					
				}
		//Ha a hordozott diszk l�tezik akkor kirajzoljuk, a t�bbi diszk f�l� (mozgat�s k�zben ne ker�lj�n egy diszk kirajzol�sa al�)
		if (dragDisc != null){
			drawingSurface.setColor(dragDisc.p.type ? disk1 : disk2);
			drawingSurface.fillOval(dragX,dragY, d, d);
		}
		//Innent�l debug, ellen�rz�si c�lok
		//Szomsz�dok
		/*drawingSurface.setColor(Color.CYAN);
		for (Disc j : Game.NBdiscs)
			drawingSurface.fillOval(j.x * fieldwidth + fieldwidth/2 - d/2,j.y * fieldwidth + fieldwidth/2 - d/2, d, d);*/
		//Lehets�ges lerak�si helyek
		if (!replay){
			drawingSurface.setColor(Color.red);
			if (dragDisc != null) for (Disc j : Game.PossibleDisc(Game.NBdiscs, dragDisc.p, null))
				drawingSurface.fillOval(j.x * fieldwidth + fieldwidth/2 - d/2,j.y * fieldwidth + fieldwidth/2 - d/2, d, d);
		}
	}

	@Override
	public void mouseMoved(MouseEvent m) {
		//Ha van hordozott diszk, akkor annak poz�ci�ja felveszi kurzor poz�ci�j�t, majd kirajzol�s
		if (dragDisc != null){
			dragX = m.getX()-8 - d/2;
			dragY = m.getY()-31 - d/2;
			repaint();
		}
	}
	
	public void Clicked(MouseEvent m){
		//Ha kattintottak akkor megviszg�ljuk, hogy lehets�ges lerakhat� helyre kattintott e
		dragX = m.getX()-8;
		dragY = m.getY()-31;
		if (dragDisc != null && !replay){
			//Mez� m�trix x,y meghat�roz�sa
			int x = dragX/fieldwidth;
			int y = dragY/fieldwidth;
			//L�p�s helyess�g�nek megvizsg�l�sa
			if (Game.Step(x, y, dragDisc)){
				dragDisc = null;
				repaint();
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent m) {}

	@Override
	public void mouseEntered(MouseEvent arg0) {}

	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent m) {
		//kattint�sok �rz�kel�s�nek gyakoris�g�nak n�vel�se
		if (dragDisc != null && m.getButton() == MouseEvent.BUTTON1 && released) {
			released = false;
			Clicked(m);
		}
	}

	@Override
	public void mouseReleased(MouseEvent m) {
		//Kell az el�z�h�z a helyes m�k�d�s garant�l�s�hoz(nem ragadhat bent abban a f�ggv�nyben)
		if (m.getButton() == MouseEvent.BUTTON1) released = true; 
	}
	
	@Override
	public void mouseDragged(MouseEvent m) {}

	@Override
	public void keyPressed(KeyEvent e) {
		//Replay be�ll�t�sa
		if (replay){
			if (e.getKeyCode() == KeyEvent.VK_LEFT && len && Game.pre.size() > rv){
				//visszan�z�s
				rv++;
				//perg�s megakad�lyoz�sa
				len = false;
				repaint();
			}
			if (e.getKeyCode() == KeyEvent.VK_RIGHT && ren && rv > 1){
				//el�ren�z�s
				rv--;
				//perg�s megakad�lyoz�sa
				ren = false;
				repaint();
			}
		}else
		if (e.getKeyCode() == KeyEvent.VK_A)
			{
				//replay bekapcs, alap�rt�kek
				replay = true;
				rv = 1;
				repaint();
			}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		//perg�s elleni biztos�t�s
		if (e.getKeyCode() == KeyEvent.VK_LEFT) len = true;
		if (e.getKeyCode() == KeyEvent.VK_RIGHT) ren = true;
		if (e.getKeyCode() == KeyEvent.VK_A){
			//replay kikapcs
			replay = false;
			rv = 1;
			repaint();
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
}
