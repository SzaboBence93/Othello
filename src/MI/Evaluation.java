package MI;

public class Evaluation {
	
	//értéktábla, az egyes pozíciók mennyire számítanak jónak az AInak
	public static int[][] eval_table = {
        {999,  -8,  15,  15,  15,  15,  -8, 999},
        {-8,  -60, -7,  -7,   -7,  -7,  -60, -8},
        { 15,  -7,  3,   2,   2,   3,   -7,  15},
        { 15,  -7,  2,   1,   1,   2,   -7,  15},
        { 15,  -7,  2,   1,   1,   2,   -7,  15},
        { 15,  -7,  3,   2,   2,   3,   -7,  15},
        {-8,  -60, -7,  -7,   -7,  -7,  -60, -8},
        {999,  -8,  15,  15,  15,  15,  -8, 999}
	}; 
	
	//Szorzó tényezõ, büntetjük azokat az állásokat, ahol sok helyre tehet az ellenfél
	public static double goodpmultiplier = 3;
}
