package org.exoplatform.social.portlet.profile;

import java.util.*;

public class Utils {

  static public List sort(List l, String key1, String key2, boolean reverse) {
    if (l == null)
      return null;
    Collections.sort(l, new MapSorter(key1, key2));
    if (reverse)
      Collections.reverse(l);
    return l;
  }

  static public List sort(List l, String key1, String key2) {
    return sort(l, key1, key2, false);
  }

  static public List sort(List l, String key) {
    return sort(l, key, null, false);
  }


  static private Object select(List<Map> l, String key, String value, boolean onlyFirst) {
    List res = new ArrayList<Map>();

    for (Map m : l) {
      if(m.containsKey(key) && m.get(key).equals(value)) {       
        if(onlyFirst)
          return m;
        else
          res.add(m);
      }
    }
    return res;
  }

  static public List select(List<Map> l, String key, String value) {
    return (List) select(l, key, value, false);
  }

  static public Map selectFirst(List<Map> l, String key, String value) {
    return (Map) select(l, key, value, true);
  }

  protected static class MapSorter implements Comparator {
    private String key1;
    private String key2;

    public MapSorter(String key1, String key2) {
      this.key1 = key1;
      this.key2 = key2;
    }

    public int compare(Object o1, Object o2) {
      int res = compare(o1, o2, key1);
      if(res == 0 && key2 != null)
        res = compare(o1, o2, key2);
      return res;
    }

    public int compare(Object o1, Object o2, String key) {
      HashMap h1 = null, h2 = null;

      if (o1 instanceof HashMap) {
        h1 = (HashMap) o1;
      }
      if (o2 instanceof HashMap) {
        h2 = (HashMap) o2;
      }
      if(h1 == null && h2 == null)
        return 0;
      if(h1 == null)
        return -1;
      if(h2 == null)
        return 1;
      if(!h1.containsKey(key) && !h2.containsKey(key))
        return 0;
      if(!h1.containsKey(key))
        return -1;
      if(!h2.containsKey(key))
        return 1;
      Object v1 = h1.get(key);
      Object v2 = h2.get(key);
      if(v1 instanceof String)
        return ((String)v1).compareToIgnoreCase((String)v2);
      else
        return ((Comparable)v1).compareTo(v2);
    }
  }
}
