package io.github.hqqich.tool.ai.asr;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenhao on 2026/4/17 is 17:09.<p/>
 *
 * @author chenhao
 */
public class DoubaoResult {

    public DoubaoResult() {
        this.result = "";
        this.words = new ArrayList<WordTime>();
    }

    private String result;

    private List<WordTime> words;


    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public List<WordTime> getWords() {
        return words;
    }

    public void setWords(List<WordTime> words) {
        this.words = words;
    }
}
