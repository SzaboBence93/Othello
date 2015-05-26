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
	
	//Elõzõ lépések visszanézéséhez szükséges változók
	private static boolean replay = false;
	private static int rv = 1;
	private static boolean len = true;
	private static boolean ren = true;


	
	
	
	
	public View(){
		initialize();
		//Bufferkép a kimenetnek a tárolására
		offScreenBuffer = new BufferedImage(fieldwidth * 8, fieldwidth * 8, BufferedImage.TYPE_INT_ARGB);
		//Kirajzoló meghatározása
		drawingSurface = offScreenBuffer.createGraphics();
		drawingSurface.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}
	
	//Inicializáció
	public static void initialize(){
		//Kezdõ paraméterek beállítása
		fieldwidth = window_size/8; //Mezõ szélessége
		d = (int)(fieldwidth - (double)fieldwidth*0.2); //korong átmérõje
		disk1 = Color.black; //korong 1 színe
		disk2 = Color.white; //korong kettõ színe
		fieldC1 = new Color(.6f,1f,.4f); //mezõ 1 színe
		fieldC2 = new Color(.6f,.8f,.4f); //mezõ 2 színe
		dragDisc = null; //kurzor általt hordozott korong
		
		replay = false; //replay módban van-e
		rv = 1; //replay index, melyik nézetet nézi
		len = true; //bal billentyût leütheti
		ren = true; //jobb billentyát leütheti
	}
	
	//Ablakkezelõ függvény kirajzoló függvénye
	public void paint(Graphics g) {
		DrawFields(); //Mezõk kirajzolása
		drawDiscs(); //korongok kirajzolása
		g.drawImage(offScreenBuffer, 0, 0, null); //buffer kirajzolása
	}
	
	//Mezõk kirajzolása
	private void DrawFields(){
		boolean dark = false;
		for (int y = 0; y<8; y++)
			for (int x = 0; x<8; x++){
				drawingSurface.setColor(dark ? fieldC1 : fieldC2); //szín beállítása
				drawingSurface.fillRect(x*fieldwidth, y*fieldwidth, fieldwidth, fieldwidth); //kitöltött négyzet kirajzolása
				if (x!=7) dark = dark ? false : true; //mezõ típus váltása
			}
	}
	
	//korongok kirajzolása
	private void drawDiscs(){
		int i = 0;
		Disc disc;
		
		if (!replay) //Ha nem replay nézet
		while((disc = Game.GetDisc(i++)) != null){ //Addig kérjük a Game osztálytól a korongokat ameddig van
			//A korong típusától függõen beállítjuk a szinét
			drawingSurface.setColor(disc.p.type ? disk1 : disk2);
			if (disc != dragDisc) //Ha nem a hordozott korong
				{
				//Ha az utolsó lerakott akkor annak kisebb átmérõ, egyébként az inicializációban meghatározott
				if (i == Game.discsize()-1)
					drawingSurface.fillOval(disc.x * fieldwidth + fieldwidth/2 - (int)(d/2.8),disc.y * fieldwidth + fieldwidth/2 - (int)(d/2.8), (int)(d/1.4), (int)(d/1.4));
				else 
					drawingSurface.fillOval(disc.x * fieldwidth + fieldwidth/2 - d/2,disc.y * fieldwidth + fieldwidth/2 - d/2, d, d);
				}
		}
		else
			//A kiválasztott nézet megjelenítése a táblán replay módban
			if (Game.pre.size() != 0) 
				for (Disc ds : Game.pre.get(Game.pre.size()-rv)){
					drawingSurface.setColor(ds.p.type ? disk1 : disk2);
					if (ds == Game.pre.get(Game.pre.size()-rv).get(Game.pre.get(Game.pre.size()-rv).size()-1))
						drawingSurface.fillOval(ds.x * fieldwidth + fieldwidth/2 - (int)(d/2.8),ds.y * fieldwidth + fieldwidth/2 - (int)(d/2.8), (int)(d/1.4), (int)(d/1.4));
					else 
						drawingSurface.fillOval(ds.x * fieldwidth + fieldwidth/2 - d/2,ds.y * fieldwidth + fieldwidth/2 - d/2, d, d);
					
				}
		//Ha a hordozott diszk létezik akkor kirajzoljuk, a többi diszk fölé (mozgatás közben ne kerüljön egy diszk kirajzolása alá)
		if (dragDisc != null){
			drawingSurface.setColor(dragDisc.p.type ? disk1 : disk2);
			drawingSurface.fillOval(dragX,dragY, d, d);
		}
		//Innentõl debug, ellenörzési célok
		//Szomszédok
		/*drawingSurface.setColor(Color.CYAN);
		for (Disc j : Game.NBdiscs)
			drawingSurface.fillOval(j.x * fieldwidth + fieldwidth/2 - d/2,j.y * fieldwidth + fieldwidth/2 - d/2, d, d);*/
		//Lehetséges lerakási helyek
		if (!replay){
			drawingSurface.setColor(Color.red);
			if (dragDisc != null) for (Disc j : Game.PossibleDisc(Game.NBdiscs, dragDisc.p, null))
				drawingSurface.fillOval(j.x * fieldwidth + fieldwidth/2 - d/2,j.y * fieldwidth + fieldwidth/2 - d/2, d, d);
		}
	}

	@Override
	public void mouseMoved(MouseEvent m) {
		//Ha van hordozott diszk, akkor annak pozíciója felveszi kurzor pozícióját, majd kirajzolás
		if (dragDisc != null){
			dragX = m.getX()-8 - d/2;
			dragY = m.getY()-31 - d/2;
			repaint();
		}
	}
	
	public void Clicked(MouseEvent m){
		//Ha kattintottak akkor megviszgáljuk, hogy lehetséges lerakható helyre kattintott e
		dragX = m.getX()-8;
		dragY = m.getY()-31;
		if (dragDisc != null && !replay){
			//Mezõ mátrix x,y meghatározása
			int x = dragX/fieldwidth;
			int y = dragY/fieldwidth;
			//Lépés helyességének megvizsgálása
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
		//kattintésok érzékelésének gyakoriságának növelése
		if (dragDisc != null && m.getButton() == MouseEvent.BUTTON1 && released) {
			released = false;
			Clicked(m);
		}
	}

	@Override
	public void mouseReleased(MouseEvent m) {
		//Kell az elõzõhöz a helyes mûködés garantálásához(nem ragadhat bent abban a függvényben)
		if (m.getButton() == MouseEvent.BUTTON1) released = true; 
	}
	
	@Override
	public void mouseDragged(MouseEvent m) {}

	@Override
	public void keyPressed(KeyEvent e) {
		//Replay beállítása
		if (replay){
			if (e.getKeyCode() == KeyEvent.VK_LEFT && len && Game.pre.size() > rv){
				//visszanézés
				rv++;
				//pergés megakadályozása
				len = false;
				repaint();
			}
			if (e.getKeyCode() == KeyEvent.VK_RIGHT && ren && rv > 1){
				//elõrenézés
				rv--;
				//pergés megakadályozása
				ren = false;
				repaint();
			}
		}else
		if (e.getKeyCode() == KeyEvent.VK_A)
			{
				//replay bekapcs, alapértékek
				replay = true;
				rv = 1;
				repaint();
			}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		//pergés elleni biztosítás
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
