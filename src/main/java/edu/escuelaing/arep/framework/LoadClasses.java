package edu.escuelaing.arep.framework;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import edu.escuelaing.arep.framework.annotatations.server;
import edu.escuelaing.arep.framework.annotatations.web;

/**
 * LoadClasses
 */
public class LoadClasses {

	public static void main(String[] args) {
		Class[] list;
		List<Class> serverClasses = new ArrayList<>();
		try {
			list = getClasses("edu.escuelaing.arep");
			for (Class c : list) {
				if (c.isAnnotationPresent(server.class))
					serverClasses.add(c);
			}
		} catch (Exception e) {
			System.err.println(e);
		}

		Map<String, Method> URLHandler = new HashMap<String, Method>();
		for (Class c : serverClasses) {
			server s = (server) c.getAnnotation(server.class);
			for (Method m : c.getMethods()) {
				if (m.isAnnotationPresent(web.class)) {
					web w = (web) m.getAnnotation(web.class);
					URLHandler.put(s.path() + w.path(), m);
				}
			}
		}

		for (Map.Entry<String, Method> url : URLHandler.entrySet()) {
			try {
				System.out.println(url.getKey());
				System.out.println(url.getValue().invoke(null));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * mappea todos los metodos con el path generado por las anotaciones server y web
	 * @return
	 */
	public static Map<String, Method> getPathClass() {
		Class[] list;
		List<Class> serverClasses = new ArrayList<>();
		try {
			list = getClasses("edu.escuelaing.arep");
			for (Class c : list) {
				if (c.isAnnotationPresent(server.class))
					serverClasses.add(c);
			}
		} catch (Exception e) {
			System.err.println(e);
		}

		Map<String, Method> URLHandler = new HashMap<String, Method>();
		for (Class c : serverClasses) {
			server s = (server) c.getAnnotation(server.class);
			for (Method m : c.getMethods()) {
				if (m.isAnnotationPresent(web.class)) {
					web w = (web) m.getAnnotation(web.class);
					URLHandler.put(s.path() + w.path(), m);
				}
			}
		}
		return URLHandler;
	}


	/**
	 * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
	 *
	 * @param packageName The base package
	 * @return The classes
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private static Class[] getClasses(String packageName) throws ClassNotFoundException, IOException {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			assert classLoader != null;
			String path = packageName.replace('.', '/');
			Enumeration resources = classLoader.getResources(path);
			List<File> dirs = new ArrayList<File>();
			while (resources.hasMoreElements()) {
					URL resource = (URL) resources.nextElement();
					dirs.add(new File(resource.getFile()));
			}
			ArrayList classes = new ArrayList();
			for (File directory : dirs) {
					classes.addAll(findClasses(directory, packageName));
			}
			return (Class[]) classes.toArray(new Class[classes.size()]);
	}

	/**
	 * Recursive method used to find all classes in a given directory and subdirs.
	 *
	 * @param directory   The base directory
	 * @param packageName The package name for classes found inside the base directory
	 * @return The classes
	 * @throws ClassNotFoundException
	 */
	private static List findClasses(File directory, String packageName) throws ClassNotFoundException {
			List classes = new ArrayList();
			if (!directory.exists()) {
					return classes;
			}
			File[] files = directory.listFiles();
			for (File file : files) {
					if (file.isDirectory()) {
							assert !file.getName().contains(".");
							classes.addAll(findClasses(file, packageName + "." + file.getName()));
					} else if (file.getName().endsWith(".class")) {
							classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
					}
			}
			return classes;
	}
}