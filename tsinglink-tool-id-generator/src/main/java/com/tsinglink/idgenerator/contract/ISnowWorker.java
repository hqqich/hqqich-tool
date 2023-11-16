package com.tsinglink.idgenerator.contract;

public interface ISnowWorker {
    long nextId() throws IdGeneratorException;
}
