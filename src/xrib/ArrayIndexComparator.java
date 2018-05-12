package xrib;

import java.util.Comparator;
import java.util.ArrayList;

public class ArrayIndexComparator<T extends Comparable<T>> implements Comparator<Integer>
{
    private final ArrayList<T> array;

    public ArrayIndexComparator(ArrayList<T> array)
    {
        this.array = array;
    }

    public ArrayList<Integer> createIndexArray()
    {
        ArrayList<Integer> indexes = new ArrayList<Integer>(array.size());
        for (int i = 0; i < array.size(); i++)
        {
            indexes.add(i); 
        }
        return indexes;
    }

    @Override
    public int compare(Integer index1, Integer index2)
    {
        return array.get(index1).compareTo(array.get(index2));
    }
}
