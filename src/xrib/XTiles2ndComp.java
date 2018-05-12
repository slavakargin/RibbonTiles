package xrib;
import java.util.Comparator;

public class XTiles2ndComp implements Comparator<XRibTile> {
    @Override
    public int compare(XRibTile t1, XRibTile t2) {
        return t1.squares().first().compareTo(t2.squares().first());
    }
}
