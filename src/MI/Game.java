package MI;

import java.util.ArrayList;
import java.util.List;

public class Game  {
	private static List<Disc> discs; //diszkek list�ja
	public static Player one, two; //j�t�kosok
	public boolean gameover; //gameover flag
	private static boolean oneStart = true;  //melyik j�t�kos k�vetkezik k�vet�s�re
	public static List<Disc> NBdiscs; //szomsz�dos mez�k, folyamatosan friss�tve, --> 
										//gyorsabb keres�s lehets�ges lerakhat� mez�kh�z
	private int possiblegameover = 0; //Lehet olyan h egyik j�t�kos sem tud tenni, ennek figyel�se
	private static boolean go = true; //Mehet a menet?
	
	public static List<List<Disc>> pre; //replay t�blan�zetek
	
	
	public Game(){
		//j�t�kosok deklar�l�sa
		pre = new ArrayList<>();
		one = new Player(true, true);
		two = new Player(false, false);
		//korongok list�j�nak inicializ�l�sa
		discs = new ArrayList<>();
		//j�t�k v�ge, m�g nincs v�ge
		gameover = false;
		
		//Alap �rt�kek inicializ�l�sa
		oneStart = true;
		possiblegameover = 0;
		go = true;
		
		NBdiscs = new ArrayList<>();
		
		//kezd� �ll�s kialak�t�sa
		discs.add(new Disc(3, 3, one));
		discs.add(new Disc(4, 3, two));
		discs.add(new Disc(4, 4, one));
		discs.add(new Disc(3, 4, two));
		
		//Szomsz�dos mez�k kezd�si felt�lt�se
		NBdiscs.add(new Disc(2,2,null));NBdiscs.add(new Disc(3,2,null));
		NBdiscs.add(new Disc(4,2,null));NBdiscs.add(new Disc(5,2,null));
		NBdiscs.add(new Disc(2,5,null));NBdiscs.add(new Disc(3,5,null));
		NBdiscs.add(new Disc(4,5,null));NBdiscs.add(new Disc(5,5,null));
		NBdiscs.add(new Disc(2,3,null));NBdiscs.add(new Disc(2,4,null));
		NBdiscs.add(new Disc(5,3,null));NBdiscs.add(new Disc(5,4,null));
		
		List<Disc> nd = new ArrayList<>();
		for (Disc i : discs) nd.add(new Disc(i.x, i.y, i.p));
		pre.add(nd);
	}
	
	//visszadja a koronglista elemeinek sz�m�t
	public static int discsize(){
		return discs.size();
	}
	
	//index alapj�n visszaad egy korongot, amennyiben l�tezik
	public static Disc GetDisc(int i){
		if (discs == null) 
			return null;
		if (i < discs.size()) 
			return discs.get(i);
		return null; //egy�bk�nt null
	}
	
	//Bead egy korongot, ha a m�sodik param�ter true, akkor a kontroller megkapja a vez�rl�s�t ennek a korongnak --> kurzor figyel�se
	public static void AddDisc(Disc d, boolean view){
		discs.add(d);
		if (view) 
			View.dragDisc = d;
	}
	
	//Update f�ggv�ny
	public void Update(){
		//Ha lement �gy 6 k�r (enn�l kisebb is el�g lenne, de egy pillanat alatt megvan), 
		//hogy nem tudott l�pni egyik sem, akkor
		//vagy ha a t�rol� el�rte a 64es m�retet �s m�r mindegyik j�t�kos lerakta a lerakhat� korongj�t, 
		//akkor gameover flag bebillent�se
		if (possiblegameover > 5 || (discs.size() == 64 && one.lastDisc == null && two.lastDisc == null)) gameover = true;
		else if (discs.size() != 64 && go){
			//ide j�het minden ami kell a j�t�k vez�nyl�s�hez kell
			//one j�t�kos k�vetkezik
			if (oneStart) {
				//Ha tud rakni akkor megkapja az es�lyt, egy�bk�nt j�het a m�sik j�t�kos
				if (!PossibleDisc(NBdiscs, one, null).isEmpty()) {
					one.Update();
					possiblegameover = 0;
					//Ha ai akkor �jra rajzolunk, mert nem volt olyan esem�ny amire a kontroller felfigyelt volna
					if (!one.ai) go = false;
					else GameFrame.view.repaint();
				} else oneStart = false;
			}else {
				//two j�t�kos, szint�n ha van olyan mez� ahova lerakhat szab�ly szerint akkor kap es�ly, k�l�nben els� j�t�kos j�n
				if (!PossibleDisc(NBdiscs, two, null).isEmpty()) {
					two.Update(); 
					possiblegameover = 0;
					//Ha ai akkor �jra rajzolunk, mert nem volt olyan esem�ny amire a kontroller felfigyelt volna
					if (!two.ai) go = false;
					else GameFrame.view.repaint();
				} else 
					oneStart = true;
			}
			possiblegameover++;
		}
		
		
	}
	
