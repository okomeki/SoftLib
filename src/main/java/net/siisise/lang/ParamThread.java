package net.siisise.lang;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * パラメータ付き柔軟スレッド。
 * run()ではなく、特定メソッドを「パラメータ付き」で呼び出すことができる。
 * プリミティブ型のデータに対応する
 *
 * 1つのクラスで複数のスレッド用メソッドを用意したいことはよくあります。
 * 別のクラスを用意することなく利用できます。
 *
 * @version 1.03
 */
public class ParamThread extends java.lang.Thread {

    Object targetObject;
//    java.lang.String mname;
    java.lang.reflect.Method runMethod;
    Object[] args;
    
    /**
     * 複数パラメータでメソッドを起動する.
     *
     * @param obj 対象オブジェクト 
     * @param methodName メソッド名
     * @param argv 一般的なパラメータ
     * @throws java.lang.NoSuchMethodException
     */
    public ParamThread(Object obj, java.lang.String methodName, Object... argv) throws NoSuchMethodException {
//        super();
        targetObject = obj;
        this.args = argv;
        runMethod = findMethod(methodName,argv);
    }
    
    /**
     * 初期化内部処理(共通部分)
     */
    private final Method findMethod(java.lang.String methodName, Object[] args) throws NoSuchMethodException {
        // メソッド特定のため、パラメータの型を取得
        Class[] argType = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            argType[i] = args[i].getClass();
            // Integerとintは区別可能か?
            if (argType[i].equals(java.lang.Integer.class)) {
                argType[i] = Integer.TYPE;
            } else if (argType[i].equals(java.lang.Long.class)) {
                argType[i] = Long.TYPE;
            } else if (argType[i].equals(java.lang.Character.class)) {
                argType[i] = Character.TYPE;
            } else if (argType[i].equals(java.lang.Double.class)) {
                argType[i] = Double.TYPE;
            } else if (argType[i].equals(java.lang.Float.class)) {
                argType[i] = Float.TYPE;
            } else if (argType[i].equals(java.lang.Boolean.class)) {
                argType[i] = Boolean.TYPE;
            }
        }

        try {
            // 最適なメソッドを探せない
            return targetObject.getClass().getMethod(methodName, argType);
        } catch (NoSuchMethodException ex) {
            // キャストできるものを探す
            boolean t;
            Method[] mtds = targetObject.getClass().getMethods();
            for (Method method : mtds) {
                if (method.getName().equals(methodName) && method.getParameterCount() == args.length) {
                    Class<?>[] ptypes = method.getParameterTypes();
                    t = true;
                    for (int i = 0; i < argType.length; i++) {
                        if (!ptypes[i].isAssignableFrom(argType[i])) {
                            t = false;
                            break;
                        }
                    }
                    if (t) {
                        return method;
                    }
    
                }
            }
            throw ex;
        }
    }

    /**
     * 新規スレッドで引数1つのメソッドを呼び出す
     *
     * @param obj
     * @param methodName
     * @param argv
     * @throws java.lang.NoSuchMethodException
     */
    public ParamThread(Object obj, java.lang.String methodName, Object argv) throws NoSuchMethodException {
        this(obj, methodName, new Object[]{argv});
    }
    
    /**
     * パラメータ無しメソッドを呼び出す
     *
     * @param obj
     * @param methodName
     * @throws java.lang.NoSuchMethodException
     */
    public ParamThread(Object obj, java.lang.String methodName) throws NoSuchMethodException {
        this(obj, methodName, new Object[0]);
    }
    
    /**
     * 実行部隊
     * 複数処理のキューも可能にする?
     */
    @Override
    public void run() {
        try {
            runMethod.invoke(targetObject, args);
        } catch (InvocationTargetException | IllegalAccessException ex) {
            System.err.println(targetObject);
            if (args.length > 0) {
                System.err.println(args[0]);
            }
        }
    }
}
