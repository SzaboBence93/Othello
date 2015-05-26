package MI;

import java.util.ArrayList;
import java.util.List;

public class Player {
	static int posinf = Integer.MAX_VALUE;
	static int neginf = Integer.MIN_VALUE;
	//egyes vagy kettes j�t�kos
	public boolean type;
	public Disc lastDisc; //A j�t�kos �ltal lehelyezend� korong
	public boolean ai; //Ha AI a j�t�kos akkor ez true
	private static int maxdeep; //AI rekurz�v f�ggv�ny�nek m�lys�g�nek meghat�roz�sa
	private static int test;
	private List<Integer> temp; //gyorsrendez�h�z seg�dv�ltoz�
	private boolean algorithmA = true;
	private Player other = null; //AI nak egyszer�s�t�sk�pp mutat� a m�sik j�t�kosra
	
	public Player(boolean one, boolean ai){
		//Param�terek be�ll�t�sa
		type = one;
		lastDisc = null;
		this.ai = ai;
		maxdeep = 5;
	}
	
	public void Update(){	
		//Ha AI akkor, egy�bk�nt sim�n csak beadunk egy �j korongot �s r�b�zzuk a kontrollerre a tov�bbiakat
		if (ai) {
			//m�sik j�t�kosra mutat� el��ll�t�sa
			if (other == null) 
				other = (type) ? Game.two : Game.one;
			
			//Lek�rj�k a Game oszt�lyt�l a lent l�v� korongokat (biztons�gi okokb�l nem f�r�nk k�zvetlen�l hozz�)
			List<Disc> ds = new ArrayList<>();
			Disc d;
			int i=0;
			while((d = Game.GetDisc(i++)) != null){ 
				ds.add(d); 
			}
			
			//Ha min szinttel kezd�nk
			boolean min = true;
			
			
			test = 0;
			//Algoritmus v�lt�s 54 lerakott diszk ut�n
			if (Game.discsize() == 54){
				algorithmA = false;
				maxdeep = 10;
			}
			//Rekurz�v AI f�ggv�ny megh�v�sa, MIN-MAX fa ki�p�t�s�re �s �rt�kel�s�re
			FindBest(Game.NBdiscs,ds,this, 0, !min, neginf, posinf);
			//L�p�sek sz�m�nak ki�r�sa
			System.out.println(test);
			
			//A kiv�lasztott korong beker�l a j�t�kba, m�sodik param�ter false, teh�t a kontroller nem veszi �t
			Game.AddDisc(lastDisc, false);
			
			int x = lastDisc.x;
			int y = lastDisc.y;
			lastDisc.x = lastDisc.y = 9;
			
			//Berakjuk a j�t�k ellen�rz� f�ggv�ny�be, ami ha minden ok�, lehelyezi a korongot a meghat�rozott helyre.
			Game.Step(x, y, lastDisc);
		} else 
			{
				//�j korong meghat�rozatlan helyre
				lastDisc = new Disc(9,9,this);
				//Beadjuk a j�t�kba, innen �tveszi a kontroller, mivel a m�sodik param�ter true
				Game.AddDisc(lastDisc, true);
			}
		
		
	}
	
