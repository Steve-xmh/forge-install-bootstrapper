package com.bangbang93;

import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Predicate;

public class ForgeInstaller {

  /**
   * 主程序函数
   *
   * @param args 参数，分别是 .minecraft 目录、版本名称、镜像源
   */
  public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException,
          InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {

    String path = args[0];

    Object action = preprocessInstaller();

    Method[] methods = action.getClass().getMethods();

    // 旧版安装器会使用 com.google.common.base.Predicate<String>
    // 而新版本会直接使用 java.util.function.Predicate<String>
    Object optionals = (Predicate<String>) (s) -> true;

    Class<?> predicateClass = tryGetClass("com.google.common.base.Predicate");
    if (predicateClass != null) {
      optionals = Proxy.newProxyInstance(predicateClass.getClassLoader(), new Class<?>[]{predicateClass}, (proxy, method, args0) -> {
        if (method.getName().equals("apply")) {
          return true;
        }
        return method.invoke(proxy, args0);
      });
    }

    Object result = null;
    File minecraftPath = new File(path);
    for (Method method : methods) {
      if (method.getName().equals("run")) {
        if (method.getParameters().length == 2) {
          result = method.invoke(action, minecraftPath, optionals);
        } else {
          Class<?> simpleInstallerClass = tryGetClass("net.minecraftforge.installer.SimpleInstaller");
          assert simpleInstallerClass != null;
          String p = simpleInstallerClass.getProtectionDomain().getCodeSource().getLocation().getPath();
          result = method.invoke(action, minecraftPath, optionals, new File(p));
        }
      }
    }
    System.out.println(result != null ? result.toString() : null);
  }

  /**
   * 预处理安装器元数据，例如设定安装版本名称，设置下载镜像源
   *
   * @return 返回一个 ClientInstall 类，用于下一步安装操作
   */
  private static Object preprocessInstaller() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, InstantiationException {
    Class<?> simpleInstallerClass = Class.forName("net.minecraftforge.installer.SimpleInstaller");
    try {
      Field headlessField = simpleInstallerClass.getDeclaredField("headless");
      headlessField.set(simpleInstallerClass, true);
    } catch (NoSuchFieldException e) {
      // 没有就不设置了
    }

    Class<?> installerClass = tryGetClass("net.minecraftforge.installer.json.InstallV1");
    if (installerClass == null) {
      installerClass = tryGetClass("net.minecraftforge.installer.json.Install");
    }

    Class<?> progressCallbackClass = tryGetClass("net.minecraftforge.installer.actions.ProgressCallback");

    Object install = null;
    Object monitor = null;

    Class<?> utilClass = tryGetClass("net.minecraftforge.installer.json.Util");
    if (utilClass != null) {
      try {
        Method loadInstallProfileMethod = utilClass.getDeclaredMethod("loadInstallProfile");
        install = loadInstallProfileMethod.invoke(utilClass);
      } catch (NoSuchMethodException ignored) {

      }
    }
    if (progressCallbackClass != null) {
      for (Method method : progressCallbackClass.getMethods()) {
        if (method.getName().equals("withOutputs")) {
          OutputStream[] arg = new OutputStream[1];
          arg[0] = System.out;
          monitor = method.invoke(progressCallbackClass, (Object) arg);
        }
      }
    }

    try {
      try {
        return Class.forName("net.minecraftforge.installer.actions.ClientInstall")
                .getConstructor(installerClass, progressCallbackClass).newInstance(install, monitor);
      } catch (IllegalArgumentException e) {
        return Class.forName("net.minecraftforge.installer.actions.ClientInstall")
                .getConstructor(installerClass, progressCallbackClass).newInstance(install, monitor);
      }
    } catch (ClassNotFoundException e) {
      return Class.forName("net.minecraftforge.installer.ClientInstall")
              .getConstructor().newInstance();
    }
  }

  private static Class<?> tryGetClass(String className) {
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }
}
