package io.github.hqqich.tool.file;

/**
 * Created by hqqich on 2025/3/4 is 下午5:16.<p/>
 *
 * @author hqqich
 */
public interface ProgressListener {

    void onProgress(long totalBytes, long transferredBytes);

}