	private int FindBest(List<Disc> nb,List<Disc> ds, Player act, 
							int deep, boolean min,int alpha,int beta){
		//Lehets�ges lerakhat� helyek meghat�roz�sa szomsz�d list�b�l
		
		test++;
		List<Disc> goodp = new ArrayList<>();
		goodp = Game.PossibleDisc(nb, act, ds);
		//Ki�rt�kel�seket tartalmaz� lista
		List<Integer> rs = new ArrayList<>();
		
		//Ha m�lys�g nulla
		if (deep == 0 && algorithmA){
			//�s va olyan lehets�ges korong ami a sarokba ker�l, akkor azt m�ris kiv�lasztjuk �s kil�p�nk a f�ggv�nyb�l
			for (Disc i : goodp){
				if ((i.x == 7 || i.x == 0) && (i.y == 7 || i.y == 0)){
					lastDisc = i;
					lastDisc.p = this;
					return 0;
				}
			}
				
		}
		
		//Ha nincs egyetlen egy lehets�ges lerakhat� hely sem, akkor visszat�r�nk
		if (goodp.size() == 0) 
			return (act == other) ? neginf +1 : posinf-1;


		//Ha el�rt�k a levelet az visszadja a lehets�ges l�p�seinek sz�m�t
		if (deep >= maxdeep) 
			return (algorithmA) ? evaluate(goodp, ds) : Balgorithm(ds);
		
		
		
		//Heurisztikus rendez�s, a min-max v�g�s felgyors�t�s�hoz
		sort(goodp, min);
		
		/*if (algorithmA)
		for(Disc i : goodp){
			//ellen�rizz�k, hogy az adott szinten nincs e  olyan hogy az Ai j�tkos, vagy az ellenf�l sarokba l�phet
			
			if (act == other && ((i.x == 7 || i.x == 0) && (i.y == 7 || i.y == 0))) //ha az ellenf�l sarokba l�phet
				return neginf+2; //A felette l�v� szint biztos nem ezt fogja v�llasztani
			
			if (act == this && ((i.x == 7 || i.x == 0) && (i.y == 7 || i.y == 0))) //Ha az AI sarokba l�phet 
				return posinf-2; //A felette l�v� szint biztos ezt fogja v�lasztani
			
		}*/		
		

		
		//Ha a fentiek egyike sem k�vetkezik be akkor...
		for(Disc i : goodp)
		{
			//�j list�k legener�l�sa, hogy a k�l�nb�z� esetek ne babr�ljanak egym�s referenci�ival
			//�j lerakott korongokat tartalmaz� lista
			List<Disc> nds = new ArrayList<>();
			for (Disc j : ds) 
				nds.add(new Disc(j.x, j.y, j.p));
			for (Disc j : i.attack) 
				nds.get(ds.indexOf(j)).p = act;
			
			//Beletessz�k az aktu�lis i korongot, mint most kiv�lasztott korongot
			nds.add(new Disc(i.x, i.y, act));
			
			//Szomsz�dok el��ll�t�sa
			List<Disc> nnb = new ArrayList<>();
			for (Disc j : nb) 
				nnb.add(new Disc(j.x, j.y, null));
			
			//Szomsz�dok m�sol�t�nak v�ltoztat�sa az �ppen lerakott korong alapj�n
			Game.NBUpdate(nnb, nds.get(nds.size()-1), nds);
			
			//M�lyebbi rekurzi� megh�v�sa az �j m�solat list�kkal
			int tm = FindBest(nnb, nds, (act == this) ? other : this, deep+1,
								min ? false : true, alpha,beta);
			
			//Ha kaptunk egy sz�ls�s�ges �rt�ket akkor kap�sb�l viszat�r�nk egy m�lys�gi szinttel feljebb, mert a min, vagy max �gy is azt v�lasztan� ki
			//felesleges lenne megn�zni a t�bbi �gat is
			/*if (deep != 0 && ((min && tm == neginf+2) || (!min && tm==posinf-2))) 
				return tm;*/
			rs.add(new Integer(tm)); //Ha nem kiugr� eset akkor sim�n gy�jtj�k ki�rt�kel�sre a t�rol�ban
			
			//alfa-b�ta v�g�s
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
		
		//Ha a gy�k�rben vagyunk akkor..
		if (deep == 0){ 
			Integer re = alpha;
			lastDisc = goodp.get(rs.indexOf(re));
			lastDisc.p = this;

			//Kis ki�r�s :D debug c�lb�l
			if (re == neginf+2 || re == posinf-2)
				System.out.println((re == neginf+2 ) ? "Ajjaj, Te igen j� vagy!" : "K�ssz az ingyen sarkot!");
			return re;
		}
		
		
		//Az adott m�lys�gi szintnek megfelel�en mint vagy maxot v�lasztunk
		if (min) 
			return beta; 
		else 
			return alpha;
		
		
	}
	
	//B algoritmus f�ggv�nye
	private int Balgorithm(List<Disc> ds) {
		int re = 0;
		//Egyszer�en ki�rt�keli a t�bl�n l�v� korongok alapj�n h mennyire j� az ai j�t�kos sz�m�ra
		for (Disc i : ds) 
			if(i.p == this) 
				re++; 
			else re--;
		return re;
	}
	
	//gyorsrendez�s vez�nyl�se
	private void sort(List<Disc> goodp, boolean min) {
		temp = new ArrayList<>();
		//temp-et felt�ltj�k a heurisztikus helyzetnek megfelel�en, 
		//majd gyors rendez�ssel rendezz�k ennek a t�mbnek a rendez�s�vel
		//az adott szinten l�v� korongok �tn�z�si sorrendj�t is
		for (Disc i : goodp)
			temp.add((!min) ? Evaluation.eval_table[i.x][i.y] : Evaluation.eval_table[i.x][i.y]*-1);
		quickSort(temp, goodp, 0, temp.size()-1); //gyors rendez�s
		
	}
	
	private int evaluate(List<Disc> goodp, List<Disc> ds){
		int re = 0;
		//az ellenf�l min�l kevesebb l�p�se lehets�ges ann�l jobb nek�nk
		re = re - (int)(goodp.size()*Evaluation.goodpmultiplier);
		//Ki�rt�kel�si t�bla haszn�lata �s meg�llap�t�sa, hogy az adott l�p�s mennyire j� az Ainak
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

	//Gyorsrendez�s	
	private static void quickSort(List<Integer> a, List<Disc> goodp, int left,int right){
		
		//Ha a k�t part�ci� �ssze�rt v�gezt�nk
		if(left >= right)
			return;
		
		// Part�ci�kra bont�s
		int pivot = a.get(right);
		int partition = partition(a, goodp,left, right, pivot);
		
		//Rekurz�v megh�v�sa a quickSort nak, a m�dos�tott part�ci�kkal
		quickSort(a,goodp,0, partition-1);
		quickSort(a,goodp,partition+1, right);
	}
	
	// part�cion�l�
	private static int partition(List<Integer> a,List<Disc> goodp,int left,int right,int pivot){
		int leftCursor = left-1;
		int rightCursor = right;
		while(leftCursor < rightCursor){
                while(a.get(++leftCursor) < pivot);
                while(rightCursor > 0 && a.get(--rightCursor) > pivot);
			if(leftCursor >= rightCursor){
				break;
			}else{
				//elemek cser�je
				swap(a,goodp,  leftCursor, rightCursor);
			}
		}
		//elemek cser�je
		swap(a,goodp, leftCursor, right);
		return leftCursor;
	}
	
	// k�t elem felcser�l�se indexek alapj�n
	public static void swap(List<Integer> a,List<Disc> goodp,int left,int right){
		//a rendez�sre haszn�lt temp t�mb�nkkel p�rhuzamosan rendezz�k az adott szinten l�v� korongokat tartalmaz�
		//t�mb�t is a meghat�rozott indexek kicser�l�s�vel
		int temp = a.get(left);
		Disc td = goodp.get(left);
		a.set(left,a.get(right));
		goodp.set(left, goodp.get(right));
		a.set(right,temp);
		goodp.set(right, td);
	}
}
