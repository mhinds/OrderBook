import java.util.Comparator;
/**
 * This class implements Comparator in order to reverse the order of the Double comparison. It's useful in
 * creating max TreeMaps.
**/
public class ReverseComparator implements Comparator<Double>{
	@Override
	public int compare(Double i1, Double i2){
		return -1 * Double.compare(i1,i2);
	}
}
