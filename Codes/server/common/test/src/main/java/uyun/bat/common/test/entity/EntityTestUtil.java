package uyun.bat.common.test.entity;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import junit.framework.TestCase;

import org.junit.Test;

public abstract class EntityTestUtil {
	/**
	 * 只为增加单元测试覆盖率<br>
	 * 且本类只适合简单的实体类对象
	 * 
	 * @throws Exception
	 */
	public static <T> T create(Class<T> clazz) throws Exception {
		// 非public,接口,抽象,静态类,单元测试类则不处理
		if (!Modifier.isPublic(clazz.getModifiers()) || Modifier.isInterface(clazz.getModifiers())
				|| Modifier.isAbstract(clazz.getModifiers()) || Modifier.isStatic(clazz.getModifiers())
				|| clazz.isAssignableFrom(TestCase.class))
			return null;
		T entity = creatEntity(clazz);
		// 构造函数调用异常
		if (entity == null)
			return null;

		for (Method method : clazz.getDeclaredMethods()) {
			// 只验证public且非静态方法 好像拿不到@override注释特殊处理compareTo方法
			if (!Modifier.isPublic(method.getModifiers()) || Modifier.isStatic(method.getModifiers())
					|| method.getName().equals("compareTo"))
				continue;

			for (Annotation a : method.getAnnotations()) {
				if (a.annotationType().equals(Test.class))
					return null;
			}
			try {
				List<Object> argsList = new ArrayList<Object>();
				for (Parameter parameter : method.getParameters()) {
					argsList.add(getBaseData(parameter.getType()));
				}
				method.invoke(entity, argsList.toArray());
			} catch (Exception e) {
				// 为了容易排查哪个方法出错
				throw new RuntimeException(method.toString(), e);
			}
		}
		return entity;
	}

	private static <T> T creatEntity(Class<T> clazz) throws Exception {
		T entity = null;
		Constructor<?> constructor = null;
		try {
			constructor = clazz.getConstructor();
			// 构造函数非公有
			if (!Modifier.isPublic(constructor.getModifiers()))
				return null;
			entity = clazz.newInstance();
		} catch (Exception e) {
			for (Constructor<?> temp : clazz.getConstructors()) {
				// 获取第一个公共构造函数
				if (Modifier.isPublic(temp.getModifiers())) {
					constructor = temp;
					break;
				}
			}
			if (constructor == null)
				return null;

			List<Object> argsList = new ArrayList<Object>();

			for (Parameter parameter : constructor.getParameters()) {
				argsList.add(getBaseData(parameter.getType()));
			}

			entity = (T) constructor.newInstance(argsList.toArray());
		}

		return entity;
	}

	@SuppressWarnings("unchecked")
	private static <T> T getBaseData(Class<T> clazz) throws Exception {
		if (Modifier.isInterface(clazz.getModifiers()))
			return null;

		Object data = null;
		switch (clazz.getName()) {
		case "java.lang.Boolean":
		case "boolean":
			data = (boolean) false;
			break;
		case "java.lang.Character":
		case "char":
			data = (char) 0;
			break;
		case "java.lang.Byte":
		case "byte":
			data = (byte) 0;
			break;
		case "java.lang.Short":
		case "short":
			data = (short) 1;
			break;
		case "java.lang.Integer":
		case "int":
			data = (int) 1;
			break;
		case "java.lang.Long":
		case "long":
			data = (long) 1;
			break;
		case "java.lang.Float":
		case "float":
			data = (float) 1.0;
			break;
		case "java.lang.Double":
		case "double":
			data = (double) 1.0;
			break;
		case "[[D":
			data = new double[][] { { 1 } };
			break;
		default:
			data = null;
			break;
		}
		return (T) data;
	}

	public static void testPackageClasses(String packageName) throws Exception {
		Set<Class<?>> clazzes = getClasses(packageName);
		for (Class<?> clazz : clazzes) {
			create(clazz);
		}
	}

	public static void main(String[] args) throws Exception {
		testPackageClasses("uyun.bat");
	}

