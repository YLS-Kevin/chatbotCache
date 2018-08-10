///**
// * 
// */
//package com.yls.app.util;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Vector;
//
///**
// * 这里集成HashMap建 根据 实例化 TMap的建保持一直， 值 这是一个List 接口，我们把相同建的值存在这个List里面
// * 
// * @author huangsy
// * @date 2018年5月22日下午4:52:06
// */
//public class TMap<T, V> extends HashMap<T, List<V>> {
//
//	private static final long serialVersionUID = 1L;
//
//	public List<V> put(T key, V value) {
//		long startTime = System.currentTimeMillis();
//		/* 判断该建是否已经存在 吗如果不存在 则放入一个新的Vector对象 */
//		if (super.get(key) == null) {
//			super.put(key, new Vector<V>());
//		}
//		/* 这里获取 key对应的List */
//		List<V> list = super.get(key);
//		/* 将当前值，放入到 key对应的List中 */
//		list.add(value);
//		System.out.println("====耗时：" + (System.currentTimeMillis() - startTime) + "ms ");
//		/* 返回当前 key对于的List对象 */
//		return super.get(list);
//	}
//
//	@Override
//	public List<V> put(T key, List<V> value) {
//		// TODO Auto-generated method stub
//		return super.put(key, value);
//	}
//
//	@Override
//	public List<V> get(Object key) {
//
//		return super.get(key);
//	}
//
//	public static void main(String[] args) {
//		TMap<String, String> map = new TMap<String, String>();
//		map.put("1", "2");
//		map.put("1", "3");
//		map.put("1", "4");
//		map.put("1", "5");
//		map.put("2", "7");
//		map.put("3", "4");
//		map.put("2", "3");
//		map.put("3", "2");
//		System.out.println(map);
//	}
//}
