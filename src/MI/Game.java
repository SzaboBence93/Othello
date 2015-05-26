package MI;

import java.util.ArrayList;
import java.util.List;

public class Game  {
	private static List<Disc> discs; //diszkek listája
	public static Player one, two; //játékosok
	public boolean gameover; //gameover flag
	private static boolean oneStart = true;  //melyik játékos következik követésére
	public static List<Disc> NBdiscs; //szomszédos mezõk, folyamatosan frissítve, --> 
										//gyorsabb keresés lehetséges lerakható mezõkhöz
	private int possiblegameover = 0; //Lehet olyan h egyik játékos sem tud tenni, ennek figyelése
	private static boolean go = true; //Mehet a menet?
	
	public static List<List<Disc>> pre; //replay táblanézetek
	
	
	public Game(){
		//játékosok deklarálása
		pre = new ArrayList<>();
		one = new Player(true, true);
		two = new Player(false, false);
		//korongok listájának inicializálása
		discs = new ArrayList<>();
		//játék vége, még nincs vége
		gameover = false;
		
		//Alap értékek inicializálása
		oneStart = true;
		possiblegameover = 0;
		go = true;
		
		NBdiscs = new ArrayList<>();
		
		//kezdõ állás kialakítása
		discs.add(new Disc(3, 3, one));
		discs.add(new Disc(4, 3, two));
		discs.add(new Disc(4, 4, one));
		discs.add(new Disc(3, 4, two));
		
		//Szomszédos mezõk kezdési feltöltése
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
	
	//visszadja a koronglista elemeinek számát
	public static int discsize(){
		return discs.size();
	}
	
	//index alapján visszaad egy korongot, amennyiben létezik
	public static Disc GetDisc(int i){
		if (discs == null) 
			return null;
		if (i < discs.size()) 
			return discs.get(i);
		return null; //egyébként null
	}
	
	//Bead egy korongot, ha a második paraméter true, akkor a kontroller megkapja a vezérlését ennek a korongnak --> kurzor figyelése
	public static void AddDisc(Disc d, boolean view){
		discs.add(d);
		if (view) 
			View.dragDisc = d;
	}
	
	//Update függvény
	public void Update(){
		//Ha lement úgy 6 kör (ennél kisebb is elég lenne, de egy pillanat alatt megvan), 
		//hogy nem tudott lépni egyik sem, akkor
		//vagy ha a tároló elérte a 64es méretet és már mindegyik játékos lerakta a lerakható korongját, 
		//akkor gameover flag bebillentése
		if (possiblegameover > 5 || (discs.size() == 64 && one.lastDisc == null && two.lastDisc == null)) gameover = true;
		else if (discs.size() != 64 && go){
			//ide jöhet minden ami kell a játék vezényléséhez kell
			//one játékos következik
			if (oneStart) {
				//Ha tud rakni akkor megkapja az esélyt, egyébként jöhet a másik játékos
				if (!PossibleDisc(NBdiscs, one, null).isEmpty()) {
					one.Update();
					possiblegameover = 0;
					//Ha ai akkor újra rajzolunk, mert nem volt olyan esemény amire a kontroller felfigyelt volna
					if (!one.ai) go = false;
					else GameFrame.view.repaint();
				} else oneStart = false;
			}else {
				//two játékos, szintén ha van olyan mezõ ahova lerakhat szabály szerint akkor kap esély, különben elsõ játékos jön
				if (!PossibleDisc(NBdiscs, two, null).isEmpty()) {
					two.Update(); 
					possiblegameover = 0;
					//Ha ai akkor újra rajzolunk, mert nem volt olyan esemény amire a kontroller felfigyelt volna
					if (!two.ai) go = false;
					else GameFrame.view.repaint();
				} else 
					oneStart = true;
			}
			possiblegameover++;
		}
		
		
	}
	
	//vissza adja az x,y [0..7][0..7] pozícióban találhat korongot, ha van, ha nincs null
	public static Disc GetDisc(int x, int y, List<Disc> d){
		if (d == null) d = discs;
		for (Disc i : d) if (i.x == x && i.y == y) return i;
		return null;
	}
	
	//Meghatározza hogy az emberi játékos a GUIn keresztül helyes lépést akar e végrehajtani
	private static boolean RightStep(int x, int y, Disc target){
		//egy áthelyezett korong szabályoknak megfelelõen helyes helyre került e, ha nem false érték vissza
		//ablakon kívülre nem helyezünk :D
		if (x> 7 || y>7) return false;
		
		//Ide lehet megírni minden szabályszerûséget, tehát leütésnél korong megváltoztatása
		
		Disc find = GetDisc(x,y, null);
		//Ha olyan helyre próbált lerakni amin nincs korong, akkor...
		if (find == null){
			List<Disc> ch = new ArrayList<>();
			//Megvizsgáljuk, hogy szabályszerûen is lehet e oda rakni...
			if (Cross(target.p, ch, x, y, null)){
				//ha igen akkor leütjük a metszetben lévõ korongokat
				for (Disc i : ch) i.p = target.p;
				//és megkapja a valós pozícióját a mátrixban a korong
				target.x = x;
				target.y = y;
				//ezt követõen frissítjük a szomszéd listát
				NBUpdate(NBdiscs, target, null);
				return true;
			}
		}
		return false;
	}
	
	//Lépés ellenörzése
	public static boolean Step(int x, int y, Disc target){
		//Szabályok ellenörzése
		boolean result = RightStep(x,y,target);
		//Ha helyes akkor cselekszünk...
		if (result){
			//replay nézetek karbantartása, az utolsó 20 pályaállapotot tartjuk nyílván
			if (pre.size() > 20){
				for (int i=1; i<pre.size();i++) pre.set(i-1,pre.get(i));
				pre.remove(pre.size()-1);
			}
			
			//Új nézet hozzáadása
			List<Disc> nd = new ArrayList<>();
			for (Disc i : discs) nd.add(new Disc(i.x, i.y, i.p));
			pre.add(nd);
			
			//Ha az elsõ játékos volt akkor a második jön és invertálva
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
	
	//Szomszéd lista frissítése
	public static void NBUpdate(List<Disc> nb, Disc d, List<Disc> ds){
		//Az új szomszédoknak egy lista
		List<Disc> lNB = new ArrayList<>();
		//Ha az új korong nem a bal szélén van a mezõnek
		if (d.x >0) {
			
			if (GetDisc(d.x-1, d.y, ds) == null) lNB.add(new Disc(d.x-1, d.y, null)); //Ha balra tõle nincs elem
			if (d.y < 7 && GetDisc(d.x-1, d.y+1, ds) == null) lNB.add(new Disc(d.x-1, d.y+1, null)); //ha balra tõle és alatta egyel nincs elem
			if (d.y > 0 && GetDisc(d.x-1, d.y-1, ds) == null) lNB.add(new Disc(d.x-1, d.y-1, null)); //ha balra tõle alatta egyel nincs elem
			}
		//Ha az új korong nem a jobb szélén van a mezõnek
		if (d.x<7){
			if (GetDisc(d.x+1, d.y, ds) == null) lNB.add(new Disc(d.x+1, d.y, null)); //Ha jobbra tõle nincs elem
			if (d.y < 7 && GetDisc(d.x+1, d.y+1, ds) == null) lNB.add(new Disc(d.x+1, d.y+1, null)); //Ha jobbra tõle és alatta egyel nincs elem
			if (d.y > 0 && GetDisc(d.x+1, d.y-1, ds) == null) lNB.add(new Disc(d.x+1, d.y-1, null)); //Ha jobbra tõle és felette egyel nincs elem
		}
		
		//Ha azonos x pozícióban de felette és alatt nincs elem akkor azok is új szomszédok
		if (d.y>0 && GetDisc(d.x, d.y-1, ds) == null) lNB.add(new Disc(d.x, d.y-1, null));
		if (d.y<7 && GetDisc(d.x, d.y+1, ds) == null) lNB.add(new Disc(d.x, d.y+1, null));
		
		//Ha az új szomszédok listának van olyan elem ami már elem a réginek akkor azt kitöröljük
		for (int i=0; i<lNB.size(); )
		{
			for (Disc j : nb) if (j.x == lNB.get(i).x && j.y == lNB.get(i).y){
				lNB.remove(i);
				i--;
				break;
			}
			i++;
		}
		//majd a régi szomszéd listából kiszedjük a lehelyezett új korongot
		for (int i=0; i<nb.size();) if (d.x == nb.get(i).x && d.y == nb.get(i).y) nb.remove(i); else i++;
		//A régi listához hozzáadjuk az újat
		nb.addAll(lNB);
	}
	
	//Lehetséges lerakható korongok visszadása
	public static List<Disc> PossibleDisc(List<Disc> nb, Player p, List<Disc> ds){
		//eredmény lista
		List<Disc> re = new ArrayList<>();
		List<Disc> attack = new ArrayList<>(); //A lehetséges lerakással leütütt korongok listája
		for (Disc i : nb) {
			if (Cross(p, attack, i.x, i.y, ds)){
				//Amennyiben lehetséges lerakóhely az aktuális szomszéd lista beli elem, akkor...
				i.attack = attack; //az adott korongba eltároljuk késõbbi felhasználásra a leütött korongok listáját
				re.add(i); // hozzáadjuk az eredmény listához
				attack = new ArrayList<>(); //új listát definiálunk a leuthetõ korongoknak
			}
		}
		return re;
	}
	
	
	//Megvizsgálja, hogy az adott helyre az adott korong lehelyezhetõ-e, és elõállítja egyúttal a leüthetõ korongok listáját
	public static boolean Cross(Player p, List<Disc> change, int xi, int yi, List<Disc> ds){
		int dir = 0;
		//Megnézzük minden irányban
		while(dir < 8){
			//a kezdõ offszet x és y irányba is 0,0
			Dir d = new Dir(0,0);
			//Meghatározzuk az offszetet a 8 lehetséges irány közül az aktuálisra
			DirOffset(d, dir);
			//A belsõ ciklus ciklusváltozója
			boolean fin = false;
			//a futó x és y felveszi a lehelyezendõ korong x és yját
			int x = xi, y = yi;
			//delete offszet
			int delete = 0;
			//ha üres mezõ van a lerakott és a legközelebbi korong között
			boolean empty = false;
			while(!fin){
				//x és y értékét növeljük az iránynak megfelelõ offszettel
				x+=d.getx();
				y+=d.gety();
				//Ha elértük a pálya szélét, vagy elértünk egy olyan korongot aminek játékos mutatója megegyezik
				//a mienkkel akkor a ennek a ciklusnak vége
				if (x<0 || y < 0 || y>7 || x>7 ||  (GetDisc(x, y, ds) != null && GetDisc(x, y, ds).p == p) ) 
					{
						//Ha volt üres mezõ vagy nem értünk el saját korongot akkor töröljük a megfelelõ ofszettel a leüthetõ korongokat
						if (x<0 || y < 0 || y>7 || x>7 || empty) 
							for (int i = change.size() - delete; i<change.size();) change.remove(i);
						fin = true;
					}
				else{
					if (GetDisc(x, y, ds) != null)	
						{
							//Egyébként találtunk lehetséges leüthetõ korongot
							change.add(GetDisc(x, y, ds));
							//növeljük az ofszetet ha esetleg kiderülne hogy nem találunk azonos játékos korongot a bejárás során
							delete++;
						}else empty = true;
				}				
			}
			dir++;
		}
		//Ha ütöttünk le korongot akkor lehetséges lépés, egyébként nem
		if (change.size() != 0) 
			return true;
		return false;
	}
	
	//Irányofszet meghatározása a megkapott irány alapján
	private static void DirOffset(Dir d, int dir){
		if (dir == 0)  d.sety(-1); //fel
		if (dir == 1)  d.setx(1);  //jobbra
		if (dir == 2)  d.sety(1);  //le
		if (dir == 3)  d.setx(-1); //balra
		
		if (dir == 4 || dir == 5)  d.setx(-1); //balra átlósan
		if (dir == 6 || dir == 7)  d.setx(1);  //jobbra átlósan
		if (dir == 4 || dir == 7)  d.sety(-1); //fel átlósan
		if (dir == 5 || dir == 6)  d.sety(1); //le átlósan
	}
	
	//Visszaadja az elsõ játékos által birtokolt korongok számát
	public static int PlayerOneCount(){
		int count = 0;
		for (Disc i : discs) 
			if (i.p == one) 
				count++;
		return count;
	}
	
}
