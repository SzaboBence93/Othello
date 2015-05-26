package MI;

//Irány osztály az egyszerûsítés miatt, a lehetséges lerakó helyek meghatározásánál az irányoknak megfelelõ x és y ofszet
public class Dir{
	
	private int xo, yo;
	
	public Dir(int xo, int yo){
		this.yo = yo;
		this.xo = xo;
	}
	
	public void setx(int ujxo){
		this.xo = ujxo;
	}
	
	public void sety(int ujyo){
		this.yo = ujyo;
	}
	
	public int getx(){
		return xo;
	}
	
	public int gety(){
		return yo;
	}
}
