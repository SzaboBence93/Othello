package MI;

import java.util.List;

//Korong
public class Disc {
	//korong poz�ci�ja [0..7][0..7]
	int x, y;
	//korongot lehelyez� j�t�kos
	Player p;
	
	//A diszk �ltal le�t�tt diszkek list�ja (AI sz�m�ra)
	List<Disc> attack;
	
	public Disc(int x, int y, Player p){
		//param�rek �tad�sa
		this.x = x;
		this.y = y;
		this.p = p;
	}
}