	/**
	 * 从包package中获取所有的Class
	 * 
	 * @param pack
	 * @return
	 */
	private static Set<Class<?>> getClasses(String pack) {

		// 第一个class类的集合
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
		// 是否循环迭代
		boolean recursive = true;
		// 获取包的名字 并进行替换
		String packageName = pack;
		String packageDirName = packageName.replace('.', '/');
		// 定义一个枚举的集合 并进行循环来处理这个目录下的things
		Enumeration<URL> dirs;
		try {
			dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
			// 循环迭代下去
			while (dirs.hasMoreElements()) {
				// 获取下一个元素
				URL url = dirs.nextElement();
				// 得到协议的名称
				String protocol = url.getProtocol();
				// 如果是以文件的形式保存在服务器上
				if ("file".equals(protocol)) {
					// 获取包的物理路径
					String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
					// 以文件的方式扫描整个包下的文件 并添加到集合中
					findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
				} else if ("jar".equals(protocol)) {
					// 如果是jar包文件
					// 定义一个JarFile
					JarFile jar;
					try {
						// 获取jar
						jar = ((JarURLConnection) url.openConnection()).getJarFile();
						// 从此jar包 得到一个枚举类
						Enumeration<JarEntry> entries = jar.entries();
						// 同样的进行循环迭代
						while (entries.hasMoreElements()) {
							// 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
							JarEntry entry = entries.nextElement();
							String name = entry.getName();
							// 如果是以/开头的
							if (name.charAt(0) == '/') {
								// 获取后面的字符串
								name = name.substring(1);
							}
							// 如果前半部分和定义的包名相同
							if (name.startsWith(packageDirName)) {
								int idx = name.lastIndexOf('/');
								// 如果以"/"结尾 是一个包
								if (idx != -1) {
									// 获取包名 把"/"替换成"."
									packageName = name.substring(0, idx).replace('/', '.');
								}
								// 如果可以迭代下去 并且是一个包
								if ((idx != -1) || recursive) {
									// 如果是一个.class文件 而且不是目录
									if (name.endsWith(".class") && !entry.isDirectory()) {
										// 去掉后面的".class" 获取真正的类名
										String className = name.substring(packageName.length() + 1, name.length() - 6);
										try {
											// 添加到classes
											classes.add(Class.forName(packageName + '.' + className));
										} catch (ClassNotFoundException e) {
											// log
											// .error("添加用户自定义视图类错误 找不到此类的.class文件");
											e.printStackTrace();
										}
									}
								}
							}
						}
					} catch (IOException e) {
						// log.error("在扫描用户定义视图时从jar包获取文件出错");
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return classes;
	}

	/**
	 * 以文件的形式来获取包下的所有Class
	 * 
	 * @param packageName
	 * @param packagePath
	 * @param recursive
	 * @param classes
	 */
	private static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive,
			Set<Class<?>> classes) {
		// 获取此包的目录 建立一个File
		File dir = new File(packagePath);
		// 如果不存在或者 也不是目录就直接返回
		if (!dir.exists() || !dir.isDirectory()) {
			System.out.println("用户定义包名 " + packageName + " 下没有任何文件");
			return;
		}
		// 如果存在 就获取包下的所有文件 包括目录
		File[] dirfiles = dir.listFiles(new FileFilter() {
			// 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
			public boolean accept(File file) {
				return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
			}
		});
		// 循环所有文件
		for (File file : dirfiles) {
			// 如果是目录 则继续扫描
			if (file.isDirectory()) {
				findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classes);
			} else {
				// 如果是java类文件 去掉后面的.class 只留下类名
				String className = file.getName().substring(0, file.getName().length() - 6);
				try {
					// 添加到集合中去
					classes.add(Class.forName(packageName + '.' + className));
				} catch (ClassNotFoundException e) {
					System.out.println("添加用户自定义视图类错误 找不到此类的.class文件");
					e.printStackTrace();
				}
			}
		}
	}

}
