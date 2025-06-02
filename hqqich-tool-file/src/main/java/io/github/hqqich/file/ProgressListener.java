package io.github.hqqich.file;

/**
 * Created by chenhao on 2025/3/4 is 下午5:16.<p/>
 *
 * @author chenhao
 */
public interface ProgressListener {

    void onProgress(long totalBytes, long transferredBytes);

}
