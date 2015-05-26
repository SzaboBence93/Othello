package MI;

import java.util.List;

//Korong
public class Disc {
	//korong pozíciója [0..7][0..7]
	int x, y;
	//korongot lehelyezõ játékos
	Player p;
	
	//A diszk által leütött diszkek listája (AI számára)
	List<Disc> attack;
	
	public Disc(int x, int y, Player p){
		//paramérek átadása
		this.x = x;
		this.y = y;
		this.p = p;
	}
}
