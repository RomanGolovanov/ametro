package org.ametro.utils;

public class Lazy<T> {

    private final IFactory<T> factory;
    private T instance;

    public interface IFactory<T> {
        T create();
    }

    public Lazy(IFactory<T> factory)
    {
        this.factory = factory;
    }

    public T getInstance() {
        if(instance == null){
            synchronized (this){
                if(instance == null){
                    instance = factory.create();
                }
            }
        }
        return instance;
    }
}