	//vissza adja az x,y [0..7][0..7] poz�ci�ban tal�lhat korongot, ha van, ha nincs null
	public static Disc GetDisc(int x, int y, List<Disc> d){
		if (d == null) d = discs;
		for (Disc i : d) if (i.x == x && i.y == y) return i;
		return null;
	}
	
	//Meghat�rozza hogy az emberi j�t�kos a GUIn kereszt�l helyes l�p�st akar e v�grehajtani
	private static boolean RightStep(int x, int y, Disc target){
		//egy �thelyezett korong szab�lyoknak megfelel�en helyes helyre ker�lt e, ha nem false �rt�k vissza
		//ablakon k�v�lre nem helyez�nk :D
		if (x> 7 || y>7) return false;
		
		//Ide lehet meg�rni minden szab�lyszer�s�get, teh�t le�t�sn�l korong megv�ltoztat�sa
		
		Disc find = GetDisc(x,y, null);
		//Ha olyan helyre pr�b�lt lerakni amin nincs korong, akkor...
		if (find == null){
			List<Disc> ch = new ArrayList<>();
			//Megvizsg�ljuk, hogy szab�lyszer�en is lehet e oda rakni...
			if (Cross(target.p, ch, x, y, null)){
				//ha igen akkor le�tj�k a metszetben l�v� korongokat
				for (Disc i : ch) i.p = target.p;
				//�s megkapja a val�s poz�ci�j�t a m�trixban a korong
				target.x = x;
				target.y = y;
				//ezt k�vet�en friss�tj�k a szomsz�d list�t
				NBUpdate(NBdiscs, target, null);
				return true;
			}
		}
		return false;
	}
	
	//L�p�s ellen�rz�se
	public static boolean Step(int x, int y, Disc target){
		//Szab�lyok ellen�rz�se
		boolean result = RightStep(x,y,target);
		//Ha helyes akkor cseleksz�nk...
		if (result){
			//replay n�zetek karbantart�sa, az utols� 20 p�lya�llapotot tartjuk ny�lv�n
			if (pre.size() > 20){
				for (int i=1; i<pre.size();i++) pre.set(i-1,pre.get(i));
				pre.remove(pre.size()-1);
			}
			
			//�j n�zet hozz�ad�sa
			List<Disc> nd = new ArrayList<>();
			for (Disc i : discs) nd.add(new Disc(i.x, i.y, i.p));
			pre.add(nd);
			
			//Ha az els� j�t�kos volt akkor a m�sodik j�n �s invert�lva
			if (oneStart){
				oneStart = false;
				one.lastDisc = null;
			}else{
				oneStart = true;
				two.lastDisc = null;
			}
			go = true;
		}
		return result;
	}
	
	//Szomsz�d lista friss�t�se
	public static void NBUpdate(List<Disc> nb, Disc d, List<Disc> ds){
		//Az �j szomsz�doknak egy lista
		List<Disc> lNB = new ArrayList<>();
		//Ha az �j korong nem a bal sz�l�n van a mez�nek
		if (d.x >0) {
			
			if (GetDisc(d.x-1, d.y, ds) == null) lNB.add(new Disc(d.x-1, d.y, null)); //Ha balra t�le nincs elem
			if (d.y < 7 && GetDisc(d.x-1, d.y+1, ds) == null) lNB.add(new Disc(d.x-1, d.y+1, null)); //ha balra t�le �s alatta egyel nincs elem
			if (d.y > 0 && GetDisc(d.x-1, d.y-1, ds) == null) lNB.add(new Disc(d.x-1, d.y-1, null)); //ha balra t�le alatta egyel nincs elem
			}
		//Ha az �j korong nem a jobb sz�l�n van a mez�nek
		if (d.x<7){
			if (GetDisc(d.x+1, d.y, ds) == null) lNB.add(new Disc(d.x+1, d.y, null)); //Ha jobbra t�le nincs elem
			if (d.y < 7 && GetDisc(d.x+1, d.y+1, ds) == null) lNB.add(new Disc(d.x+1, d.y+1, null)); //Ha jobbra t�le �s alatta egyel nincs elem
			if (d.y > 0 && GetDisc(d.x+1, d.y-1, ds) == null) lNB.add(new Disc(d.x+1, d.y-1, null)); //Ha jobbra t�le �s felette egyel nincs elem
		}
		
		//Ha azonos x poz�ci�ban de felette �s alatt nincs elem akkor azok is �j szomsz�dok
		if (d.y>0 && GetDisc(d.x, d.y-1, ds) == null) lNB.add(new Disc(d.x, d.y-1, null));
		if (d.y<7 && GetDisc(d.x, d.y+1, ds) == null) lNB.add(new Disc(d.x, d.y+1, null));
		
		//Ha az �j szomsz�dok list�nak van olyan elem ami m�r elem a r�ginek akkor azt kit�r�lj�k
		for (int i=0; i<lNB.size(); )
		{
			for (Disc j : nb) if (j.x == lNB.get(i).x && j.y == lNB.get(i).y){
				lNB.remove(i);
				i--;
				break;
			}
			i++;
		}
		//majd a r�gi szomsz�d list�b�l kiszedj�k a lehelyezett �j korongot
		for (int i=0; i<nb.size();) if (d.x == nb.get(i).x && d.y == nb.get(i).y) nb.remove(i); else i++;
		//A r�gi list�hoz hozz�adjuk az �jat
		nb.addAll(lNB);
	}
	
