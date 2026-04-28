package io.github.hqqich.tool.ai.asr;

/**
 * Created by chenhao on 2026/4/17 is 17:10.<p/>
 *
 * @author chenhao
 */
public class WordTime {

    private String text;
    private String startTime;
    private String endTime;


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
