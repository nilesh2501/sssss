package org.basex.web.xquery.util;

import net.spy.memcached.AddrUtil;

import net.spy.memcached.BinaryConnectionFactory;

import net.spy.memcached.MemcachedClient;

/**
 * Memcache Facade.
 * @author Michael Seiferle, BaseX Team
 * @author Sudarshan Acharyam
 * http://sacharya.com/using-memcached-with-java/
 */
public final class MyCache {

  /** Number of cache instances. */
  private static final int NUM_CONN = 31;

  /** Cache items namespace. */
  private static final String NAMESPACE = "basex:";

  /** The instance. */
  private static MyCache instance;

  /** Memcache client. */
  private static MemcachedClient[] m;

  /**
   * Sets up a connection pool of initial clients.
   */
  private MyCache() {

    try {
      m = new MemcachedClient[MyCache.NUM_CONN];
      for(int i = 0; i < MyCache.NUM_CONN; i++) {
        MemcachedClient c = new MemcachedClient(new BinaryConnectionFactory(),
            AddrUtil.getAddresses("127.0.0.1:11211"));
        m[i] = c;
      }
    } catch(Exception e) { }
  }

  /**
   * Factory method.
   * @return cache instance.
   */
  public static synchronized MyCache getInstance() {
    // System.out.println("Instance: " + instance);
    if(instance == null) {
      System.out.println("Creating a new instance");
      instance = new MyCache();
    }
    return instance;
  }

  /**
   * Sets an Item.
   * @param key the key
   * @param ttl time to live in seconds
   * @param o object to set
   */
  public void set(final String key, final int ttl, final Object o) {
    getCache().set(NAMESPACE + key, ttl, o);
  }

  /**
   * Gets an item from the cache.
   * @param key the key
   * @return the cached object
   */
  public Object get(final String key) {
    final Object o = getCache().get(NAMESPACE + key);
    if(o == null) {
      System.out.println("Cache MISS for KEY: " + key);
    } else {
      // System.out.println("Cache HIT for KEY: " + key);
    }
    return o;
  }

  /**
   * Removes the object <code>key</code> from the cache pool.
   * @param key the key
   * @return the object that has been just deleted.
   */
  public Object delete(final String key) {
    return getCache().delete(NAMESPACE + key);
  }
  /**
   * Flushes memcache and removes all objects.
   */
  public void flushAll() {
    System.out.println("Flushing caches");
    getCache().flush();
    return;
  }

  /**
   * Gets an cache instance.
   * @return a cache client.
   */
  public MemcachedClient getCache() {
    MemcachedClient c = null;
    try {
      int i = (int) (Math.random() * (NUM_CONN - 1));
      c = m[i];
    } catch(Exception e) { }
    return c;
  }
}
