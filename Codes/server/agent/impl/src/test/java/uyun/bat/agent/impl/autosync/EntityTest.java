package uyun.bat.agent.impl.autosync;

import org.junit.Test;
import org.meanbean.test.BeanTestException;
import org.meanbean.test.BeanTester;
import uyun.bat.agent.impl.autosync.entity.Event;
import uyun.bat.agent.impl.autosync.entity.FileOperMode;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class EntityTest {

    @Test
    public void testEntity() throws IOException, ClassNotFoundException {
        BeanTester tester = new BeanTester();
        Class[] classes = getClasses("uyun.bat.agent.impl.autosync.entity");
        for (Class clazz : classes) {
            try {
                tester.testBean(clazz);
            } catch (Exception e) {
                // 不规范的bean
            }
        }
    }



    private Class[] getClasses(String packageName)
            throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList();
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


    private List findClasses(File directory, String packageName) throws ClassNotFoundException {
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
