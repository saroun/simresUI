package cz.restrax.sim.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class TestLoader {

	public TestLoader() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public void loadMyClass() throws Exception {
		Class<String[]> cTypes = String[].class;
		String[] cArgs = new String[1];
		URL[] myURL = new URL[1];
		
		String path = new String("file:///E:/Saroun/restrax_project/work/simres/GUI/example.jar");
		myURL[0]=new URL(path);
		URLClassLoader child = new URLClassLoader (myURL, this.getClass().getClassLoader());
		Class<?> morphing = Class.forName("org.jdesktop.j3d.examples.morphing.Morphing", true, child);
		
		Method[] ms = morphing.getMethods();
		for (int i = 0; i< ms.length;i++ ) {
			System.out.printf("%d:\t%s \n",i,ms[i]);
		}
		
		Constructor<Object> c =  (Constructor<Object>) morphing.getConstructor(cTypes);
		Object instance = c.newInstance(cArgs);
		
		//Object instance = morphing.newInstance();
	//	Method method = morphing.getDeclaredMethod("setVisible",parameterTypes);
	//	Object result = method.invoke(instance,true);
		
		Method method = morphing.getDeclaredMethod("show");
		Object result = method.invoke(instance);
		
	//	Method method = morphing.getDeclaredMethod("main",cTypes);
	//	Object result = method.invoke(morphing.getinstance,cArgs);
		
   //     return result;
	}

}