	//Lehets�ges lerakhat� korongok visszad�sa
	public static List<Disc> PossibleDisc(List<Disc> nb, Player p, List<Disc> ds){
		//eredm�ny lista
		List<Disc> re = new ArrayList<>();
		List<Disc> attack = new ArrayList<>(); //A lehets�ges lerak�ssal le�t�tt korongok list�ja
		for (Disc i : nb) {
			if (Cross(p, attack, i.x, i.y, ds)){
				//Amennyiben lehets�ges lerak�hely az aktu�lis szomsz�d lista beli elem, akkor...
				i.attack = attack; //az adott korongba elt�roljuk k�s�bbi felhaszn�l�sra a le�t�tt korongok list�j�t
				re.add(i); // hozz�adjuk az eredm�ny list�hoz
				attack = new ArrayList<>(); //�j list�t defini�lunk a leuthet� korongoknak
			}
		}
		return re;
	}
	
	
	//Megvizsg�lja, hogy az adott helyre az adott korong lehelyezhet�-e, �s el��ll�tja egy�ttal a le�thet� korongok list�j�t
	public static boolean Cross(Player p, List<Disc> change, int xi, int yi, List<Disc> ds){
		int dir = 0;
		//Megn�zz�k minden ir�nyban
		while(dir < 8){
			//a kezd� offszet x �s y ir�nyba is 0,0
			Dir d = new Dir(0,0);
			//Meghat�rozzuk az offszetet a 8 lehets�ges ir�ny k�z�l az aktu�lisra
			DirOffset(d, dir);
			//A bels� ciklus ciklusv�ltoz�ja
			boolean fin = false;
			//a fut� x �s y felveszi a lehelyezend� korong x �s yj�t
			int x = xi, y = yi;
			//delete offszet
			int delete = 0;
			//ha �res mez� van a lerakott �s a legk�zelebbi korong k�z�tt
			boolean empty = false;
			while(!fin){
				//x �s y �rt�k�t n�velj�k az ir�nynak megfelel� offszettel
				x+=d.getx();
				y+=d.gety();
				//Ha el�rt�k a p�lya sz�l�t, vagy el�rt�nk egy olyan korongot aminek j�t�kos mutat�ja megegyezik
				//a mienkkel akkor a ennek a ciklusnak v�ge
				if (x<0 || y < 0 || y>7 || x>7 ||  (GetDisc(x, y, ds) != null && GetDisc(x, y, ds).p == p) ) 
					{
						//Ha volt �res mez� vagy nem �rt�nk el saj�t korongot akkor t�r�lj�k a megfelel� ofszettel a le�thet� korongokat
						if (x<0 || y < 0 || y>7 || x>7 || empty) 
							for (int i = change.size() - delete; i<change.size();) change.remove(i);
						fin = true;
					}
				else{
					if (GetDisc(x, y, ds) != null)	
						{
							//Egy�bk�nt tal�ltunk lehets�ges le�thet� korongot
							change.add(GetDisc(x, y, ds));
							//n�velj�k az ofszetet ha esetleg kider�lne hogy nem tal�lunk azonos j�t�kos korongot a bej�r�s sor�n
							delete++;
						}else empty = true;
				}				
			}
			dir++;
		}
		//Ha �t�tt�nk le korongot akkor lehets�ges l�p�s, egy�bk�nt nem
		if (change.size() != 0) 
			return true;
		return false;
	}
	
	//Ir�nyofszet meghat�roz�sa a megkapott ir�ny alapj�n
	private static void DirOffset(Dir d, int dir){
		if (dir == 0)  d.sety(-1); //fel
		if (dir == 1)  d.setx(1);  //jobbra
		if (dir == 2)  d.sety(1);  //le
		if (dir == 3)  d.setx(-1); //balra
		
		if (dir == 4 || dir == 5)  d.setx(-1); //balra �tl�san
		if (dir == 6 || dir == 7)  d.setx(1);  //jobbra �tl�san
		if (dir == 4 || dir == 7)  d.sety(-1); //fel �tl�san
		if (dir == 5 || dir == 6)  d.sety(1); //le �tl�san
	}
	
	//Visszaadja az els� j�t�kos �ltal birtokolt korongok sz�m�t
	public static int PlayerOneCount(){
		int count = 0;
		for (Disc i : discs) 
			if (i.p == one) 
				count++;
		return count;
	}
	
}
