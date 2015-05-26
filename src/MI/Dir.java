package MI;

//Ir�ny oszt�ly az egyszer�s�t�s miatt, a lehets�ges lerak� helyek meghat�roz�s�n�l az ir�nyoknak megfelel� x �s y ofszet
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
