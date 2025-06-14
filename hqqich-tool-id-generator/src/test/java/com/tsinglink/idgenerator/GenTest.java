package io.github.hqqich.tool.idgenerator;


import io.github.hqqich.tool.idgenerator.util.IdUtils;

import java.util.HashSet;
import java.util.Set;

public class GenTest {

    private int GenIdCount;
    private int WorkerId;
    private Set IdSet = new HashSet();

    public GenTest(int genIdCount, int workerId) {
        GenIdCount = genIdCount;
        WorkerId = workerId;
    }

    public void GenStart() {
        long start = System.currentTimeMillis();
        long id = 0;

        for (int i = 0; i < GenIdCount; i++) {
            id = IdUtils.nextId();
        }

        long end = System.currentTimeMillis();
        long time = end - start;

        // System.out.println(id);
        System.out.println("++++++++++++++++++++++++++++++++++++++++WorkerId: "
                + WorkerId + ", total: " + time + " ms");

    }
}
