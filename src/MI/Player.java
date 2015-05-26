package MI;

import java.util.ArrayList;
import java.util.List;

public class Player {
	static int posinf = Integer.MAX_VALUE;
	static int neginf = Integer.MIN_VALUE;
	//egyes vagy kettes játékos
	public boolean type;
	public Disc lastDisc; //A játékos által lehelyezendõ korong
	public boolean ai; //Ha AI a játékos akkor ez true
	private static int maxdeep; //AI rekurzív függvényének mélységének meghatározása
	private static int test;
	private List<Integer> temp; //gyorsrendezõhöz segédváltozó
	private boolean algorithmA = true;
	private Player other = null; //AI nak egyszerûsítésképp mutató a másik játékosra
	
	public Player(boolean one, boolean ai){
		//Paraméterek beállítása
		type = one;
		lastDisc = null;
		this.ai = ai;
		maxdeep = 5;
	}
	
	public void Update(){	
		//Ha AI akkor, egyébként simán csak beadunk egy új korongot és rábízzuk a kontrollerre a továbbiakat
		if (ai) {
			//másik játékosra mutató elõállítása
			if (other == null) 
				other = (type) ? Game.two : Game.one;
			
			//Lekérjük a Game osztálytól a lent lévõ korongokat (biztonsági okokból nem férünk közvetlenül hozzá)
			List<Disc> ds = new ArrayList<>();
			Disc d;
			int i=0;
			while((d = Game.GetDisc(i++)) != null){ 
				ds.add(d); 
			}
			
			//Ha min szinttel kezdünk
			boolean min = true;
			
			
			test = 0;
			//Algoritmus váltás 54 lerakott diszk után
			if (Game.discsize() == 54){
				algorithmA = false;
				maxdeep = 10;
			}
			//Rekurzív AI függvény meghívása, MIN-MAX fa kiépítésére és értékelésére
			FindBest(Game.NBdiscs,ds,this, 0, !min, neginf, posinf);
			//Lépések számának kiírása
			System.out.println(test);
			
			//A kiválasztott korong bekerül a játékba, második paraméter false, tehát a kontroller nem veszi át
			Game.AddDisc(lastDisc, false);
			
			int x = lastDisc.x;
			int y = lastDisc.y;
			lastDisc.x = lastDisc.y = 9;
			
			//Berakjuk a játék ellenörzõ függvényébe, ami ha minden oké, lehelyezi a korongot a meghatározott helyre.
			Game.Step(x, y, lastDisc);
		} else 
			{
				//új korong meghatározatlan helyre
				lastDisc = new Disc(9,9,this);
				//Beadjuk a játékba, innen átveszi a kontroller, mivel a második paraméter true
				Game.AddDisc(lastDisc, true);
			}
		
		
	}
	
	private int FindBest(List<Disc> nb,List<Disc> ds, Player act, 
							int deep, boolean min,int alpha,int beta){
		//Lehetséges lerakható helyek meghatározása szomszéd listából
		
		test++;
		List<Disc> goodp = new ArrayList<>();
		goodp = Game.PossibleDisc(nb, act, ds);
		//Kiértékeléseket tartalmazó lista
		List<Integer> rs = new ArrayList<>();
		
		//Ha mélység nulla
		if (deep == 0 && algorithmA){
			//És va olyan lehetséges korong ami a sarokba kerül, akkor azt máris kiválasztjuk és kilépünk a függvénybõl
			for (Disc i : goodp){
				if ((i.x == 7 || i.x == 0) && (i.y == 7 || i.y == 0)){
					lastDisc = i;
					lastDisc.p = this;
					return 0;
				}
			}
				
		}
		
		//Ha nincs egyetlen egy lehetséges lerakható hely sem, akkor visszatérünk
		if (goodp.size() == 0) 
			return (act == other) ? neginf +1 : posinf-1;


		//Ha elértük a levelet az visszadja a lehetséges lépéseinek számát
		if (deep >= maxdeep) 
			return (algorithmA) ? evaluate(goodp, ds) : Balgorithm(ds);
		
		
		
		//Heurisztikus rendezés, a min-max vágás felgyorsításához
		sort(goodp, min);
		
		/*if (algorithmA)
		for(Disc i : goodp){
			//ellenõrizzük, hogy az adott szinten nincs e  olyan hogy az Ai jétkos, vagy az ellenfél sarokba léphet
			
			if (act == other && ((i.x == 7 || i.x == 0) && (i.y == 7 || i.y == 0))) //ha az ellenfél sarokba léphet
				return neginf+2; //A felette lévõ szint biztos nem ezt fogja vállasztani
			
			if (act == this && ((i.x == 7 || i.x == 0) && (i.y == 7 || i.y == 0))) //Ha az AI sarokba léphet 
				return posinf-2; //A felette lévõ szint biztos ezt fogja választani
			
		}*/		
		

		
		//Ha a fentiek egyike sem következik be akkor...
		for(Disc i : goodp)
		{
			//Új listák legenerálása, hogy a különbözõ esetek ne babráljanak egymás referenciáival
			//Új lerakott korongokat tartalmazó lista
			List<Disc> nds = new ArrayList<>();
			for (Disc j : ds) 
				nds.add(new Disc(j.x, j.y, j.p));
			for (Disc j : i.attack) 
				nds.get(ds.indexOf(j)).p = act;
			
			//Beletesszük az aktuális i korongot, mint most kiválasztott korongot
			nds.add(new Disc(i.x, i.y, act));
			
			//Szomszédok elõállítása
			List<Disc> nnb = new ArrayList<>();
			for (Disc j : nb) 
				nnb.add(new Disc(j.x, j.y, null));
			
			//Szomszédok másolátának változtatása az éppen lerakott korong alapján
			Game.NBUpdate(nnb, nds.get(nds.size()-1), nds);
			
			//Mélyebbi rekurzió meghívása az új másolat listákkal
			int tm = FindBest(nnb, nds, (act == this) ? other : this, deep+1,
								min ? false : true, alpha,beta);
			
			//Ha kaptunk egy szélsõséges értéket akkor kapásból viszatérünk egy mélységi szinttel feljebb, mert a min, vagy max úgy is azt választaná ki
			//felesleges lenne megnézni a többi ágat is
			/*if (deep != 0 && ((min && tm == neginf+2) || (!min && tm==posinf-2))) 
				return tm;*/
			rs.add(new Integer(tm)); //Ha nem kiugró eset akkor simán gyûjtjük kiértékelésre a tárolóban
			
			//alfa-béta vágás
			if (min) {
				int hasonlitando = tm; 
				if(hasonlitando < alpha)
					return hasonlitando;
				if(hasonlitando < beta)
					beta = hasonlitando;
			}
			else {
				int hasonlitando = tm;
				if(beta < hasonlitando)
					return hasonlitando;
				if(hasonlitando > alpha)
					alpha = hasonlitando;
			}
		}
		
		//Ha a gyökérben vagyunk akkor..
		if (deep == 0){ 
			Integer re = alpha;
			lastDisc = goodp.get(rs.indexOf(re));
			lastDisc.p = this;

			//Kis kiírás :D debug célból
			if (re == neginf+2 || re == posinf-2)
				System.out.println((re == neginf+2 ) ? "Ajjaj, Te igen jó vagy!" : "Kössz az ingyen sarkot!");
			return re;
		}
		
		
		//Az adott mélységi szintnek megfelelõen mint vagy maxot választunk
		if (min) 
			return beta; 
		else 
			return alpha;
		
		
	}
	
