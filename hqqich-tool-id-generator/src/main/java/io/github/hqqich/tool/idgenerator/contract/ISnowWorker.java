package io.github.hqqich.tool.idgenerator.contract;

public interface ISnowWorker {
    long nextId() throws IdGeneratorException;
}
