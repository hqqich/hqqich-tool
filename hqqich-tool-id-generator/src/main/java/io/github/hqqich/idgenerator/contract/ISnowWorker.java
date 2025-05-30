package io.github.hqqich.idgenerator.contract;

public interface ISnowWorker {
    long nextId() throws IdGeneratorException;
}