	//B algoritmus függvénye
	private int Balgorithm(List<Disc> ds) {
		int re = 0;
		//Egyszerûen kiértékeli a táblán lévõ korongok alapján h mennyire jó az ai játékos számára
		for (Disc i : ds) 
			if(i.p == this) 
				re++; 
			else re--;
		return re;
	}
	
	//gyorsrendezés vezénylése
	private void sort(List<Disc> goodp, boolean min) {
		temp = new ArrayList<>();
		//temp-et feltöltjük a heurisztikus helyzetnek megfelelõen, 
		//majd gyors rendezéssel rendezzük ennek a tömbnek a rendezésével
		//az adott szinten lévõ korongok átnézési sorrendjét is
		for (Disc i : goodp)
			temp.add((!min) ? Evaluation.eval_table[i.x][i.y] : Evaluation.eval_table[i.x][i.y]*-1);
		quickSort(temp, goodp, 0, temp.size()-1); //gyors rendezés
		
	}
	
	private int evaluate(List<Disc> goodp, List<Disc> ds){
		int re = 0;
		//az ellenfél minél kevesebb lépése lehetséges annál jobb nekünk
		re = re - (int)(goodp.size()*Evaluation.goodpmultiplier);
		//Kiértékelési tábla használata és megállapítása, hogy az adott lépés mennyire jó az Ainak
		for (Disc i : ds){
			if(i.p == this){
				re = re + Evaluation.eval_table[i.x][i.y];
			}
			else{
				re = re - Evaluation.eval_table[i.x][i.y];
			}
		}
			
		return re;
	}

	//Gyorsrendezés	
	private static void quickSort(List<Integer> a, List<Disc> goodp, int left,int right){
		
		//Ha a két partíció összeért végeztünk
		if(left >= right)
			return;
		
		// Partíciókra bontás
		int pivot = a.get(right);
		int partition = partition(a, goodp,left, right, pivot);
		
		//Rekurzív meghívása a quickSort nak, a módosított partíciókkal
		quickSort(a,goodp,0, partition-1);
		quickSort(a,goodp,partition+1, right);
	}
	
	// partícionáló
	private static int partition(List<Integer> a,List<Disc> goodp,int left,int right,int pivot){
		int leftCursor = left-1;
		int rightCursor = right;
		while(leftCursor < rightCursor){
                while(a.get(++leftCursor) < pivot);
                while(rightCursor > 0 && a.get(--rightCursor) > pivot);
			if(leftCursor >= rightCursor){
				break;
			}else{
				//elemek cseréje
				swap(a,goodp,  leftCursor, rightCursor);
			}
		}
		//elemek cseréje
		swap(a,goodp, leftCursor, right);
		return leftCursor;
	}
	
	// két elem felcserélése indexek alapján
	public static void swap(List<Integer> a,List<Disc> goodp,int left,int right){
		//a rendezésre használt temp tömbünkkel párhuzamosan rendezzük az adott szinten lévõ korongokat tartalmazó
		//tömböt is a meghatározott indexek kicserélésével
		int temp = a.get(left);
		Disc td = goodp.get(left);
		a.set(left,a.get(right));
		goodp.set(left, goodp.get(right));
		a.set(right,temp);
		goodp.set(right, td);
	}
}
